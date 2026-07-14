#!/usr/bin/env bash
# Genere un certificat auto-signe et le secret K8s sla-monitoring-tls.
set -euo pipefail

NS="${NS:-sla-monitoring}"
SECRET="${SECRET:-sla-monitoring-tls}"
DIR="${TMPDIR:-/tmp}/sla-tls-$$"
mkdir -p "$DIR"
trap 'rm -rf "$DIR"' EXIT

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$DIR/tls.key" \
  -out "$DIR/tls.crt" \
  -subj "/O=SLA Monitoring/CN=sla.84.8.216.210.sslip.io" \
  -addext "subjectAltName=DNS:sla-monitoring.local,DNS:grafana.sla-monitoring.local,DNS:prometheus.sla-monitoring.local,DNS:sla.84.8.216.210.sslip.io,DNS:grafana.84.8.216.210.sslip.io,DNS:prometheus.84.8.216.210.sslip.io,IP:84.8.216.210"

kubectl create secret tls "$SECRET" \
  --cert="$DIR/tls.crt" \
  --key="$DIR/tls.key" \
  -n "$NS" \
  --dry-run=client -o yaml | kubectl apply -f -

echo "Secret $SECRET pret dans $NS"
kubectl get secret "$SECRET" -n "$NS"
