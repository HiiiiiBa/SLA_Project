# DevSecOps — Sécurité (étape 9)

| Élément | Rôle |
|---------|------|
| **Trivy** (CI/CD) | Scan des images Docker avant push / merge |
| **TLS Traefik** | HTTPS via secret `sla-monitoring-tls` (openssl ou cert-manager) |
| **Secrets K8s** | `JWT_SECRET`, `GEMINI_API_KEY` hors Git |
| **NetworkPolicy** | Placeholder d’isolation réseau (optionnel) |

## 1. Trivy (automatique)

Dans `.github/workflows/ci.yml` et `cd.yml` :

- scan des images backend / frontend
- échec si vulnérabilité **CRITICAL** non corrigée

Localement :

```bash
trivy image hiiiba698/sla-backend:latest
trivy image hiiiba698/sla-frontend:latest
```

## 2. HTTPS

### Recommandé sur ce cluster K3s : openssl

Les pods peinent parfois à joindre l’API (`10.43.0.1:443`) → le webhook **cert-manager** peut rester NotReady. Un secret TLS openssl contourne le problème :

```bash
bash k8s/security/generate-tls-secret.sh
sudo kubectl apply -f k8s/traefik/ingressroute-frontend.yaml
sudo kubectl apply -f k8s/traefik/ingressroute-backend.yaml
sudo kubectl apply -f k8s/monitoring/ingressroute.yaml
```

URLs (accepter l’avertissement navigateur — cert auto-signé) :

- https://sla-monitoring.local
- https://grafana.sla-monitoring.local
- https://prometheus.sla-monitoring.local

HTTP reste disponible. Redirect forcé via middleware `redirect-https` (optionnel).

### Optionnel : cert-manager

```bash
# Télécharger puis appliquer (évite kubectl apply -f URL qui peut bloquer)
curl -fsSL -o /tmp/cert-manager.yaml \
  https://github.com/cert-manager/cert-manager/releases/download/v1.16.2/cert-manager.yaml
sudo kubectl apply -f /tmp/cert-manager.yaml
# Attendre que webhook soit Ready, puis :
sudo kubectl apply -f k8s/security/cluster-issuer.yaml
sudo kubectl apply -f k8s/security/certificate.yaml
```

Si le webhook reste en erreur API timeout, rester sur `generate-tls-secret.sh`.

## 3. Secrets applicatifs

**Ne jamais** committer de vraies clés.

```bash
export JWT_SECRET='...'
export GEMINI_API_KEY='...'
bash k8s/security/create-backend-secret.sh
```

Template : `backend-secret.example.yaml`.

## 4. NetworkPolicy (optionnel)

```bash
sudo kubectl apply -f k8s/security/network-policy.yaml
```
