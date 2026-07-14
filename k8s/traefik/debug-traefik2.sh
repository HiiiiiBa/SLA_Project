#!/bin/bash
set -euo pipefail

echo "==== SCALE DOWN ===="
sudo kubectl -n kube-system scale deploy/traefik --replicas=0
sleep 3
sudo kubectl -n kube-system delete pod -l app.kubernetes.io/name=traefik --force --grace-period=0 --ignore-not-found || true
sleep 2

echo "==== PATCH ARGS ===="
sudo kubectl -n kube-system get deploy traefik -o json > /tmp/traefik-deploy.json
python3 << 'PY'
import json, copy
with open("/tmp/traefik-deploy.json") as f:
    d = json.load(f)
# remove status and resourceVersion conflict fields for replace
d.pop("status", None)
md = d["metadata"]
for k in ["resourceVersion", "uid", "generation", "creationTimestamp", "managedFields"]:
    md.pop(k, None)
args = d["spec"]["template"]["spec"]["containers"][0]["args"]
args = [a for a in args if not a.startswith("--log.level=") and a != "--api.insecure=true"]
args += ["--log.level=DEBUG", "--api.insecure=true"]
d["spec"]["template"]["spec"]["containers"][0]["args"] = args
d["spec"]["replicas"] = 1
with open("/tmp/traefik-deploy-patched.json", "w") as f:
    json.dump(d, f)
print("OK args:", args)
PY

sudo kubectl replace -f /tmp/traefik-deploy-patched.json
sudo kubectl -n kube-system rollout status deploy/traefik --timeout=120s
sleep 3

echo "==== ROUTERS ===="
curl -sS http://127.0.0.1:8080/api/http/routers | python3 -m json.tool | head -250 || true

echo "==== TEST ===="
curl -sS -o /tmp/out.html -w "nohost=%{http_code}\n" http://127.0.0.1:8000/ || true
head -c 120 /tmp/out.html; echo
curl -sS -o /tmp/out2.html -w "withhost=%{http_code}\n" --header "Host: sla-monitoring.local" http://127.0.0.1:8000/ || true
head -c 200 /tmp/out2.html; echo
curl -sS -o /tmp/out3.html -w "lb=%{http_code}\n" --header "Host: sla-monitoring.local" http://84.8.216.210/ || true
head -c 200 /tmp/out3.html; echo

echo "==== LOG FILTER ===="
sudo kubectl -n kube-system logs deploy/traefik --tail=300 | grep -iE 'error|warn|Configuration|ingressroute|sla-monitoring|Provider error|Cannot' | head -100 || true
