#!/bin/bash
set -e
GPOD=$(sudo kubectl get pods -n sla-monitoring -l app=grafana -o jsonpath='{.items[0].metadata.name}')
echo "=== datasource.yml in pod ==="
sudo kubectl exec -n sla-monitoring "$GPOD" -- cat /etc/grafana/provisioning/datasources/datasource.yml

echo
echo "=== Grafana -> Prometheus ==="
sudo kubectl exec -n sla-monitoring "$GPOD" -- wget -qO- --timeout=5 http://10.0.0.247:9090/api/v1/query?query=up
echo

echo
echo "=== Grafana API datasources ==="
curl -sS -m 10 -u admin:admin -H 'Host: grafana.sla-monitoring.local' http://127.0.0.1/api/datasources | python3 -c 'import sys,json
for d in json.load(sys.stdin):
  print(d["name"], d["url"])'

echo
echo "=== Proxy query up ==="
curl -sS -m 15 -u admin:admin -H 'Host: grafana.sla-monitoring.local' \
  'http://127.0.0.1/api/datasources/proxy/uid/prometheus/api/v1/query?query=up' | python3 -c 'import sys,json;d=json.load(sys.stdin);print(d["status"], len(d["data"]["result"]), [x["metric"].get("job") for x in d["data"]["result"]])'

echo
echo "=== Proxy query jvm ==="
curl -sS -m 15 -u admin:admin -H 'Host: grafana.sla-monitoring.local' \
  'http://127.0.0.1/api/datasources/proxy/uid/prometheus/api/v1/query?query=jvm_memory_used_bytes' | python3 -c 'import sys,json;d=json.load(sys.stdin);print("jvm", len(d["data"]["result"]))'
