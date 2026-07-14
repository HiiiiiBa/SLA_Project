#!/bin/bash
set -euo pipefail

echo "==== VERIFY LOCAL API ===="
TOKEN=$(sudo kubectl -n kube-system create token traefik --duration=10m)
curl -sk --cacert /var/lib/rancher/k3s/server/tls/server-ca.crt \
  -H "Authorization: Bearer $TOKEN" \
  https://127.0.0.1:6443/apis/traefik.io/v1alpha1/namespaces/sla-monitoring/ingressroutes \
  | python3 -c 'import sys,json; d=json.load(sys.stdin); print("ingressroutes via 127.0.0.1:6443 =", len(d.get("items",[])))'

# Also test with SA ca.crt from a projected volume perspective
POD=$(sudo kubectl -n kube-system get pod -l app.kubernetes.io/name=traefik -o jsonpath='{.items[0].metadata.name}')
sudo kubectl -n kube-system exec "$POD" -- sh -c '
TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)
CACERT=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
wget -qO- --timeout=5 --ca-certificate=$CACERT \
  --header="Authorization: Bearer $TOKEN" \
  https://127.0.0.1:6443/apis/traefik.io/v1alpha1/namespaces/sla-monitoring/ingressroutes 2>&1 | head -c 200
echo
'

echo "==== PATCH TRAEFIK TO USE 127.0.0.1:6443 ===="
sudo kubectl -n kube-system scale deploy/traefik --replicas=0
sleep 3
sudo kubectl -n kube-system delete pod -l app.kubernetes.io/name=traefik --force --grace-period=0 --ignore-not-found || true
sleep 2

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
args = [a for a in c["args"] if not a.startswith("--log.level=") and a != "--api.insecure=true"]
# keep insecure API briefly to verify routers, INFO logs
args += ["--log.level=INFO", "--api.insecure=true"]
c["args"] = args

env = c.get("env") or []
# remove previous overrides if any
env = [e for e in env if e.get("name") not in (
    "KUBERNETES_SERVICE_HOST", "KUBERNETES_SERVICE_PORT",
    "KUBERNETES_SERVICE_PORT_HTTPS", "TRAEFIK_LOG_LEVEL"
)]
env += [
    {"name": "KUBERNETES_SERVICE_HOST", "value": "127.0.0.1"},
    {"name": "KUBERNETES_SERVICE_PORT", "value": "6443"},
    {"name": "KUBERNETES_SERVICE_PORT_HTTPS", "value": "6443"},
]
c["env"] = env

# Fix DNS for hostNetwork
d["spec"]["template"]["spec"]["dnsPolicy"] = "ClusterFirstWithHostNet"
d["spec"]["replicas"] = 1

with open("/tmp/traefik-fixed.json", "w") as f:
    json.dump(d, f)
print("Patched env + dnsPolicy OK")
PY

sudo kubectl replace -f /tmp/traefik-fixed.json
sudo kubectl -n kube-system rollout status deploy/traefik --timeout=120s
sleep 4

echo "==== ROUTERS AFTER FIX ===="
curl -sS http://127.0.0.1:8080/api/http/routers | python3 -c '
import sys,json
d=json.load(sys.stdin)
print(len(d), "routers")
for r in d:
    print("-", r.get("name"), "|", r.get("rule"), "|", r.get("entryPoints"), "|", r.get("provider"))
'

echo "==== HTTP TESTS ===="
curl -sS -o /tmp/t1.html -w "nohost=%{http_code}\n" http://127.0.0.1:8000/
head -c 120 /tmp/t1.html; echo
curl -sS -o /tmp/t2.html -w "withhost=%{http_code}\n" --header "Host: sla-monitoring.local" http://127.0.0.1:8000/
head -c 200 /tmp/t2.html; echo
curl -sS -o /tmp/t3.html -w "lb=%{http_code}\n" --header "Host: sla-monitoring.local" http://84.8.216.210/
head -c 200 /tmp/t3.html; echo

echo "==== LOGS ===="
sudo kubectl -n kube-system logs deploy/traefik --tail=40
