#!/bin/bash
set -euo pipefail

# Usage: apply-gemini.sh <api_key>
KEY="${1:-}"
if [ -z "$KEY" ]; then
  echo "Missing GEMINI_API_KEY"
  exit 1
fi

# Merge into backend-secret (keep JWT_SECRET)
JWT=$(sudo kubectl get secret backend-secret -n sla-monitoring -o jsonpath='{.data.JWT_SECRET}')
sudo kubectl create secret generic backend-secret -n sla-monitoring \
  --from-literal=JWT_SECRET="$(echo "$JWT" | base64 -d)" \
  --from-literal=GEMINI_API_KEY="$KEY" \
  --dry-run=client -o yaml | sudo kubectl apply -f -

sudo kubectl set env deploy/backend -n sla-monitoring \
  GEMINI_ENABLED=true \
  GEMINI_MODEL=gemini-2.5-flash \
  --containers=backend

sudo kubectl patch deploy backend -n sla-monitoring --type='json' -p='[
  {"op":"add","path":"/spec/template/spec/containers/0/env/-","value":{
    "name":"GEMINI_API_KEY",
    "valueFrom":{"secretKeyRef":{"name":"backend-secret","key":"GEMINI_API_KEY"}}
  }}
]' 2>/dev/null || true

# If GEMINI_API_KEY already exists as plain env, replace with secret ref via set env from secret
sudo kubectl set env deploy/backend -n sla-monitoring \
  GEMINI_API_KEY- 2>/dev/null || true
sudo kubectl set env deploy/backend -n sla-monitoring \
  --from=secret/backend-secret --keys=GEMINI_API_KEY

sudo kubectl set env deploy/backend -n sla-monitoring \
  GEMINI_ENABLED=true \
  GEMINI_MODEL=gemini-2.5-flash

sudo kubectl rollout restart deploy/backend -n sla-monitoring
sudo kubectl rollout status deploy/backend -n sla-monitoring --timeout=180s

echo "==== verify (key present, not printed) ===="
sudo kubectl exec -n sla-monitoring deploy/backend -- sh -c \
  'if [ -n "$GEMINI_API_KEY" ]; then echo GEMINI_API_KEY=SET; else echo GEMINI_API_KEY=MISSING; fi; echo GEMINI_ENABLED=$GEMINI_ENABLED; echo GEMINI_MODEL=$GEMINI_MODEL'
