#!/bin/bash
set -euo pipefail

POD=$(sudo kubectl -n kube-system get pod -l app.kubernetes.io/name=traefik -o jsonpath='{.items[0].metadata.name}')
echo "POD=$POD"

echo "==== ENV / DNS POLICY ===="
sudo kubectl -n kube-system get pod "$POD" -o jsonpath='dnsPolicy={.spec.dnsPolicy} hostNetwork={.spec.hostNetwork}{"\n"}'
sudo kubectl -n kube-system exec "$POD" -- printenv | grep -E 'KUBERNETES|NAMESPACE' || true

echo "==== API REACHABILITY FROM TRAEFIK POD ===="
sudo kubectl -n kube-system exec "$POD" -- sh -c 'wget -qO- --timeout=5 http://127.0.0.1:8080/api/http/routers | head -c 200' || true
echo
# Use SA token inside pod
sudo kubectl -n kube-system exec "$POD" -- sh -c '
set -e
TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)
HOST=$KUBERNETES_SERVICE_HOST
PORT=$KUBERNETES_SERVICE_PORT
echo "API=$HOST:$PORT"
# busybox wget may not support https well; try with wget --no-check-certificate
wget -qO- --timeout=8 --no-check-certificate \
  --header="Authorization: Bearer $TOKEN" \
  https://$HOST:$PORT/apis/traefik.io/v1alpha1/ingressroutes 2>&1 | head -c 500
echo
wget -qO- --timeout=8 --no-check-certificate \
  --header="Authorization: Bearer $TOKEN" \
  https://$HOST:$PORT/api/v1/namespaces 2>&1 | head -c 300
echo
'

echo "==== ALL TRAEFIK LOG LINES (provider related, full) ===="
sudo kubectl -n kube-system logs "$POD" | grep -n -iE 'kubernetes|crd|error|fail|sync|inform|forbid|timeout|dial|connect' | head -200

echo "==== NODEPORT DIRECT TEST ===="
curl -sS -o /tmp/np.html -w "nodeport=%{http_code}\n" http://127.0.0.1:32088/ || true
head -c 150 /tmp/np.html; echo
