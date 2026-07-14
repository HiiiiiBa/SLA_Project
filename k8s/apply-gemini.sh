#!/bin/bash
set -euo pipefail

KEY_FILE=/tmp/gemini_key.txt
if [ ! -f "$KEY_FILE" ]; then
  echo "Missing $KEY_FILE"
  exit 1
fi
KEY=$(cat "$KEY_FILE")
rm -f "$KEY_FILE"

JWT=$(sudo kubectl get secret backend-secret -n sla-monitoring -o jsonpath='{.data.JWT_SECRET}' | base64 -d)

sudo kubectl create secret generic backend-secret -n sla-monitoring \
  --from-literal=JWT_SECRET="$JWT" \
  --from-literal=GEMINI_API_KEY="$KEY" \
  --dry-run=client -o yaml | sudo kubectl apply -f -

# Avoid duplicate env entries
sudo kubectl set env deploy/backend -n sla-monitoring GEMINI_API_KEY- || true
sudo kubectl set env deploy/backend -n sla-monitoring --from=secret/backend-secret --keys=GEMINI_API_KEY
sudo kubectl set env deploy/backend -n sla-monitoring GEMINI_ENABLED=true GEMINI_MODEL=gemini-2.5-flash

sudo kubectl rollout restart deploy/backend -n sla-monitoring
sudo kubectl rollout status deploy/backend -n sla-monitoring --timeout=180s

sudo kubectl exec -n sla-monitoring deploy/backend -- sh -c \
  'if [ -n "$GEMINI_API_KEY" ]; then echo GEMINI_OK; else echo GEMINI_MISSING; fi; echo ENABLED=$GEMINI_ENABLED; echo MODEL=$GEMINI_MODEL'
