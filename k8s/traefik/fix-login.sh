#!/bin/bash
set -euo pipefail

echo "==== Apply backend IngressRoute ===="
sudo kubectl apply -f /tmp/ingressroute-backend.yaml

echo "==== CORS ===="
sudo kubectl set env deploy/backend -n sla-monitoring \
  CORS_ALLOWED_ORIGINS="http://sla-monitoring.local,http://84.8.216.210"

echo "==== Frontend deploy env ===="
sudo kubectl set env deploy/frontend -n sla-monitoring \
  NEXT_PUBLIC_API_URL="http://sla-monitoring.local" \
  NEXT_PUBLIC_WS_URL="http://sla-monitoring.local/ws"

echo "==== Rebuild frontend on server ===="
cd "$HOME/SLA_Project/frontend"
sudo docker build \
  --build-arg NEXT_PUBLIC_API_URL=http://sla-monitoring.local \
  --build-arg NEXT_PUBLIC_WS_URL=http://sla-monitoring.local/ws \
  -t hiiiba698/sla-frontend:latest .

echo "==== Import into k3s ===="
sudo docker save hiiiba698/sla-frontend:latest | sudo k3s ctr images import -

echo "==== Restart frontend ===="
# Force pull from local import: set imagePullPolicy IfNotPresent temporarily or delete pods
sudo kubectl patch deploy frontend -n sla-monitoring -p '{"spec":{"template":{"spec":{"containers":[{"name":"frontend","imagePullPolicy":"IfNotPresent"}]}}}}'
sudo kubectl rollout restart deploy/frontend -n sla-monitoring
sudo kubectl rollout status deploy/frontend -n sla-monitoring --timeout=180s
sudo kubectl rollout status deploy/backend -n sla-monitoring --timeout=120s

echo "==== Test API via Traefik ===="
sleep 2
curl -sS -m 8 -w "\nhttp=%{http_code}\n" \
  -H "Host: sla-monitoring.local" \
  -H "Content-Type: application/json" \
  -X POST "http://127.0.0.1:8000/api/auth/login" \
  -d '{"email":"admin@example.com","password":"wrong"}' | head -c 400
echo
sudo kubectl get ingressroute -n sla-monitoring
