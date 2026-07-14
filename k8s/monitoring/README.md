# Monitoring Prometheus / Grafana

## Docker Compose (local)

```bash
docker compose up -d prometheus grafana
```

| Service | URL | Credentials |
|---------|-----|-------------|
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3001 | `admin` / `admin` |

Dashboard provisionné : **SLA → SLA Monitoring Overview**

## Kubernetes (prod)

```bash
kubectl apply -f k8s/monitoring/
```

| Service | URL | Credentials |
|---------|-----|-------------|
| Grafana | http://grafana.sla-monitoring.local | `admin` / `admin` |
| Prometheus | http://prometheus.sla-monitoring.local | — |

Ajouter dans le fichier hosts :

```
84.8.216.210  grafana.sla-monitoring.local prometheus.sla-monitoring.local
```

## Backend

- Endpoint Prometheus : `GET /actuator/prometheus`
- Health : `GET /actuator/health`
- Tags Micrometer : `application=sla-monitoring`

Cibles scrapées :
- Backend Spring Boot (`/actuator/prometheus`)
- Traefik (port métriques `9100`, K8s uniquement)
