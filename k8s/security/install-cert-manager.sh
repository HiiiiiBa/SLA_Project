#!/usr/bin/env bash
set -euo pipefail

VERSION="${CERT_MANAGER_VERSION:-v1.16.2}"

echo "Installing cert-manager $VERSION ..."
kubectl apply -f "https://github.com/cert-manager/cert-manager/releases/download/${VERSION}/cert-manager.yaml"

kubectl -n cert-manager rollout status deploy/cert-manager --timeout=180s
kubectl -n cert-manager rollout status deploy/cert-manager-webhook --timeout=180s
kubectl -n cert-manager rollout status deploy/cert-manager-cainjector --timeout=180s

echo "Applying ClusterIssuer + Certificate ..."
kubectl apply -f "$(dirname "$0")/cluster-issuer.yaml"
kubectl apply -f "$(dirname "$0")/certificate.yaml"

echo "Waiting for Certificate Ready ..."
kubectl wait --for=condition=Ready certificate/sla-monitoring-tls -n sla-monitoring --timeout=120s || true
kubectl get certificate,secret -n sla-monitoring | grep -E 'sla-monitoring-tls|NAME' || true

echo "Done. Apply IngressRoutes with TLS (k8s/traefik/, k8s/monitoring/ingressroute.yaml)."
