# Monitoring Prometheus / Grafana / Loki

## Docker Compose (local)

```bash
docker compose up -d prometheus loki promtail grafana
```

| Service | URL | Credentials |
|---------|-----|-------------|
| Prometheus | http://localhost:9090 | — |
| Loki | http://localhost:3100 | — |
| Grafana | http://localhost:3001 | `admin` / `admin` |

Dashboards provisionnés :
- **SLA → SLA Monitoring Overview** (métriques)
- **SLA → SLA Application Logs** (Loki)

## Kubernetes (prod)

```bash
kubectl apply -f k8s/monitoring/prometheus.yaml
kubectl apply -f k8s/monitoring/loki.yaml
kubectl apply -f k8s/monitoring/promtail.yaml
kubectl apply -f k8s/monitoring/grafana.yaml
kubectl apply -f k8s/monitoring/ingressroute.yaml
```

| Service | URL | Credentials |
|---------|-----|-------------|
| Grafana | http://grafana.sla-monitoring.local | `admin` / `admin` |
| Prometheus | http://prometheus.sla-monitoring.local | — |

Hosts :

```
84.8.216.210  grafana.sla-monitoring.local prometheus.sla-monitoring.local
```

### Explorer les logs dans Grafana

1. Ouvrir Grafana → **Explore** → datasource **Loki**
2. Requête exemple : `{namespace="sla-monitoring", app="backend"}`
3. Ou dashboard **SLA Application Logs**

## Backend

- Endpoint Prometheus : `GET /actuator/prometheus`
- Health : `GET /actuator/health`
- Tags Micrometer : `application=sla-monitoring`

Cibles scrapées :
- Backend Spring Boot (`/actuator/prometheus`)
- Traefik (port métriques `9100`)
- Logs pods via Promtail → Loki
