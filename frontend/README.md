# SLA Monitoring — Frontend

Interface Next.js pour le système de gestion des SLA.

## Stack

- Next.js 16 (App Router)
- React 19 + TypeScript
- Tailwind CSS 4
- Recharts (graphiques)
- STOMP + SockJS (alertes temps réel)

## Démarrage

1. Copier la configuration :

```bash
cp .env.local.example .env.local
```

2. Installer les dépendances :

```bash
npm install
```

3. Démarrer le backend Spring Boot sur `http://localhost:8080`

4. Lancer le frontend :

```bash
npm run dev
```

Ouvrir [http://localhost:3000](http://localhost:3000)

## Comptes démo

| Email | Mot de passe | Rôle |
|-------|--------------|------|
| admin@sla.com | Admin123! | ADMIN |
| manager@sla.com | Manager123! | MANAGER (Sophie — Acme, TechStart) |
| manager2@sla.com | Manager123! | MANAGER (Ahmed — Global Retail, FinServ) |
| employee1@sla.com | Employee123! | EMPLOYEE |
| client@acme.com | Client123! | CLIENT |

## Docker (stack complète)

Depuis la racine du projet :

```bash
cp .env.example .env
docker compose up --build
```

Voir le [README principal](../README.md) pour le détail (MailHog, ports, variables).

## Pages

| Route | Description |
|-------|-------------|
| `/login` | Connexion JWT |
| `/dashboard` | Vue d'ensemble + graphiques |
| `/slas` | Liste des contrats SLA |
| `/alerts` | Alertes + WebSocket temps réel |
| `/reports` | Rapports + export PDF/CSV |
| `/clients` | Clients (admin) + CRUD |
| `/services` | Services monitorés + CRUD (admin) |
| `/admin` | Administration (users, moteur SLA, simulation) |
| `/incidents` | Incidents + CRUD (admin) |
| `/slas/[id]` | Détail SLA + graphiques métriques |

## Variables d'environnement

```
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws
```
