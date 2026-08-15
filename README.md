# The Bridge — API Spring Boot

> Plateforme de formation et gestion d'apprentissage (LMS) — Backend Spring Boot 4.1 / Java 17

[![CI/CD](https://github.com/REPLACE_OWNER/REPLACE_REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/REPLACE_OWNER/REPLACE_REPO/actions/workflows/ci.yml)

---

## Table des matières

1. [Stack technique](#stack-technique)
2. [Prérequis](#prérequis)
3. [Variables d'environnement](#variables-denvironnement)
4. [Installation et lancement local](#installation-et-lancement-local)
5. [Lancement des tests](#lancement-des-tests)
6. [SonarQube](#sonarqube)
7. [Docker](#docker)
8. [Minikube / Kubernetes](#minikube--kubernetes)
9. [Monitoring — Prometheus & Grafana](#monitoring--prometheus--grafana)
10. [GitHub Actions CI/CD](#github-actions-cicd)
11. [Déploiement Render (Production)](#déploiement-render-production)
12. [Gestion des secrets](#gestion-des-secrets)
13. [Architecture CI/CD](#architecture-cicd)
14. [Troubleshooting](#troubleshooting)

---

## Stack technique

| Composant | Technologie |
|---|---|
| Runtime | Java 17 + Spring Boot 4.1.0 |
| Build | Maven (mvnw) |
| Base de données | MySQL 8 (local port 3307) |
| Cache / Rate limiting | Redis + Bucket4j |
| Auth | JWT + OAuth2 (Google, Facebook) |
| Paiements | Stripe |
| Stockage media | Cloudinary |
| Blockchain | Web3j (Polygon Amoy Testnet) |
| WebSocket | Spring WebSocket (STOMP) |
| PDF | Apache PDFBox |
| Monitoring | Spring Boot Actuator + Micrometer + Prometheus + Grafana |
| CI/CD | GitHub Actions |
| Container | Docker (multi-stage, non-root) |
| Orchestration | Kubernetes / Minikube (local) |
| Production | Render |

---

## Prérequis

### Local

| Outil | Version minimale | Commande de vérification |
|---|---|---|
| Java JDK | 17 | `java -version` |
| Maven | (inclus via `mvnw`) | `./mvnw --version` |
| MySQL | 8.0 | `mysql --version` |
| Redis | 7.x | `redis-server --version` |
| Docker | 24.x | `docker --version` |
| Docker Compose | v2 | `docker compose version` |

### CI/CD + Déploiement

| Outil | Usage |
|---|---|
| Compte GitHub | Repository + GitHub Actions + GitHub Secrets |
| Compte SonarCloud | Analyse qualité (gratuit pour projets publics) |
| Compte Render | Hébergement production |
| Minikube | Test Kubernetes local |
| kubectl | CLI Kubernetes |

---

## Variables d'environnement

### 1. Local (fichier `.env`)

```bash
cp .env.example .env
# Éditer .env avec les vraies valeurs
```

> ⚠️ **JAMAIS** commiter `.env`. Il est dans `.gitignore`.

### 2. Variables requises par environnement

| Variable | Local | GitHub Actions | Render |
|---|---|---|---|
| `DATABASE_URL` | `.env` | (optionnel, H2 pour tests) | Render Env Var |
| `DATABASE_USERNAME` | `.env` | — | Render Env Var |
| `DATABASE_PASSWORD` | `.env` | — | Render Env Var |
| `JWT_SECRET` | `.env` | GitHub Secret | Render Env Var |
| `MAIL_USERNAME` | `.env` | — | Render Env Var |
| `MAIL_PASSWORD` | `.env` | — | Render Env Var |
| `CLOUDINARY_*` | `.env` | — | Render Env Var |
| `GOOGLE_CLIENT_*` | `.env` | — | Render Env Var |
| `FACEBOOK_CLIENT_*` | `.env` | — | Render Env Var |
| `STRIPE_*` | `.env` | — | Render Env Var |
| `REDIS_URL` | `.env` | — | Render Env Var |
| `SONAR_TOKEN` | — | GitHub Secret | — |
| `RENDER_API_KEY` | — | GitHub Secret | — |
| `RENDER_SERVICE_ID` | — | GitHub Secret | — |

---

## Installation et lancement local

### 1. Cloner le projet

```bash
git clone https://github.com/REPLACE_OWNER/REPLACE_REPO.git
cd the_bridge
```

### 2. Configurer l'environnement

```bash
cp .env.example .env
# Éditer .env avec vos valeurs réelles
```

### 3. Démarrer MySQL et Redis

```bash
# Avec Docker :
docker run -d --name mysql-bridge -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=yourpassword \
  -e MYSQL_DATABASE=the_bridge \
  mysql:8.0

docker run -d --name redis-bridge -p 6379:6379 redis:7-alpine
```

### 4. Lancer l'application

```bash
./mvnw spring-boot:run
```

L'API sera disponible sur : `http://localhost:8080`  
Swagger UI : `http://localhost:8080/swagger-ui/index.html`  
Health check : `http://localhost:8080/api/health`  
Actuator : `http://localhost:8081/actuator/health`  
Métriques Prometheus : `http://localhost:8081/actuator/prometheus`

---

## Lancement des tests

### Tests unitaires (H2 in-memory — pas de MySQL/Redis requis)

```bash
./mvnw test -Dspring.profiles.active=test
```

### Tests avec rapport de couverture JaCoCo

```bash
./mvnw verify -Dspring.profiles.active=test
# Rapport : target/site/jacoco/index.html
```

### Scan de vulnérabilités OWASP (optionnel)

```bash
./mvnw verify -P security -DskipTests
# Rapport : target/dependency-check-report.html
```

---

## SonarQube

### Prérequis

1. Créer un compte sur [SonarCloud](https://sonarcloud.io)
2. Créer une organisation
3. Générer un token dans `User Settings → Security → Tokens`

### Configuration

Éditer [`sonar-project.properties`](./sonar-project.properties) :

```properties
sonar.projectKey=the-bridge-api
sonar.organization=VOTRE_ORGANISATION_SONARCLOUD
```

### Analyse locale

```bash
./mvnw sonar:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=VOTRE_SONAR_TOKEN \
  -Dspring.profiles.active=test
```

### En CI (GitHub Actions)

La pipeline exécute automatiquement l'analyse et vérifie le Quality Gate.  
Configurer les GitHub Secrets suivants :
- `SONAR_TOKEN`
- `SONAR_HOST_URL` → `https://sonarcloud.io`
- Et la variable d'Actions : `SONAR_ORGANIZATION` → votre org

---

## Docker

### Build de l'image

```bash
# Build simple
docker build -t the-bridge:latest .

# Build avec un tag sha
docker build -t the-bridge:$(git rev-parse --short HEAD) .
```

### Lancer le conteneur

```bash
docker run -d \
  --name the-bridge \
  -p 8080:8080 \
  -p 8081:8081 \
  --env-file .env \
  the-bridge:latest
```

### Vérifications

```bash
# Logs
docker logs -f the-bridge

# Health
curl http://localhost:8080/api/health

# Métriques Prometheus
curl http://localhost:8081/actuator/prometheus

# Shell dans le container (debug)
docker exec -it the-bridge sh
```

---

## Minikube / Kubernetes

### 1. Démarrer Minikube

```bash
minikube start --cpus=2 --memory=4096
```

### 2. Construire l'image et la charger dans Minikube

```bash
# Pointez Docker vers le daemon Minikube (évite un registry externe)
eval $(minikube docker-env)

# Build de l'image
docker build -t the-bridge:local .
```

### 3. Créer les secrets Kubernetes (à faire UNE FOIS, localement)

```bash
# Encoder vos valeurs en base64
echo -n "votre-valeur" | base64

# Copier le template et remplir les valeurs encodées
cp k8s/secret.example.yaml /tmp/secret.yaml
# Éditer /tmp/secret.yaml avec les vraies valeurs base64
nano /tmp/secret.yaml

# Appliquer les secrets (ne PAS commiter /tmp/secret.yaml)
kubectl apply -f k8s/namespace.yaml
kubectl apply -f /tmp/secret.yaml -n the-bridge

# Supprimer le fichier temporaire
rm /tmp/secret.yaml
```

### 4. Déployer l'application

```bash
# Appliquer tous les manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### 5. Déployer le monitoring

```bash
kubectl apply -f k8s/monitoring/prometheus-configmap.yaml
kubectl apply -f k8s/monitoring/prometheus-deployment.yaml
kubectl apply -f k8s/monitoring/grafana-deployment.yaml
```

### 6. Vérifier les pods

```bash
kubectl get pods -n the-bridge
kubectl get pods -n the-bridge -w  # watch en temps réel
```

### 7. Vérifier les services

```bash
kubectl get services -n the-bridge
```

### 8. Consulter les logs

```bash
# Logs de l'app
kubectl logs -f deployment/the-bridge -n the-bridge

# Logs Prometheus
kubectl logs -f deployment/prometheus -n the-bridge

# Logs Grafana
kubectl logs -f deployment/grafana -n the-bridge
```

### 9. Accéder aux services

```bash
# URL de l'application
minikube service the-bridge-service -n the-bridge --url

# Ou ouvrir directement dans le navigateur
minikube service the-bridge-service -n the-bridge

# Prometheus
minikube service prometheus-service -n the-bridge --url

# Grafana
minikube service grafana-service -n the-bridge --url
```

### 10. Tester l'application

```bash
# Récupérer l'IP Minikube
MINIKUBE_IP=$(minikube ip)

# Health check (app)
curl http://$MINIKUBE_IP:30080/api/health

# Health check (actuator)
curl http://$MINIKUBE_IP:30081/actuator/health

# Métriques Prometheus
curl http://$MINIKUBE_IP:30081/actuator/prometheus | head -20

# Prometheus UI
open http://$MINIKUBE_IP:30090

# Grafana UI (admin/admin)
open http://$MINIKUBE_IP:30030
```

### 11. Supprimer le déploiement

```bash
# Supprimer l'application
kubectl delete -f k8s/ --recursive -n the-bridge

# Supprimer le namespace entier (inclut tout)
kubectl delete namespace the-bridge

# Arrêter Minikube
minikube stop

# Supprimer le cluster Minikube (reset complet)
minikube delete
```

---

## Monitoring — Prometheus & Grafana

### Métriques exposées automatiquement

Spring Boot Actuator + Micrometer expose automatiquement :
- JVM (heap, GC, threads, classes)
- HTTP requests (durée, taux de succès/erreur, par endpoint)
- Datasource (connexions HikariCP)
- Cache Redis
- Spring Security (tentatives auth)
- Métriques custom (taux de rate limiting)

**Endpoint** : `http://localhost:8081/actuator/prometheus`

### Monitoring local avec Docker Compose

```bash
# Démarrer le stack monitoring
docker compose -f docker-compose.monitoring.yml up -d

# Prometheus : http://localhost:9090
# Grafana    : http://localhost:3000  (admin / admin)
```

> L'app Spring Boot doit tourner localement sur le port 8081 pour que Prometheus puisse la scraper via `host.docker.internal:8081`.

### Dashboards Grafana recommandés

Importer depuis [grafana.com/dashboards](https://grafana.com/grafana/dashboards/) :

| Dashboard | ID | Description |
|---|---|---|
| Spring Boot Statistics | 6756 | JVM + HTTP + threads |
| Spring Boot 3.x | 19004 | Complet Spring Boot 3+ |
| Redis Dashboard | 11835 | Métriques Redis |

### Monitoring en Kubernetes

Grafana est accessible via Minikube :

```bash
minikube service grafana-service -n the-bridge
# Login: admin / admin (à changer)
```

---

## GitHub Actions CI/CD

### Pipeline globale

```
Push / PR
    │
    ├── build-and-test   → Compile + Tests (H2) + JaCoCo
    ├── sonarqube        → Analyse statique + Quality Gate ← dépend de build-and-test
    ├── secret-scan      → Gitleaks (parallel)
    ├── dependency-scan  → OWASP (parallel)
    │
    └── [sur main seulement]
         ├── docker-build-push → Build + Trivy + Push ghcr.io
         └── deploy-render     → Déploiement production Render
```

### GitHub Secrets requis

Aller dans : `Settings → Secrets and variables → Actions → New repository secret`

| Secret | Valeur |
|---|---|
| `SONAR_TOKEN` | Token SonarCloud |
| `SONAR_HOST_URL` | `https://sonarcloud.io` |
| `RENDER_API_KEY` | API Key Render |
| `RENDER_SERVICE_ID` | ID du service Render (srv-xxx...) |
| `NVD_API_KEY` | (optionnel) API key NVD pour OWASP |

### GitHub Variables (Actions → Variables)

| Variable | Valeur |
|---|---|
| `SONAR_ORGANIZATION` | Nom de l'organisation SonarCloud |
| `SONAR_PROJECT_KEY` | `the-bridge-api` |

---

## Déploiement Render (Production)

### Premier déploiement

1. Créer un service **Web Service** sur [render.com](https://render.com)
2. Connecter le repository GitHub
3. Configurer **Environment Variables** dans Render avec toutes les variables de `.env.example`
4. Récupérer le **Service ID** (format `srv-xxx...`) dans les paramètres du service
5. Générer une **API Key** dans : `Account Settings → API Keys`
6. Ajouter dans GitHub Secrets : `RENDER_API_KEY` et `RENDER_SERVICE_ID`

### Déploiements automatiques

Tout push sur `main` qui passe le CI complet déclenche automatiquement un déploiement Render via l'API.

### Variables Render à configurer

Dans Render → Environment → Add Environment Variable :

```
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
MAIL_USERNAME
MAIL_PASSWORD
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
FACEBOOK_CLIENT_ID
FACEBOOK_CLIENT_SECRET
STRIPE_SECRET_KEY
STRIPE_PUBLISHABLE_KEY
STRIPE_WEBHOOK_SECRET
BLOCKCHAIN_ENABLED=false
BLOCKCHAIN_RPC_URL
BLOCKCHAIN_CHAIN_ID
BLOCKCHAIN_PRIVATE_KEY
REDIS_URL
CORS_ALLOWED_ORIGINS
RATE_LIMIT_GLOBAL_PER_MIN=100
RATE_LIMIT_LOGIN_PER_MIN=5
RATE_LIMIT_REGISTER_PER_10MIN=3
RATE_LIMIT_PASSWORD_RESET_PER_15MIN=3
RATE_LIMIT_EXPENSIVE_PER_MIN=10
RATE_LIMIT_AUTHENTICATED_PER_MIN=120
```

---

## Gestion des secrets

### Politique

| Localisation | Secrets réels | Autorisé |
|---|---|---|
| `.env` (local) | Oui | ✅ Jamais commité |
| `.env.example` (Git) | Non (placeholders vides) | ✅ Commité |
| `application.properties` (Git) | Non (références `${VAR}`) | ✅ Commité |
| GitHub Secrets | Oui | ✅ Encryptés par GitHub |
| GitHub Actions logs | Non | ✅ Masqués automatiquement |
| Kubernetes Secret | Oui (base64) | ✅ Jamais commité |
| `k8s/secret.example.yaml` | Non (placeholders) | ✅ Commité |
| Dockerfile | Non | ✅ Vérifier avec `.dockerignore` |
| Docker image | Non | ✅ `.dockerignore` exclut `.env` |

### Rotation des secrets

Si un secret est accidentellement exposé :

```bash
# 1. Révoquer immédiatement :
#    - Gmail : Settings > Security > App passwords > Revoke
#    - Google OAuth : console.cloud.google.com > Credentials > Regenerate
#    - Facebook : developers.facebook.com > App Settings > Regenerate secret
#    - Stripe : dashboard.stripe.com > Developers > API Keys > Roll key
#    - Cloudinary : cloudinary.com > Settings > Security > Regenerate API Secret
#    - JWT Secret : générer un nouveau avec `openssl rand -base64 64`
#    - Blockchain private key : transférer les fonds, abandonner le wallet

# 2. Purger l'historique Git si le fichier était commité :
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch .env' \
  --prune-empty --tag-name-filter cat -- --all

# Ou avec BFG Repo Cleaner (plus simple) :
# bfg --delete-files .env
# git reflog expire --expire=now --all
# git gc --prune=now --aggressive
# git push origin --force --all
```

---

## Architecture CI/CD

```
Developer
    │
    ▼
Git Push (feature/*)
    │
    ▼
GitHub
    │
    ▼
GitHub Actions
    │
    ├─── build-and-test ──────────────────────┐
    │    ├── Checkout                         │
    │    ├── Java 17 + Maven cache            │
    │    ├── mvn compile                      │
    │    ├── mvn test (H2 profile=test)       │
    │    └── JaCoCo coverage report           │
    │                                         │
    ├─── sonarqube ◄──────────────────────────┘
    │    ├── mvn sonar:sonar
    │    └── Quality Gate check ← BLOQUE si KO
    │
    ├─── secret-scan (parallel)
    │    └── Gitleaks ← BLOQUE si secret trouvé
    │
    ├─── dependency-scan (parallel)
    │    └── OWASP check (rapport, warn si CVSS >= 7)
    │
    └─── [main only] ─────────────────────────────────┐
         │                                            │
         ▼                                            │
    docker-build-push                                 │
         ├── docker build (multi-stage)               │
         ├── Trivy scan ← BLOQUE si CRITICAL/HIGH     │
         └── Push → ghcr.io/owner/the-bridge:sha      │
                                                      │
         ▼                                            │
    deploy-render                                     │
         ├── POST /v1/services/{id}/deploys           │
         └── Wait for status=live ──────────────────► │
                                                      │
         ▼
    Production (Render)

──────────────────────────────────────────────────────
Validation locale Kubernetes :

Docker Image
    │
    ▼
Minikube
    ├── kubectl apply -f k8s/
    ├── Readiness probe /actuator/health/readiness
    ├── Liveness probe /actuator/health/liveness
    └── Services NodePort
         ├── App         → :30080
         ├── Actuator    → :30081
         ├── Prometheus  → :30090
         └── Grafana     → :30030
```

---

## Troubleshooting

### L'application ne démarre pas en local

```bash
# Vérifier que MySQL est accessible
mysql -h 127.0.0.1 -P 3307 -u root -p

# Vérifier que Redis est accessible
redis-cli -p 6379 ping

# Vérifier les variables d'environnement
cat .env

# Logs Spring Boot détaillés
./mvnw spring-boot:run --debug 2>&1 | tail -100
```

### Les tests échouent en CI

```bash
# Reproduire exactement le comportement CI en local
./mvnw test -Dspring.profiles.active=test

# Voir les logs de test détaillés
./mvnw test -Dspring.profiles.active=test -Dsurefire.failIfNoSpecifiedTests=false -e
```

### Docker build échoue

```bash
# Vérifier le Dockerfile localement
docker build --no-cache -t the-bridge:debug . 2>&1 | tail -50

# Vérifier que .dockerignore est bien appliqué
docker build --no-cache -t test . && docker run --rm test ls /app
```

### Kubernetes — pod en CrashLoopBackOff

```bash
# Décrire le pod pour voir les events
kubectl describe pod -l app=the-bridge -n the-bridge

# Logs du pod (même s'il crashe)
kubectl logs -l app=the-bridge -n the-bridge --previous

# Vérifier que le Secret est bien créé
kubectl get secrets -n the-bridge
kubectl describe secret the-bridge-secrets -n the-bridge
```

### Kubernetes — readiness probe fails

```bash
# Tester l'endpoint depuis l'intérieur du pod
kubectl exec -it deployment/the-bridge -n the-bridge -- \
  wget -qO- http://localhost:8081/actuator/health

# Vérifier la connexion DB/Redis depuis le pod
kubectl exec -it deployment/the-bridge -n the-bridge -- \
  wget -qO- http://localhost:8080/api/health
```

### SonarQube Quality Gate échoue

Vérifier sur SonarCloud :
- `sonarcloud.io/project/overview?id=the-bridge-api`
- Examiner les issues : bugs, vulnérabilités, code smells
- La couverture de code doit être ≥ 80% (selon la configuration du Quality Gate)

### Render deploy échoue

```bash
# Vérifier les logs du deploy Render
# Dashboard Render → Service → Deploys → Logs

# Vérifier via API
curl -H "Authorization: Bearer $RENDER_API_KEY" \
  "https://api.render.com/v1/services/$RENDER_SERVICE_ID/deploys?limit=5"
```

### Grafana ne se connecte pas à Prometheus

```bash
# Vérifier que Prometheus est UP
kubectl logs deployment/prometheus -n the-bridge | tail -20

# Tester la connexion depuis Grafana
kubectl exec -it deployment/grafana -n the-bridge -- \
  wget -qO- http://prometheus-service:9090/-/ready
```
