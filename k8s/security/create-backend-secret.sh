#!/usr/bin/env bash
# Crée / met à jour backend-secret sans stocker de secrets dans Git.
set -euo pipefail

NS="${NS:-sla-monitoring}"

if [[ -z "${JWT_SECRET:-}" ]]; then
  echo "JWT_SECRET is required (export JWT_SECRET=...)"
  exit 1
fi

ARGS=(--from-literal=JWT_SECRET="$JWT_SECRET")
if [[ -n "${GEMINI_API_KEY:-}" ]]; then
  ARGS+=(--from-literal=GEMINI_API_KEY="$GEMINI_API_KEY")
fi

kubectl create secret generic backend-secret -n "$NS" "${ARGS[@]}" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Secret backend-secret applied in namespace $NS"

# Brancher le déploiement backend sur le secret (clés présentes)
if kubectl get deploy backend -n "$NS" >/dev/null 2>&1; then
  kubectl set env deploy/backend -n "$NS" --from=secret/backend-secret
  if [[ -n "${GEMINI_API_KEY:-}" ]]; then
    kubectl set env deploy/backend -n "$NS" GEMINI_ENABLED=true GEMINI_MODEL=gemini-2.5-flash
  fi
  echo "Deployment backend env updated from secret"
fi
