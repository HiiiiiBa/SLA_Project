# SonarQube / SonarCloud — configuration exacte

## Est-ce qu’il faut « importer depuis GitHub » ?

**Oui, une fois** — pour créer le projet. Ensuite c’est la **CI GitHub Actions** qui envoie l’analyse (pas besoin de réimporter à chaque fois).

### Étapes SonarCloud (à vérifier maintenant)

1. Allez sur [https://sonarcloud.io](https://sonarcloud.io) → connecté avec **GitHub**
2. **Projects** → vous devez voir **SLA_Project** (ou similaire)
3. Ouvrez le projet → icône engrenage / **Project Information**
4. Copiez **exactement** :

| Champ SonarCloud | Variable GitHub |
|------------------|-----------------|
| **Project Key** (ex. `HiiiiiBa_SLA_Project`) | `SONAR_PROJECT_KEY` |
| **Organization Key** (ex. `hiiiiiba` en minuscules) | `SONAR_ORGANIZATION` |

5. GitHub → repo → **Settings → Secrets and variables → Actions → Variables**
6. Vérifiez que les valeurs sont **identiques caractère par caractère** (majuscules/minuscules)
7. Secret `SONAR_TOKEN` = token créé dans SonarCloud → *My Account → Security*
8. Variable `SONAR_ENABLED` = `true`

### Si le projet n’existe pas encore

1. SonarCloud → **➕ → Analyze new project**
2. Choisissez le dépôt GitHub **SLA_Project**
3. **Set up** / Create
4. Choisissez **With GitHub Actions** (pas besoin de suivre leur snippet : le nôtre est déjà dans `.github/workflows/ci.yml`)
5. Recopiez Project Key + Organization dans les variables GitHub

### Quality Gate trop stricte (souvent la cause du rouge)

1. SonarCloud → **Quality Gates**
2. Create / Copy « Sonar way » → nommez `SLA-stage`
3. Condition **Coverage on New Code** → **0%**
4. Projet → **Project Settings → Quality Gate** → choisissez `SLA-stage`

Dans la CI actuelle, `sonar.qualitygate.wait=false` : l’analyse est **envoyée** même si la gate est rouge (le rapport apparaît sur SonarCloud). Vous pourrez remettre `wait=true` plus tard.

## Trivy

Le scan tourne et **affiche** les CVE, sans faire échouer le pipeline (images de base Java/Node ont souvent des CRITICAL non liés à votre code).
