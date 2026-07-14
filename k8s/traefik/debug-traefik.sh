#!/bin/bash
set -euo pipefail

echo "==== ENABLE API INSECURE + DEBUG ===="
# Get current args as JSON and rebuild with extra flags
sudo kubectl -n kube-system get deploy traefik -o json > /tmp/traefik-deploy.json

python3 << 'PY'
import json
with open("/tmp/traefik-deploy.json") as f:
    d = json.load(f)
args = d["spec"]["template"]["spec"]["containers"][0]["args"]
# normalize log level and add api.insecure
args = [a for a in args if not a.startswith("--log.level=") and a != "--api.insecure=true"]
args.append("--log.level=DEBUG")
args.append("--api.insecure=true")
d["spec"]["template"]["spec"]["containers"][0]["args"] = args
# clear any leftover env
env = d["spec"]["template"]["spec"]["containers"][0].get("env") or []
d["spec"]["template"]["spec"]["containers"][0]["env"] = [e for e in env if e.get("name") != "TRAEFIK_LOG_LEVEL"]
with open("/tmp/traefik-deploy-patched.json", "w") as f:
    json.dump(d, f)
print("ARGS:")
for a in args:
    print(" ", a)
PY

sudo kubectl -n kube-system scale deploy/traefik --replicas=0
sleep 2
sudo kubectl apply -f /tmp/traefik-deploy-patched.json
sudo kubectl -n kube-system scale deploy/traefik --replicas=1
sudo kubectl -n kube-system rollout status deploy/traefik --timeout=120s
sleep 2

echo "==== ROUTERS ===="
curl -sS http://127.0.0.1:8080/api/http/routers | python3 -m json.tool | head -200

echo "==== SERVICES ===="
curl -sS http://127.0.0.1:8080/api/http/services | python3 -m json.tool | head -100

echo "==== TEST CURL ===="
curl -sS -o /tmp/out.html -w "catch_http=%{http_code}\n" http://127.0.0.1:8000/
head -c 200 /tmp/out.html; echo
curl -sS -o /tmp/out2.html -w "host_http=%{http_code}\n" -H "Host: sla-monitoring.local" http://127.0.0.1:8000/
head -c 200 /tmp/out2.html; echo
curl -sS -o /tmp/out3.html -w "lb_http=%{http_code}\n" -H "Host: sla-monitoring.local" http://84.8.216.210/
head -c 200 /tmp/out3.html; echo

echo "==== DEBUG LOG (crd/ingress) ===="
sudo kubectl -n kube-system logs deploy/traefik --tail=200 | grep -iE 'error|warn|ingress|crd|sla-monitoring|frontend|router' | head -80
