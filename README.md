# SLA Monitoring System

Système intelligent de gestion des SLA — backend Spring Boot + frontend Next.js.

## Architecture Docker

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | Interface Next.js |
| Backend API | http://localhost:8080 | REST + WebSocket |
| Swagger | http://localhost:8080/swagger-ui.html | Documentation API |
| PostgreSQL | *(interne Docker)* | Base `sla_monitoring` — non exposée sur l'hôte par défaut |
| MailHog UI | http://localhost:8025 | Emails de test capturés |

## Démarrage rapide

### Prérequis

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Docker Compose v2)

### Lancer toute la stack

```bash
# À la racine du projet
cp .env.example .env
docker compose up --build
```

Premier démarrage : le build Maven + Next.js peut prendre **5 à 10 minutes**.

Arrêter :

```bash
docker compose down
```

Supprimer aussi les données PostgreSQL :

```bash
docker compose down -v
```

## Comptes démo

| Email | Mot de passe | Rôle |
|-------|--------------|------|
| admin@sla.com | Admin123! | ADMIN |
| user@sla.com | User123! | USER |
| client@acme.com | Client123! | CLIENT |

Les données démo (client Acme, SLA, 3 services) sont créées automatiquement au premier démarrage.

## Parcours de test

1. Ouvrir http://localhost:3000
2. Se connecter avec `admin@sla.com` / `Admin123!`
3. **Administration** → Simuler (`DEGRADED`) → Évaluer les SLA
4. Consulter dashboard, alertes, rapports
5. Vérifier les emails sur http://localhost:8025

## Développement local (sans Docker frontend/backend)

PostgreSQL et MailHog peuvent tourner seuls dans Docker :

```bash
docker compose up postgres mailhog -d
```

Puis lancer manuellement :

```bash
# Backend
cd backend && mvn spring-boot:run

# Frontend
cd frontend && npm run dev
```

## Variables d'environnement

Copier `.env.example` vers `.env` à la racine. Les principales :

| Variable | Défaut | Description |
|----------|--------|-------------|
| `POSTGRES_PORT` | 5433 | Port PostgreSQL sur l'hôte |
| `BACKEND_PORT` | 8080 | Port API |
| `FRONTEND_PORT` | 3000 | Port frontend |
| `NEXT_PUBLIC_API_URL` | http://localhost:8080 | URL API vue par le navigateur |
| `JWT_SECRET` | (voir .env.example) | Secret JWT — **à changer en production** |

## Structure du projet

```
SLA_Project/
├── backend/          # Spring Boot 3 / Java 21
├── frontend/         # Next.js 16 / TypeScript
├── docker-compose.yml
├── .env.example
└── README.md
```

## Commandes utiles

```bash
# Logs en direct
docker compose logs -f backend

# Rebuild uniquement le backend
docker compose up --build backend -d

# État des services
docker compose ps
```

## Notes

- Si le port **8080** ou **3000** est déjà utilisé, arrêtez les processus locaux (`mvn spring-boot:run`, `npm run dev`) ou modifiez les ports dans `.env`.
- Un ancien conteneur **`sla-postgres`** sur le port 5433 n'empêche plus le démarrage : PostgreSQL Compose reste sur le réseau interne Docker. Pour l'exposer sur l'hôte, copiez `docker-compose.override.example.yml` vers `docker-compose.override.yml` et choisissez un port libre (ex. **5434**).
- Les URLs `NEXT_PUBLIC_*` sont injectées **au build** du frontend : après modification, rebuild avec `docker compose up --build frontend`.
