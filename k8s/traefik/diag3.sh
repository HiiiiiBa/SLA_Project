#!/bin/bash
set -euo pipefail

echo "==== CURRENT STATE ===="
sudo kubectl -n kube-system get pods -l app.kubernetes.io/name=traefik -o wide
sudo kubectl get ingressroute,ingress -n sla-monitoring
curl -sS http://127.0.0.1:8080/api/http/routers | python3 -c 'import sys,json; d=json.load(sys.stdin); print(len(d),"routers"); [print(r["name"], r.get("rule"), r.get("provider")) for r in d]'

echo "==== FULL DEBUG: kubernetes providers ===="
sudo kubectl -n kube-system logs deploy/traefik --tail=500 > /tmp/traefik-debug.log
grep -iE 'kubernetes|crd|ingress|error|forbid|unauthorized|sla-monitoring|Configuration received' /tmp/traefik-debug.log | head -150

echo "==== SA TOKEN / API ACCESS TEST ===="
# Test if traefik SA can list ingressroutes via API
TOKEN=$(sudo kubectl -n kube-system create token traefik --duration=10m)
APISERVER=$(sudo kubectl config view --raw -o jsonpath='{.clusters[0].cluster.server}')
# k3s often uses https://127.0.0.1:6443
curl -sk -H "Authorization: Bearer $TOKEN" "$APISERVER/apis/traefik.io/v1alpha1/namespaces/sla-monitoring/ingressroutes" | python3 -m json.tool | head -60
curl -sk -H "Authorization: Bearer $TOKEN" "$APISERVER/apis/networking.k8s.io/v1/namespaces/sla-monitoring/ingresses" | python3 -m json.tool | head -40

echo "==== HELM VALUES / EXTRA CONFIG ===="
sudo kubectl -n kube-system get cm -l app.kubernetes.io/name=traefik -o name
sudo helm get values traefik -n kube-system 2>/dev/null || sudo k3s kubectl -n kube-system get secret -l owner=helm,name=traefik -o name 2>/dev/null || true
ls /var/lib/rancher/k3s/server/manifests/ 2>/dev/null | head
sudo cat /var/lib/rancher/k3s/server/manifests/traefik*.yaml 2>/dev/null | head -200 || true
