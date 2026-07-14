#!/bin/bash
set -euo pipefail
echo "==== PODS ===="
sudo kubectl get pods -n sla-monitoring

echo "==== ACTUATOR ===="
curl -sS -m 10 -o /tmp/p.txt -w "actuator=%{http_code}\n" \
  --header "Host: sla-monitoring.local" \
  "http://127.0.0.1:8000/actuator/prometheus"
grep -E "jvm_|http_server|application=" /tmp/p.txt | head -20 || true

echo "==== PROM TARGETS ===="
curl -sS -m 10 --header "Host: prometheus.sla-monitoring.local" \
  "http://127.0.0.1:8000/api/v1/targets" > /tmp/targets.json
python3 -c 'import json; d=json.load(open("/tmp/targets.json"));
[print(t["labels"].get("job"), t["health"], (t.get("lastError") or "")[:120]) for t in d.get("data",{}).get("activeTargets",[])]'

echo "==== GRAFANA ===="
curl -sS -m 8 -o /dev/null -w "grafana_login=%{http_code}\n" \
  --header "Host: grafana.sla-monitoring.local" \
  "http://127.0.0.1:8000/login"
