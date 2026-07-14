#!/bin/bash
# Rebuild frontend en same-origin (API relative) pour login depuis n'importe quel PC via l'IP.
set -e
cd ~/SLA_Project

echo "=== URLs AVANT ==="
POD=$(sudo kubectl get pods -n sla-monitoring -l app=frontend -o jsonpath='{.items[0].metadata.name}')
sudo kubectl exec -n sla-monitoring "$POD" -- sh -c \
  'grep -Rho "sla-monitoring.local\|84.8.216.210\|sslip.io\|localhost:8080" /app/.next/static 2>/dev/null | sort | uniq -c' || true

echo "=== Build same-origin (API/WS vides) ==="
sudo docker build --no-cache \
  --build-arg NEXT_PUBLIC_API_URL= \
  --build-arg NEXT_PUBLIC_WS_URL= \
  -t hiiiba698/sla-frontend:latest \
  ./frontend

sudo docker save hiiiba698/sla-frontend:latest | sudo k3s ctr images import -

TS=$(date +%s)
sudo kubectl patch deploy frontend -n sla-monitoring --type strategic -p \
  "{\"spec\":{\"template\":{\"metadata\":{\"annotations\":{\"rebuild\":\"$TS\"}},\"spec\":{\"containers\":[{\"name\":\"frontend\",\"image\":\"hiiiba698/sla-frontend:latest\",\"imagePullPolicy\":\"Never\"}]}}}}"

sudo kubectl rollout status deployment/frontend -n sla-monitoring --timeout=180s

POD=$(sudo kubectl get pods -n sla-monitoring -l app=frontend -o jsonpath='{.items[0].metadata.name}')
echo "NEW_POD=$POD"
echo "=== URLs APRES (doit etre vide / sans .local) ==="
sudo kubectl exec -n sla-monitoring "$POD" -- sh -c \
  'grep -Rho "sla-monitoring.local\|84.8.216.210\|sslip.io\|localhost:8080" /app/.next/static 2>/dev/null | sort | uniq -c' \
  || echo "OK: aucune URL absolue trouvee"

sudo kubectl set env deploy/backend -n sla-monitoring \
  CORS_ALLOWED_ORIGINS='http://84.8.216.210,http://sla-monitoring.local,https://sla-monitoring.local'

echo "=== Login API ==="
curl -sS -m 15 -X POST http://84.8.216.210/api/auth/login \
  -H 'Content-Type: application/json' \
  -H 'Origin: http://84.8.216.210' \
  -d '{"email":"admin@sla.com","password":"Admin123!"}' | head -c 200
echo
echo DONE
