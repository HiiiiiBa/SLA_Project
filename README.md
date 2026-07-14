# SLA Monitoring System

Système intelligent de gestion des SLA — backend Spring Boot + frontend Next.js.

## Architecture Docker

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | Interface Next.js |
| Backend API | http://localhost:8080 | REST + WebSocket |
| Swagger | http://localhost:8080/swagger-ui.html | Documentation API |
| Prometheus | http://localhost:9090 | Métriques scrapées |
| Loki | http://localhost:3100 | Agrégation de logs |
| Grafana | http://localhost:3001 | Dashboards (`admin` / `admin`) — métriques + logs |
| PostgreSQL | *(interne Docker)* | Base `sla_monitoring` — non exposée sur l'hôte par défaut |
| MailHog UI | http://localhost:8025 | Emails de test capturés |

## Infra as Code (DevOps)

Voir [`infra/README.md`](infra/README.md) :

- **Terraform** — réseau OCI (VCN, Security List 22/80/443)
- **Ansible** — configuration / vérif du serveur K3s

## Accès web public (Oracle Cloud)

| URL | Usage |
|-----|--------|
| **http://84.8.216.210** | Accès public (téléphone, autres PC) |
| http://84.8.216.210/login | Page de connexion |
| http://sla-monitoring.local | Uniquement si fichier `hosts` configuré |

Grafana : ouvrir via IP + Host, ou `hosts` local (`grafana.sla-monitoring.local`).

> `sslip.io` est optionnel ; certains réseaux DNS ne le résolvent pas. L’IP publique Oracle suffit.

## DevSecOps (étape 9)

Voir [`k8s/security/README.md`](k8s/security/README.md) :

- **Trivy** — scan d’images Docker dans CI/CD (bloque les CVE CRITICAL)
- **HTTPS** — certificat TLS auto-signé (`sla-monitoring-tls`) + IngressRoutes `websecure`
- **Secrets K8s** — `JWT_SECRET` / `GEMINI_API_KEY` hors du dépôt

## Qualité & tests (CI)

- **Backend** — JUnit + couverture **JaCoCo**
- **Frontend** — ESLint + **Vitest**
- **SonarQube / SonarCloud** — analyse + Quality Gate (voir [`docs/SONARQUBE.md`](docs/SONARQUBE.md))

## Démarrage rapide

### Prérequis

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Docker Compose v2)

### Lancer toute la stack

```bash
# À la racine du projet
cp .env.example .env

# Démarrage quotidien (recommandé) — sans rebuild, pas besoin de Docker Hub
docker compose up -d

# Premier lancement ou après changement de code
docker compose up --build
```

Sous Windows PowerShell, vous pouvez aussi utiliser `.\scripts\docker-up.ps1` (sans rebuild) ou `.\scripts\docker-up.ps1 -Build`.

Premier démarrage avec `--build` : le build Maven + Next.js peut prendre **5 à 10 minutes**.

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
| SLA | Créer, modifier, archiver, supprimer ; gérer les services depuis le détail SLA |
| Métriques, alertes, incidents | Consultation globale ; créer des incidents ; **assigner à un manager** du client |
| Rapports | Générer, consulter, télécharger PDF/CSV |
| Validations | Approuver ou refuser les demandes des managers (notification temps réel) |

### Manager (MANAGER)

Périmètre limité aux clients affectés (`client_managers`).

| Domaine | Droits |
|---------|--------|
| Dashboard | Vue **projets** (pas le dashboard global admin) |
| Clients | Consultation de ses clients ; gestion projets/SLA |
| SLA | Créer (avec services optionnels), modifier ; gérer les services (ajouter, modifier, supprimer) depuis le détail SLA ; **demander** la suppression du SLA |
| Projets / équipes | Créer, modifier ; **demander** la suppression (validation admin) |
| Incidents | Créer, **assigner à un employé**, suivre la résolution (incidents qui lui sont assignés par l’admin) |
| Alertes | Consultation ; **marquer comme lu**, **résoudre** (périmètre clients assignés) |
| Rapports | Générer et télécharger PDF/CSV |
| Notifications | Alertes SLA temps réel + résultat des demandes de validation (cloche) |
| Interdit | Suppression directe de projets/équipes/SLA, gestion utilisateurs, clients des autres managers |

### Client (CLIENT)

Accès limité à son organisation (compte lié à l’email client, ex. `client@acme.com`).

**Pages accessibles :**

