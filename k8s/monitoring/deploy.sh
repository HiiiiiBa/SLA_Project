#!/bin/bash
set -euo pipefail
cd ~/SLA_Project

echo "==== Apply monitoring manifests ===="
sudo kubectl apply -f k8s/monitoring/prometheus.yaml
sudo kubectl apply -f k8s/monitoring/grafana.yaml
sudo kubectl apply -f k8s/monitoring/ingressroute.yaml
sudo kubectl apply -f k8s/traefik/ingressroute-backend.yaml

echo "==== Rebuild backend with Prometheus metrics ===="
cd ~/SLA_Project/backend
sudo docker build -t hiiiba698/sla-backend:latest .
sudo docker save hiiiba698/sla-backend:latest | sudo k3s ctr images import -
sudo kubectl set image deployment/backend backend=hiiiba698/sla-backend:latest -n sla-monitoring
sudo kubectl patch deploy backend -n sla-monitoring -p '{"spec":{"template":{"spec":{"containers":[{"name":"backend","imagePullPolicy":"IfNotPresent"}]}}}}'
sudo kubectl rollout restart deploy/backend -n sla-monitoring
sudo kubectl rollout status deploy/backend -n sla-monitoring --timeout=240s
sudo kubectl rollout status deploy/prometheus -n sla-monitoring --timeout=120s
sudo kubectl rollout status deploy/grafana -n sla-monitoring --timeout=120s

echo "==== Tests ===="
sleep 5
curl -sS -m 8 -o /dev/null -w "prometheus_ui=%{http_code}\n" -H "Host: prometheus.sla-monitoring.local" http://127.0.0.1:8000/-/ready
curl -sS -m 8 -o /dev/null -w "grafana=%{http_code}\n" -H "Host: grafana.sla-monitoring.local" http://127.0.0.1:8000/api/health
curl -sS -m 8 -o /tmp/prom.txt -w "actuator=%{http_code}\n" -H "Host: sla-monitoring.local" http://127.0.0.1:8000/actuator/prometheus
head -c 200 /tmp/prom.txt; echo
sudo kubectl get pods -n sla-monitoring -l 'app in (prometheus,grafana,backend)'
