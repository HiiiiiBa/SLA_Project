#!/bin/bash
set -euo pipefail

echo "==== FOLLOW REDIRECT ===="
curl -sS -L -o /tmp/final.html -w "final=%{http_code} url=%{url_effective}\n" --header "Host: sla-monitoring.local" http://84.8.216.210/
head -c 300 /tmp/final.html; echo

echo "==== CLEANUP duplicate catch-all ===="
sudo kubectl delete ingressroute catch-all -n sla-monitoring --ignore-not-found

echo "==== DISABLE api.insecure (keep API fix env) ===="
sudo kubectl -n kube-system scale deploy/traefik --replicas=0
sleep 3
sudo kubectl -n kube-system get deploy traefik -o json > /tmp/traefik-deploy.json
python3 << 'PY'
import json
with open("/tmp/traefik-deploy.json") as f:
    d = json.load(f)
d.pop("status", None)
md = d["metadata"]
for k in ["resourceVersion", "uid", "generation", "creationTimestamp", "managedFields"]:
    md.pop(k, None)
c = d["spec"]["template"]["spec"]["containers"][0]
c["args"] = [a for a in c["args"] if a != "--api.insecure=true" and not a.startswith("--log.level=")]
c["args"].append("--log.level=INFO")
# ensure API host override remains
names = {e.get("name") for e in (c.get("env") or [])}
env = [e for e in (c.get("env") or []) if e.get("name") not in (
    "KUBERNETES_SERVICE_HOST", "KUBERNETES_SERVICE_PORT", "KUBERNETES_SERVICE_PORT_HTTPS"
)]
env += [
    {"name": "KUBERNETES_SERVICE_HOST", "value": "127.0.0.1"},
    {"name": "KUBERNETES_SERVICE_PORT", "value": "6443"},
    {"name": "KUBERNETES_SERVICE_PORT_HTTPS", "value": "6443"},
]
c["env"] = env
d["spec"]["template"]["spec"]["dnsPolicy"] = "ClusterFirstWithHostNet"
d["spec"]["replicas"] = 1
with open("/tmp/traefik-secure.json", "w") as f:
    json.dump(d, f)
print("OK")
PY
sudo kubectl replace -f /tmp/traefik-secure.json
sudo kubectl -n kube-system rollout status deploy/traefik --timeout=120s
sleep 3

echo "==== FINAL TEST ===="
curl -sS -o /tmp/f1.html -w "host=%{http_code}\n" --header "Host: sla-monitoring.local" http://84.8.216.210/
curl -sS -L -o /tmp/f2.html -w "follow=%{http_code}\n" --header "Host: sla-monitoring.local" http://84.8.216.210/
# show title-ish
grep -o '<title>[^<]*</title>' /tmp/f2.html | head -3 || head -c 200 /tmp/f2.html; echo

echo "==== PERSIST via HelmChartConfig (k3s) ===="
sudo tee /var/lib/rancher/k3s/server/manifests/traefik-api-host.yaml > /dev/null << 'EOF'
apiVersion: helm.cattle.io/v1
kind: HelmChartConfig
metadata:
  name: traefik
  namespace: kube-system
spec:
  valuesContent: |-
    deployment:
      podAnnotations: {}
    additionalArguments: []
    # Force in-cluster client to use local apiserver (hostNetwork cannot reach 10.43.0.1)
    env:
      - name: KUBERNETES_SERVICE_HOST
        value: "127.0.0.1"
      - name: KUBERNETES_SERVICE_PORT
        value: "6443"
      - name: KUBERNETES_SERVICE_PORT_HTTPS
        value: "6443"
    deployment:
      dnsPolicy: ClusterFirstWithHostNet
EOF
echo "HelmChartConfig written"
sudo kubectl get ingressroute,ingress -n sla-monitoring
sudo kubectl -n kube-system get deploy traefik -o jsonpath='{.spec.template.spec.containers[0].env}' ; echo