| Page | Droits |
|------|--------|
| Tableau de bord | Ses statistiques uniquement (projets et SLA) |
| Projets | Consultation de ses projets |
| SLA (liste et détail) | Consultation ; dans le détail, services couverts et état (UP/DOWN/DEGRADED) en lecture seule |
| Incidents | Création et consultation (pas de modification ni suppression) |
| Alertes | Consultation uniquement |
| Rapports | Téléchargement PDF/CSV |

**Pages interdites :** Administration, Clients, Équipes, Services (pas de création ni modification de services ; pas d’accès à la page de gestion des services).

| Domaine | Droits |
|---------|--------|
| Dashboard | Ses projets et SLA |
| SLA, métriques, alertes | Consultation |
| Incidents | Création et consultation |
| Rapports | Télécharger PDF/CSV uniquement |
| Interdit | Gestion, modification/suppression d’incidents, génération de rapports, pages admin/org |

### Employé (EMPLOYEE)

Périmètre = projets assignés.

**Pages accessibles :**

| Page | Droits |
|------|--------|
| Tableau de bord | Projets et SLA assignés |
| Projets | Consultation |
| SLA (liste et détail) | Consultation ; services associés visibles dans le détail SLA (lecture seule) |
| Incidents | Incidents assignés uniquement ; mise à jour, commentaires, résolution |
| Alertes | Consultation |
| Maintenances | Créer, modifier et annuler sur les SLA de ses projets |

**Pages interdites :** Administration, Clients, Équipes, Services, Rapports.

| Domaine | Droits |
|---------|--------|
| Dashboard | Projets assignés |
| SLA, métriques, alertes | Consultation filtrée |
| Incidents | Mettre à jour la description, commenter, résoudre (**RESOLVED**) — assignation par le manager |
| Maintenances | Créer / modifier / annuler (périmètre projets assignés) |
| Interdit | Création d’incidents, auto-assignation, page Services, gestion SLA/services/projets, administration, rapports |

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

## Assistant IA (Google Gemini)

Deux fonctionnalités sont disponibles pour tous les rôles authentifiés (données filtrées selon le périmètre utilisateur) :

| Fonctionnalité | Accès | Endpoint |
|----------------|-------|----------|
| **Analyse d'incident** | Page Incidents → ouvrir un incident → bouton **Analyser** | `POST /api/incidents/{id}/analyze` |
| **Chatbot flottant** | Bouton en bas à droite sur toutes les pages du dashboard | `POST /api/ai/chat` |

### Configuration

1. Obtenez une clé API sur [Google AI Studio](https://aistudio.google.com/apikey).
2. Ajoutez-la dans `.env` :

```bash
GEMINI_API_KEY=votre_cle_api
GEMINI_MODEL=gemini-2.5-flash
GEMINI_ENABLED=true
```

3. Redémarrez le backend : `docker compose up --build backend -d`

La clé **ne doit jamais** être exposée côté frontend — tous les appels passent par le backend Spring Boot.

## Notes

- Si le port **8080** ou **3000** est déjà utilisé, arrêtez les processus locaux (`mvn spring-boot:run`, `npm run dev`) ou modifiez les ports dans `.env`.
- Un ancien conteneur **`sla-postgres`** sur le port 5433 n'empêche plus le démarrage : PostgreSQL Compose reste sur le réseau interne Docker. Pour l'exposer sur l'hôte, copiez `docker-compose.override.example.yml` vers `docker-compose.override.yml` et choisissez un port libre (ex. **5434**).
- Les URLs `NEXT_PUBLIC_*` sont injectées **au build** du frontend : après modification, rebuild avec `docker compose up --build frontend`.

## Dépannage Docker / réseau

### `registry-1.docker.io: no such host`, `auth.docker.io: no such host` ou `repo.maven.apache.org: Try again`

Le réseau ou le DNS bloque les téléchargements **depuis l'intérieur des conteneurs** (Docker Hub, Maven Central). L'erreur sur `docker/dockerfile:1` en ligne 1 du Dockerfile est le même symptôme.

**Option 1 — Démarrer sans rebuild** (si les images existent déjà) :

```bash
docker compose up -d
```

**Option 2 — Builder sur l'hôte** (recommandé si Maven/npm fonctionnent en local) :

```bash
cd frontend && npm run build
cd ../backend && mvn package -DskipTests
cd ..
docker compose -f docker-compose.yml -f docker-compose.local-build.yml up --build -d
```

Backend seul : omettez `frontend` et `npm run build`, ou ajoutez `backend` à la fin de la commande compose.

**Option 3 — Corriger le réseau Docker Desktop** : Settings → Docker Engine → ajouter `"dns": ["8.8.8.8", "1.1.1.1"]`, redémarrer Docker, puis relancer `docker compose up --build -d`.
