#!/bin/bash
set -euo pipefail
echo "==== PODS ===="
sudo kubectl get pods -n sla-monitoring -l 'app in (loki,promtail,grafana)'
echo "==== LOKI READY ===="
LP=$(sudo kubectl get pod -n sla-monitoring -l app=loki -o jsonpath='{.items[0].status.podIP}')
curl -sS -m 8 -o /dev/null -w "loki=%{http_code}\n" "http://${LP}:3100/ready"
echo "==== LOKI LABELS ===="
sleep 5
curl -sS -m 10 "http://${LP}:3100/loki/api/v1/labels" | head -c 500; echo
echo "==== QUERY BACKEND LOGS ===="
curl -sS -m 15 -G "http://${LP}:3100/loki/api/v1/query_range" \
  --data-urlencode 'query={namespace="sla-monitoring"}' \
  --data-urlencode "limit=5" \
  --data-urlencode "start=$(date -u -d '15 minutes ago' +%s)000000000" \
  --data-urlencode "end=$(date -u +%s)000000000" | python3 -c 'import sys,json; d=json.load(sys.stdin); print("status", d.get("status")); print("streams", len(d.get("data",{}).get("result",[])))'
echo "==== GRAFANA DATASOURCES ===="
curl -sS -m 8 -u admin:admin --header "Host: grafana.sla-monitoring.local" \
  "http://127.0.0.1:8000/api/datasources" | python3 -c 'import sys,json; ds=json.load(sys.stdin); [print(d["name"], d["type"], d.get("url")) for d in ds]'
