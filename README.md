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
| manager@sla.com | Manager123! | MANAGER (Sophie — Acme, TechStart) |
| manager2@sla.com | Manager123! | MANAGER (Ahmed — Global Retail, FinServ) |
| employee1@sla.com | Employee123! | EMPLOYEE |
| employee2@sla.com | Employee123! | EMPLOYEE |
| client@acme.com | Client123! | CLIENT |

Les données démo (client Acme, équipes Team Dev / Team Réseaux, projets, SLA, services) sont créées automatiquement au premier démarrage.

### Organisation (projets & équipes)

- **Client** → plusieurs **projets**
- **Équipe** (ex. Team Dev, Team Réseaux) → gérée par un **manager**, composée d’**employés**
- **Projet** → lié à un client, une équipe et des employés assignés
- **Incidents** → peuvent être associés à un projet

### Vue employé (EMPLOYEE)

Chaque employé ne voit que **ses projets assignés** et le **SLA lié à chaque projet** :

| Employé | Projets | SLA visibles |
|---------|---------|--------------|
| employee1@sla.com | Portail Acme, Core API TechStart | Production API SLA, Core API SLA |
| employee2@sla.com | Core API TechStart, Réseau Retail EU | Core API SLA, Boutique en ligne SLA |

Le projet **Core API TechStart** est partagé (les deux employés y travaillent) — seul ce projet/SLA est commun.

Le projet **Core API TechStart** est partagé (les deux employés y travaillent) — seul ce projet/SLA est commun.

## Rôles et permissions

### Administrateur (ADMIN)

Accès complet à la plateforme.

| Domaine | Droits |
|---------|--------|
| Dashboard | Vue **globale** (tous les clients) |
| Utilisateurs | CRUD via **Administration** |
| Clients / projets / équipes | Gestion complète |
| SLA | Créer, modifier, archiver, supprimer |
| Métriques, alertes, incidents | Consultation globale ; créer des incidents |
| Rapports | Générer, consulter, télécharger PDF/CSV |

### Manager (MANAGER)

Périmètre limité aux clients affectés (`client_managers`).

| Domaine | Droits |
|---------|--------|
| Dashboard | Vue **projets** (pas le dashboard global admin) |
| Clients | Consultation de ses clients ; gestion projets/SLA |
| SLA | Créer, modifier, archiver (pas supprimer) |
| Incidents | Créer (dans son périmètre) |
| Rapports | Générer et télécharger PDF/CSV |
| Interdit | Gestion utilisateurs, clients des autres managers |

### Client (CLIENT)

Lecture seule sur son organisation (compte lié à l’email client, ex. `client@acme.com`).

| Domaine | Droits |
|---------|--------|
| Dashboard | Ses projets et SLA |
| SLA, métriques, alertes, incidents | Consultation |
| Rapports | Télécharger PDF/CSV uniquement |
| Interdit | Gestion, création d’incidents, génération de rapports |

### Employé (EMPLOYEE)

Périmètre = projets assignés.

| Domaine | Droits |
|---------|--------|
| Dashboard | Projets assignés |
| SLA, métriques, alertes, incidents | Consultation filtrée |
| Incidents | **Créer** sur ses projets/SLA |
| Interdit | Gestion, rapports (pas de PDF/CSV) |

### Vue manager (MANAGER) — démo

Un manager ne voit que **les clients qui lui sont affectés** (relation many-to-many `client_managers`).

| Manager | Clients affectés (démo) |
|---------|-------------------------|
| manager@sla.com (Sophie Martin) | Acme Corp, TechStart SA |
| manager2@sla.com (Ahmed Karim) | Global Retail Ltd, FinServ Partners |

**Peut :**
- Voir ses clients et leur portefeuille (projets, SLA)
- Créer / modifier des projets pour ses clients
- Affecter des employés aux projets et gérer ses équipes
- Créer et modifier des SLA (pas de suppression)
- Consulter incidents, alertes, dashboard et rapports
- Télécharger les rapports PDF / CSV

**Ne peut pas :**
- Créer des administrateurs (page Administration réservée à ADMIN)
- Voir les clients des autres managers (ex. FinServ non assigné au manager démo)
- Supprimer des utilisateurs ou des clients

## Parcours de test

1. Ouvrir http://localhost:3000
2. Se connecter avec `admin@sla.com` / `Admin123!`
3. **Administration** → Simuler (`DEGRADED`) → Évaluer les SLA
4. Consulter dashboard, alertes, rapports
5. Se connecter avec `manager@sla.com` puis `manager2@sla.com` (mot de passe `Manager123!`) — comparer les clients visibles dans **Clients**
6. Vérifier les emails sur http://localhost:8025

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

## Dépannage Docker / réseau

### `auth.docker.io: no such host` ou `repo.maven.apache.org: Try again`

Le réseau ou le DNS bloque les téléchargements **depuis l'intérieur des conteneurs** (Docker Hub, Maven Central).

**Option 1 — Démarrer sans rebuild** (si les images existent déjà) :

```bash
docker compose up -d
```

**Option 2 — Builder le backend sur l'hôte** (recommandé si Maven fonctionne en local) :

```bash
cd backend
mvn package -DskipTests
cd ..
docker compose -f docker-compose.yml -f docker-compose.local-build.yml up --build -d
```

**Option 3 — Corriger le réseau Docker Desktop** : Settings → Docker Engine → ajouter `"dns": ["8.8.8.8", "1.1.1.1"]`, redémarrer Docker, puis relancer `docker compose up --build -d`.
