# SonarQube / SonarCloud — Quality Gate CI

Analyse statique du code + couverture de tests. Si la **Quality Gate** échoue, le job CI `sonarqube` devient rouge.

## Prérequis (SonarCloud — gratuit pour projets étudiants / open source)

1. Créer un compte sur [https://sonarcloud.io](https://sonarcloud.io)
2. **Analyze new project** → importer le dépôt GitHub `SLA_Project`
3. Noter :
   - **Organization Key** (ex. `hiiiba698`)
   - **Project Key** (ex. `sla-monitoring` ou celui proposé)
4. Créer un token : *My Account → Security → Generate Token*

## Secrets & variables GitHub

Dans le dépôt → **Settings → Secrets and variables → Actions** :

| Type | Nom | Valeur |
|------|-----|--------|
| Secret | `SONAR_TOKEN` | token SonarCloud |
| Variable | `SONAR_ENABLED` | `true` |
| Variable | `SONAR_ORGANIZATION` | votre org SonarCloud |
| Variable | `SONAR_PROJECT_KEY` | clé du projet |

Tant que `SONAR_ENABLED` n’est pas `true`, le job Sonar est **ignoré** (les tests CI tournent quand même).

## Localement

```bash
# Backend + JaCoCo
cd backend && ./mvnw clean test
# Rapport : backend/target/site/jacoco/index.html

# Frontend
cd frontend && npm ci && npm run test:coverage
```

Scan Sonar (avec token) :

```bash
export SONAR_TOKEN=...
# Installer sonar-scanner ou utiliser Docker :
docker run --rm \
  -e SONAR_TOKEN \
  -v "$(pwd):/usr/src" \
  sonarsource/sonar-scanner-cli \
  -Dsonar.organization=VOTRE_ORG \
  -Dsonar.projectKey=sla-monitoring
```

## Quality Gate

Par défaut SonarCloud vérifie notamment :

- bugs / vulnérabilités sur le *New Code*
- couverture sur le *New Code*
- code smells

Si la gate est trop stricte au début : dans SonarCloud → **Quality Gates** → assouplir *Coverage on New Code* (ex. 50 %), ou exclure davantage via `sonar.coverage.exclusions` dans `sonar-project.properties`.

## Ce que la CI exécute

1. **backend-tests** — JUnit + JaCoCo  
2. **frontend-tests** — ESLint + Vitest + couverture  
3. **sonarqube** — analyse + **attente Quality Gate** (`sonar.qualitygate.wait=true`)  
4. **docker-security** — build images + Trivy  
