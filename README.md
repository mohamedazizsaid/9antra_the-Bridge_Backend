# 🌉 The Bridge — Enterprise LMS Backend API

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT_%26_OAuth2-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![TiDB Cloud](https://img.shields.io/badge/TiDB_Cloud-Distributed_SQL-E30C34?style=for-the-badge&logo=pingcap&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Minikube-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![Polygon](https://img.shields.io/badge/Polygon-Amoy_Testnet-8247E5?style=for-the-badge&logo=polygon&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-Payments-008CDD?style=for-the-badge&logo=stripe&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-Dashboards-F46800?style=for-the-badge&logo=grafana&logoColor=white)
[![Render Production](https://img.shields.io/badge/Render-Live_Production-46E3B7?style=for-the-badge&logo=render&logoColor=white)](https://nineantra-the-bridge-backend.onrender.com)
[![CI/CD Pipeline](https://github.com/mohamedazizsaid/9antra_the-Bridge_Backend/actions/workflows/ci.yml/badge.svg)](https://github.com/mohamedazizsaid/9antra_the-Bridge_Backend/actions/workflows/ci.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=the-bridge-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=the-bridge-api)

<br/>

**The Bridge** est une plateforme backend robuste, scalable et hautement sécurisée de gestion de l'apprentissage (LMS / EdTech) et de certification infalsifiable ancrée sur la blockchain Polygon.

> 🌐 **Environnement de Production (Render)** : [https://nineantra-the-bridge-backend.onrender.com](https://nineantra-the-bridge-backend.onrender.com)  
> 📖 **Swagger UI Production** : [https://nineantra-the-bridge-backend.onrender.com/swagger-ui/index.html](https://nineantra-the-bridge-backend.onrender.com/swagger-ui/index.html)  
> 🩺 **Health Check Production** : [https://nineantra-the-bridge-backend.onrender.com/api/health](https://nineantra-the-bridge-backend.onrender.com/api/health)

[Fonctionnalités](#-fonctionnalités-clés) • [Architecture](#-architecture-système) • [Stack Technique](#-stack-technique) • [Démarrage Rapide](#-démarrage-rapide) • [API Reference](#-documentation-de-lapi-swagger) • [Déploiement Render](#-déploiement-en-production-render) • [CI/CD & DevOps](#-devops-cicd--monitoring)

---

</div>

## 📌 Sommaire

- [✨ Présentation Générale](#-présentation-générale)
- [🏗️ Architecture Système](#-architecture-système)
- [🚀 Fonctionnalités Clés](#-fonctionnalités-clés)
  - [1. Gestion des Identités & Sécurité (IAM)](#1-gestion-des-identités--sécurité-iam)
  - [2. Catalogue & Gestion des Formations](#2-catalogue--gestion-des-formations)
  - [3. Sessions de Formation & Emploi du Temps](#3-sessions-de-formation--emploi-du-temps)
  - [4. Inscriptions & Moteur de Progression](#4-inscriptions--moteur-de-progression)
  - [5. Suivi des Présences & Évaluations](#5-suivi-des-présences--évaluations)
  - [6. Certifications Blockchain & Génération PDF](#6-certifications-blockchain--génération-pdf)
  - [7. Paiements en Ligne Stripe](#7-paiements-en-ligne-stripe)
  - [8. Notifications Temps Réel & Emails](#8-notifications-temps-réel--emails)
  - [9. Administration & Tableaux de Bord Analytiques](#9-administration--tableaux-de-bord-analytiques)
- [🛠️ Stack Technique](#️-stack-technique)
- [🌐 Déploiement en Production (Render)](#-déploiement-en-production-render)
- [⚡ Démarrage Rapide](#-démarrage-rapide)
  - [Prérequis](#prérequis)
  - [Configuration Locale (.env)](#configuration-locale-env)
  - [Lancement avec Base Locale](#lancement-avec-base-locale)
  - [Lancement Full Stack via Docker Compose](#lancement-full-stack-via-docker-compose)
- [📖 Documentation de l'API (Swagger)](#-documentation-de-lapi-swagger)
  - [Points d'Entrée Principaux](#points-dentrée-principaux)
- [🧪 Tests & Qualité de Code](#-tests--qualité-de-code)
- [📊 Observabilité, Métriques & Monitoring](#-observabilité-métriques--monitoring)
- [🐳 Conteneurisation & Déploiement Kubernetes](#-conteneurisation--déploiement-kubernetes)
- [🔄 Pipeline CI/CD GitHub Actions](#-pipeline-cicd-github-actions)
- [🔐 Sécurité & Gestion des Secrets](#-sécurité--gestion-des-secrets)
- [📋 Guide des Variables d'Environnement](#-guide-des-variables-denvironnement)
- [🤝 Contribution](#-contribution)
- [📄 Licence](#-licence)

---

## ✨ Présentation Générale

**The Bridge** fournit l'infrastructure backend complète pour un écosystème d'apprentissage moderne reliant étudiants, formateurs et administrateurs. Conçu selon les meilleures pratiques de l'ingénierie logicielle (architecture en couches, stateless JWT, rate limiting distribué, validation stricte, observabilité Prometheus/Grafana, container multi-stage sécurisé), il intègre nativement l'ancrage cryptographique de diplômes sur **Polygon (Amoy Testnet)**.

---

## 🏗️ Architecture Système

```
                                  ┌────────────────────────────────────────────────┐
                                  │             Clients Frontend / Mobile          │
                                  │      (Web React/Angular/Vue, Mobile App)       │
                                  └──────────────────────┬─────────────────────────┘
                                                         │ HTTPS / WSS
                                                         ▼
                                  ┌────────────────────────────────────────────────┐
                                  │             Reverse Proxy / Ingress            │
                                  │              (Nginx / Kubernetes / Render)     │
                                  └──────────────────────┬─────────────────────────┘
                                                         │
                ┌────────────────────────────────────────┴────────────────────────────────────────┐
                │                                                                                 │
                ▼ (Port 8080 : REST + WebSockets)                                                 ▼ (Port 8081 : Actuator)
┌─────────────────────────────────────────────────────────────┐                 ┌──────────────────────────────────┐
│                   THE BRIDGE SPRING BOOT API                │                 │       SPRING BOOT ACTUATOR       │
│                                                             │                 │                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────┐  │                 │  • /actuator/health              │
│  │ Security & Auth  │  │ Rate Limiter     │  │ Audit Log │  │                 │  • /actuator/prometheus (Basic)  │
│  │ (JWT + OAuth2)   │  │ (Bucket4j/Redis) │  │ Filter    │  │                 │  • JVM, HikariCP, Redis Metrics  │
│  └────────┬─────────┘  └────────┬─────────┘  └─────┬─────┘  │                 └─────────────────┬────────────────┘
│           │                     │                  │        │                                   │ Scraping HTTP
│  ┌────────▼─────────────────────▼──────────────────▼─────┐  │                                   ▼
│  │                    REST Controllers                   │  │                 ┌──────────────────────────────────┐
│  │  Auth • Formations • Sessions • Enrollments • Tests   │  │                 │            PROMETHEUS            │
│  │  Certificates • Payments • Notifications • Admin      │  │                 │        (Time-Series DB)          │
│  └──────────────────────────────┬────────────────────────┘  │                 └─────────────────┬────────────────┘
│                                 │                           │                                   │ Querying
│  ┌──────────────────────────────▼────────────────────────┐  │                                   ▼
│  │                     Service Layer                     │  │                 ┌──────────────────────────────────┐
│  │   Business Logic • PDF Engine • Blockchain Anchoring  │  │                 │             GRAFANA              │
│  └──────────────────────────────┬────────────────────────┘  │                 │     (Visual Dashboards & Alerts) │
│                                 │                           │                 └──────────────────────────────────┘
└───────┬─────────────────────────┼───────────────────────────┼─────────────────────────┬───────────────┬──────────┘
        │                         │                           │                         │               │
        ▼                         ▼                           ▼                         ▼               ▼
┌───────────────────┐     ┌───────────────┐           ┌───────────────┐         ┌───────────────┐ ┌───────────────┐
│ MySQL 8 (Local)   │     │    Redis 7    │           │  Cloudinary   │         │    Stripe     │ │ Polygon Amoy  │
│ TiDB Cloud (Prod) │     │ Cache & Limit │           │ Media Storage │         │ Payment Engine│ │  Blockchain   │
└───────────────────┘     └───────────────┘           └───────────────┘         └───────────────┘ └───────────────┘
```

---

## 🚀 Fonctionnalités Clés

### 1. Gestion des Identités & Sécurité (IAM)
- **Authentification Hybride** : Authentification standard (Email + Mot de passe sécurisé via `BCrypt`) et connexion sociale **OAuth2** (Google Sign-In, Facebook Login).
- **Jetons Stateless JWT** : Émission et validation de tokens HMAC-SHA sécurisés avec extraction des rôles et contrôle d'expiration.
- **Cycle de Vie des Comptes & OTP** : Inscription avec confirmation par code OTP email à 6 chiffres, réexpédition et expiration de codes.
- **Réinitialisation de Mot de Passe Sécurisée** : Workflow complet de réinitialisation avec codes éphémères à usage unique.
- **Contrôle d'Accès Basé sur les Rôles (RBAC)** :
  - `ROLE_ETUDIANT` (Stagiaire)
  - `ROLE_FORMATEUR` (Enseignant / Instructeur)
  - `ROLE_ADMIN` (Gestionnaire de la plateforme)
- **Protection Anti-Abus & Rate Limiting** : Filtrage distribué via **Bucket4j** et **Redis** pour contrer le bruteforce, le credential stuffing et les surcharges API.
- **Sécurité HTTP Durcie** : CSP (*Content Security Policy*), protection XSS active, blocage anti-clickjacking via *FrameOptions*, gestion granulaire CORS et headers de corrélation (`X-Correlation-ID`).
- **Endpoint Monitoring Isolé** : Endpoint `/actuator/prometheus` sécurisé par authentification HTTP Basic dédiée (`ROLE_MONITORING`).

### 2. Catalogue & Gestion des Formations
- **CRUD Complet des Formations** : Titre, description, niveau de difficulté, durée estimée, prix, prérequis et objectifs pédagogiques.
- **Structuration Pédagogique Hiérarchique** : Formations composées de modules, chapitres, leçons et ressources téléchargeables.
- **Gestion Multimédia via Cloudinary CDN** : Upload et optimisation d'images de couverture, logos et supports de cours.
- **Moteur de Recherche & Filtrage Avancé** : Recherche multi-critères, filtres par catégorie, niveau, prix et pagination optimisée.

### 3. Sessions de Formation & Emploi du Temps
- **Planification des Sessions** : Calendrier des sessions en direct, gestion des dates de début/fin et de la capacité maximale.
- **Affectation des Formateurs** : Assignation d'instructeurs qualifiés à chaque promotion ou session live.
- **Suivi des Places Disponibles** : Calcul automatique en temps réel des places restantes lors des inscriptions.

### 4. Inscriptions & Moteur de Progression
- **Workflow d'Inscription** : Gestion des inscriptions gratuites ou soumises à paiement préalable.
- **Suivi Granulaire de la Progression** : Enregistrement de l'état d'avancement leçon par leçon pour chaque apprenant.
- **Calcul Dynamique du Taux de Complétion** : Calcul en temps réel du pourcentage global d'avancement de la formation.
- **Déclenchement d'Éligibilité au Certificat** : Déblocage automatique des évaluations finales et des certifications dès 100% de progression atteinte.

### 5. Suivi des Présences & Évaluations
- **Feuille de Présence Numérique** : Enregistrement des présences/absences par session pour chaque apprenant.
- **Système de Quizz & Évaluations** : Création d'évaluations formatives et sommatives avec seuil minimal de validation.
- **Calcul et Restitution des Résultats** : Historique des notes, feedbacks des formateurs et statistiques de réussite.

### 6. Certifications Blockchain & Génération PDF
- **Moteur de Rendu PDF (Apache PDFBox)** : Génération automatisée de diplômes haute résolution personnalisés avec métadonnées officielles.
- **Ancrage Immuable sur Polygon Blockchain** :
  - Calcul de l'empreinte cryptographique unique **SHA-256** du certificat.
  - Envoi d'une transaction zéro-valeur vers le testnet **Polygon Amoy (Chain ID: 80002)** via **Web3j**, intégrant le hash dans la payload hexadécimale de transaction.
  - Sauvegarde du `transactionHash` officiel vérifiable sur l'explorateur public PolygonScan.
- **Portail Public de Vérification** : Endpoint et QR Code intégrés au PDF permettant à un employeur ou tiers de vérifier instantanément l'authenticité d'un diplôme.

### 7. Paiements en Ligne Stripe
- **Intégration Stripe Checkout & Payment Intents** : Traitement sécurisé des transactions par carte bancaire.
- **Webhooks Stripe avec Vérification de Signature** : Traitement asynchrone des événements de paiement (`charge.succeeded`, `payment_intent.failed`), déblocage immédiat de l'accès aux cours.
- **Facturation & Reçus** : Archivage des transactions et historique financier par utilisateur.

### 8. Notifications Temps Réel & Emails
- **WebSockets STOMP / SockJS** : Diffusion en direct d'événements (approbation d'inscription, début de session, nouveau message, obtention de certificat).
- **Centre de Notifications In-App** : Consultation, compteur d'éléments non lus et archivage.
- **Emails Transactionnels HTML Riches** : Envoi de templates HTML soignés pour la bienvenue, les codes OTP, les alertes de sécurité et l'envoi de certificats.

### 9. Administration & Tableaux de Bord Analytiques
- **Gestion Centralisée des Utilisateurs** : Activation, suspension, changement de rôles et modération.
- **Tableau de Bord Analytique** : Métriques business (chiffre d'affaires cumulé, inscriptions actives, formations populaires, taux de rétention).
- **Journal d'Audit Système** : Traçabilité des actions sensibles des administrateurs et des flux d'authentification.

---

## 🛠️ Stack Technique

| Domaine | Technologie | Version / Détails |
|---|---|---|
| **Langage & Framework** | Java / Spring Boot | Java 17 LTS / Spring Boot 4.1.0 |
| **Sécurité** | Spring Security, JJWT, OAuth2 | JWT (Access/Refresh), Google & Facebook Social Auth |
| **Base de Données (Production)** | **TiDB Cloud (Serverless)** | Base NewSQL Distribuée, Auto-scaling, Haute Disponibilité, SSL/TLS |
| **Base de Données (Local)** | MySQL | 8.0+ (Driver Connector-J, Hibernate / JPA) |
| **Base de Données de Test** | H2 Database | Base mémoire pour tests unitaires & CI automatisée |
| **Cache & Limitation de Débit**| Redis + Bucket4j | Redis 7.x Alpine / Bucket4j 8.10.1 Distributed Token Bucket |
| **Blockchain** | Web3j (Ethereum / Polygon) | Polygon Amoy Testnet (Chain ID: 80002) |
| **Paiements** | Stripe Java SDK | 24.0.0 (PaymentIntents & Webhooks) |
| **Média & Stockage** | Cloudinary Java SDK | 1.39.0 (Images de cours & Avatars) |
| **Génération Documentaire** | Apache PDFBox | 3.0.3 (Diplômes & Certificats vectoriels) |
| **Temps Réel & Messagerie** | Spring WebSocket (STOMP) / JavaMail | WebSockets + SockJS / Spring Starter Mail |
| **Documentation API** | Springdoc OpenAPI (Swagger 3) | 2.5.0 (Swagger UI interactif) |
| **Observabilité & Métriques** | Spring Actuator, Micrometer, Prometheus | Scraping `/actuator/prometheus` |
| **Visualisation Métriques** | Grafana | Dashboards JVM, HikariCP, HTTP, Redis |
| **Conteneurisation** | Docker & Docker Compose | Multi-Stage Build, Utilisateur non-root (`spring:spring`) |
| **Orchestration** | Kubernetes / Minikube | Manifests (Deployments, Services, ConfigMaps, Secrets) |
| **CI / CD** | GitHub Actions | 6 jobs : Build, Tests, SonarQube, Gitleaks, OWASP, Docker/Render |
| **Qualité & Couverture** | JaCoCo / SonarCloud | Couverture de code, détection de code smells et failles |

---

## 🌐 Déploiement en Production (Render)

L'application **The Bridge API** est déployée et accessible publiquement sur la plateforme cloud **Render** avec intégration et déploiement continus (CI/CD).

### 🔗 Liens Officiels de Production

| Service | URL Officielle |
|---|---|
| 🌍 **API Base URL** | [`https://nineantra-the-bridge-backend.onrender.com`](https://nineantra-the-bridge-backend.onrender.com) |
| 📚 **Swagger UI Interactif** | [`https://nineantra-the-bridge-backend.onrender.com/swagger-ui/index.html`](https://nineantra-the-bridge-backend.onrender.com/swagger-ui/index.html) |
| 🩺 **Endpoint Health Check** | [`https://nineantra-the-bridge-backend.onrender.com/api/health`](https://nineantra-the-bridge-backend.onrender.com/api/health) |
| 📊 **Actuator Health** | [`https://nineantra-the-bridge-backend.onrender.com/actuator/health`](https://nineantra-the-bridge-backend.onrender.com/actuator/health) |

### 🗄️ Base de Données de Production (TiDB Cloud Serverless)

En environnement de production, l'API s'appuie sur un cluster **TiDB Cloud Serverless** (PingCAP) :
- **Architecture NewSQL Distribuée** : Compatibilité MySQL 8 native avec scaling horizontal automatique et sharding transparent.
- **Haute Disponibilité & Résilience** : Réplication multi-zones active-active avec tolérance de pannes sans perte de données (RPO = 0).
- **Sécurité & Chiffrement SSL/TLS** : Connexion chiffrée obligatoire via TLS v1.2 / v1.3 (`sslMode=VERIFY_IDENTITY` & `useSSL=true`).
- **Conformité ACID Complète** : Transactions distribuées pour garantir l'intégrité absolue des paiements Stripe et des inscriptions.

### 🔄 Déploiement Continu Automatisé (CI/CD)

1. **Déclenchement Automatique** : Tout commit fusionné sur la branche `main` déclenche le pipeline GitHub Actions.
2. **Quality Gate & Sécurité** : Les tests unitaires, l'analyse SonarCloud, le scan de secrets Gitleaks et le scan OWASP doivent **tous être validés à 100%**.
3. **Déploiement Render API** : Le job `deploy-render` transmet un hook de déploiement sécurisé via l'API Render (`POST /v1/services/${RENDER_SERVICE_ID}/deploys`) et attend l'état `live`.
4. **Zéro Interruption (Zero-Downtime)** : Render effectue un démarrage contrôlé avec vérification de la santé via `/api/health` avant de basculer le trafic.

---

## ⚡ Démarrage Rapide

### Prérequis

Assurez-vous de disposer des outils suivants sur votre machine :
- **Java JDK 17+** (`java -version`)
- **Git** (`git --version`)
- **Docker & Docker Compose** (recommandé pour Redis et MySQL)

### Configuration Locale (.env)

Créez votre fichier local `.env` à la racine du dossier backend à partir du modèle fourni :

```bash
cp .env.example .env
```

Renseignez les variables indispensables (voir la section [Variables d'Environnement](#-guide-des-variables-denvironnement)).

### Lancement avec Base Locale

1. **Démarrer les services d'infrastructure (MySQL & Redis) :**
```bash
# MySQL 8 sur le port 3307 (pour éviter les conflits avec le port 3306 par défaut)
docker run -d --name mysql-bridge -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=yourpassword \
  -e MYSQL_DATABASE=the_bridge \
  mysql:8.0

# Redis 7
docker run -d --name redis-bridge -p 6379:6379 redis:7-alpine
```

2. **Compiler et exécuter l'application Spring Boot :**
```bash
# Sur Linux / macOS
./mvnw clean spring-boot:run

# Sur Windows PowerShell
.\mvnw.cmd clean spring-boot:run
```

3. **Points d'accès disponibles :**
- 🚀 **API REST** : `http://localhost:8080`
- 📚 **Swagger UI** : `http://localhost:8080/swagger-ui/index.html`
- 🩺 **Health Check** : `http://localhost:8080/api/health`
- 📊 **Actuator Health** : `http://localhost:8081/actuator/health`
- 📈 **Métriques Prometheus** : `http://localhost:8081/actuator/prometheus`

---

### Lancement Full Stack via Docker Compose

Pour démarrer l'ensemble de l'écosystème en une seule commande (API + MySQL + Redis) :

```bash
docker compose up -d --build
```

Pour lancer la pile complète avec la suite d'observabilité (**Prometheus + Grafana**) :

```bash
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
```

- **Grafana** : `http://localhost:3000` *(Identifiants par défaut : `admin` / `admin`)*
- **Prometheus UI** : `http://localhost:9090`

---

## 📖 Documentation de l'API (Swagger)

L'API est intégralement documentée via **OpenAPI 3 / Swagger**. Accédez à l'interface interactive sur :
👉 `http://localhost:8080/swagger-ui/index.html`

### Points d'Entrée Principaux

#### 🔐 Authentification & Profil (`/api/auth`, `/api/users`)
| Méthode | Endpoint | Description | Accès |
|---|---|---|---|
| `POST` | `/api/auth/register` | Inscription d'un nouveau stagiaire (multipart avec avatar) | Public |
| `POST` | `/api/auth/login` | Connexion et émission du token JWT | Public |
| `POST` | `/api/auth/verify-email` | Validation du code OTP à 6 chiffres | Public |
| `POST` | `/api/auth/resend-code` | Renvoi d'un nouveau code de validation OTP | Public |
| `POST` | `/api/auth/forgot-password` | Demande de réinitialisation de mot de passe | Public |
| `POST` | `/api/auth/reset-password` | Application du nouveau mot de passe avec code | Public |
| `POST` | `/api/auth/oauth/login` | Authentification sociale (Google / Facebook) | Public |
| `GET` | `/api/users/me` | Récupération du profil de l'utilisateur connecté | Authentifié |
| `PUT` | `/api/users/profile` | Mise à jour des informations de profil | Authentifié |

#### 🎓 Formations & Sessions (`/api/formations`, `/api/sessions`)
| Méthode | Endpoint | Description | Accès |
|---|---|---|---|
| `GET` | `/api/formations` | Liste paginée des formations avec filtres | Public |
| `GET` | `/api/formations/{id}` | Détail complet d'une formation et son plan | Public |
| `POST` | `/api/formations` | Création d'une nouvelle formation | Formateur / Admin |
| `PUT` | `/api/formations/{id}` | Modification d'une formation existante | Formateur / Admin |
| `DELETE` | `/api/formations/{id}` | Suppression d'une formation | Admin |
| `GET` | `/api/sessions` | Calendrier des sessions ouvertes | Authentifié |
| `POST` | `/api/sessions` | Création d'une session de cours | Formateur / Admin |

#### 📈 Inscriptions & Progression (`/api/enrollments`, `/api/progressions`)
| Méthode | Endpoint | Description | Accès |
|---|---|---|---|
| `POST` | `/api/enrollments` | Inscription à une formation ou session | Étudiant |
| `GET` | `/api/enrollments/my-enrollments` | Liste des inscriptions de l'apprenant connecté | Étudiant |
| `GET` | `/api/progressions/{formationId}` | Taux d'avancement et leçons complétées | Étudiant |
| `POST` | `/api/progressions/lesson/{lessonId}/complete` | Marquer une leçon comme complétée | Étudiant |

#### ⛓️ Certificats & Blockchain (`/api/certificates`)
| Méthode | Endpoint | Description | Accès |
|---|---|---|---|
| `GET` | `/api/certificates/my-certificates` | Liste des diplômes obtenus par l'étudiant | Étudiant |
| `GET` | `/api/certificates/download/{id}` | Téléchargement du diplôme PDF haute définition | Authentifié |
| `GET` | `/api/certificates/verify/{certificateCode}` | Vérification publique de validité d'un diplôme | Public |
| `POST` | `/api/certificates/generate/{formationId}` | Génération et ancrage on-chain Polygon | Formateur / Admin |

#### 💳 Paiements Stripe (`/api/payments`)
| Méthode | Endpoint | Description | Accès |
|---|---|---|---|
| `POST` | `/api/payments/create-intent` | Création d'un `PaymentIntent` Stripe | Étudiant |
| `POST` | `/api/payments/stripe/webhook` | Réception asynchrone des événements Stripe | Stripe Server (Signature) |

#### 📊 Administration (`/api/admin`)
| Méthode | Endpoint | Description | Accès |
|---|---|---|---|
| `GET` | `/api/admin/users` | Gestion et liste exhaustive des utilisateurs | Admin |
| `PUT` | `/api/admin/users/{id}/role` | Modification du rôle d'un utilisateur | Admin |
| `GET` | `/api/admin/stats` | Indicateurs de performance et KPIs globaux | Admin |
| `GET` | `/api/admin/revenue` | Statistiques de revenus Stripe consolidées | Admin |

---

## 🧪 Tests & Qualité de Code

Le projet est couvert par une suite complète de tests unitaires et d'intégration utilisant **JUnit 5**, **Mockito** et la base mémoire **H2** (ne requérant aucun service externe actif).

### Exécution des Tests

```bash
# Exécution simple des tests unitaires
./mvnw test -Dspring.profiles.active=test

# Exécution avec rapport de couverture JaCoCo
./mvnw verify -Dspring.profiles.active=test
```

Le rapport de couverture JaCoCo est généré dans :
`target/site/jacoco/index.html`

### Analyse Statique SonarQube / SonarCloud

Pour lancer une analyse qualité en local :

```bash
./mvnw sonar:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=$SONAR_TOKEN \
  -Dspring.profiles.active=test
```

---

## 📊 Observabilité, Métriques & Monitoring

L'application intègre **Spring Boot Actuator** et **Micrometer Prometheus** pour exposer les indicateurs de santé et de performance en temps réel.

### Métriques Surveillées
- **Santé JVM** : Utilisation de la mémoire Heap/Non-Heap, Garbage Collector pauses, Threads actifs.
- **Trafic HTTP** : Débit de requêtes, latence moyenne/p99, taux d'erreurs 4xx et 5xx par endpoint.
- **Pool de Connexions BD (HikariCP)** : Connexions actives, idle, en attente, temps d'acquisition.
- **Cache & Rate Limiting (Redis)** : Commandes par seconde, hit/miss ratio, clés expirées.

### Sécurisation des Métriques
L'accès aux métriques sensibles `/actuator/prometheus` est verrouillé via une authentification **HTTP Basic** dédiée :

```bash
curl -u monitoring_user:monitoring_secure_password http://localhost:8081/actuator/prometheus
```

---

## 🐳 Conteneurisation & Déploiement Kubernetes

### Docker (Multi-Stage Build)

Le fichier `Dockerfile` utilise un build multi-étapes pour garantir un conteneur final ultra-léger et sécurisé (utilisateur non-root `spring:spring`, JRE 17 Eclipse Temurin minimal).

```bash
# Construction de l'image
docker build -t the-bridge:latest .

# Exécution du conteneur
docker run -d \
  --name the-bridge \
  -p 8080:8080 \
  -p 8081:8081 \
  --env-file .env \
  the-bridge:latest
```

### Kubernetes / Minikube

Les manifests prêts pour la production et le test local se trouvent dans le répertoire `k8s/` :

```bash
# 1. Démarrer Minikube
minikube start --cpus=2 --memory=4096

# 2. Pointer Docker vers le daemon Minikube et builder l'image
eval $(minikube docker-env)
docker build -t the-bridge:local .

# 3. Déployer l'application et les secrets
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.example.yaml -n the-bridge
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 4. Vérifier le statut des Pods
kubectl get pods -n the-bridge -w
```

---

## 🔄 Pipeline CI/CD GitHub Actions

Chaque push ou pull request déclenche un workflow automatisé complet (`.github/workflows/ci.yml`) comprenant :

```
Push / Pull Request
    │
    ├── 1. Build & Test ──────► Compilation Java 17 + Tests JUnit/H2 + JaCoCo Coverage
    │
    ├── 2. SonarQube Scan ────► Analyse qualité SonarCloud + Validation Quality Gate
    │
    ├── 3. Secret Scan ───────► Scan Gitleaks anti-fuite de secrets (bloquant si détecté)
    │
    ├── 4. Security Scan ─────► Analyse des vulnérabilités des dépendances (OWASP)
    │
    └── [Branche 'main' uniquement]
         │
         ├── 5. Docker Build ──► Multi-stage Build + Scan de vulnérabilités Trivy + Push Registry
         │
         └── 6. Deploy Render ─► Déploiement automatique continu en production sur Render
```

---

## 🔐 Sécurité & Gestion des Secrets

### Bonnes Pratiques Appliquées
- 🚫 **Zéro Secret dans Git** : Le fichier `.env` est ignoré par `.gitignore` et `.dockerignore`.
- 🔑 **Gestion via Secrets Managers** : En production et en CI/CD, les secrets sont injectés exclusivement via **GitHub Actions Secrets** et **Render Environment Variables**.
- 🛡️ **Rate Limiting Distribué** : Protection par pallier selon le type de route (Login: 5 req/min, Register: 3 req/10min, API générale: 100 req/min).
- 🔒 **Stateless JWT & BCrypt** : Salage et hachage cryptographique robuste des mots de passe.

---

## 📋 Guide des Variables d'Environnement

| Variable | Description | Exemple / Défaut | Environnement |
|---|---|---|---|
| `SERVER_PORT` | Port d'écoute de l'API REST | `8080` | Local / Prod |
| `MANAGEMENT_SERVER_PORT` | Port d'écoute Actuator / Métriques | `8081` | Local / Prod |
| `DATABASE_URL` | URL JDBC MySQL (Local) ou TiDB Cloud Serverless (Prod) | Local: `jdbc:mysql://localhost:3307/the_bridge`<br/>Prod: `jdbc:mysql://<host>.prod.aws.tidbcloud.com:4000/the_bridge?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3` | Requis |
| `DATABASE_USERNAME` | Utilisateur base de données (ex: `<prefix>.root` pour TiDB) | `root` / `<user>.root` | Requis |
| `DATABASE_PASSWORD` | Mot de passe base de données | `secret` / `<tidb_password>` | Requis |
| `REDIS_URL` | Hôte Redis pour cache & rate limiter | `redis://localhost:6379` | Requis |
| `JWT_SECRET` | Clé secrète de signature HMAC-SHA (256-bit min) | `VOTRE_CLE_SECRETE_SECURISEE` | Requis |
| `JWT_EXPIRATION` | Durée de validité du token JWT en ms | `86400000` (24h) | Optionnel |
| `MAIL_USERNAME` | Adresse email expéditrice SMTP | `contact@thebridge.com` | Requis |
| `MAIL_PASSWORD` | Mot de passe d'application SMTP | `app_password` | Requis |
| `CLOUDINARY_CLOUD_NAME`| Nom du cloud Cloudinary pour les médias | `my_cloud` | Requis |
| `CLOUDINARY_API_KEY` | Clé API Cloudinary | `1234567890` | Requis |
| `CLOUDINARY_API_SECRET`| Secret API Cloudinary | `abcdef12345` | Requis |
| `STRIPE_SECRET_KEY` | Clé privée Stripe | `sk_test_...` | Requis |
| `STRIPE_WEBHOOK_SECRET`| Secret de validation des webhooks Stripe | `whsec_...` | Requis |
| `BLOCKCHAIN_ENABLED` | Activation de l'ancrage Polygon | `true` / `false` | Optionnel |
| `BLOCKCHAIN_RPC_URL` | Endpoint RPC Polygon Amoy | `https://rpc-amoy.polygon.technology/`| Optionnel |
| `BLOCKCHAIN_PRIVATE_KEY`| Clé privée hex (64 chars) du wallet émetteur| `0x...` | Requis si enabled |
| `MONITORING_USERNAME` | Identifiant HTTP Basic pour Prometheus | `monitoring_admin` | Requis |
| `MONITORING_PASSWORD` | Mot de passe HTTP Basic pour Prometheus | `monitoring_password` | Requis |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées pour les requêtes web | `http://localhost:3000,http://localhost:5173` | Requis |

---

## 🤝 Contribution

1. Forkez le projet (`git checkout -b feature/AmazingFeature`)
2. Commitez vos modifications selon la convention Conventional Commits (`git commit -m 'feat: add amazing feature'`)
3. Poussez votre branche (`git push origin feature/AmazingFeature`)
4. Ouvrez une **Pull Request**

---

## 📄 Licence

Ce projet est sous licence propriétaire — développé dans le cadre de la plateforme d'apprentissage **The Bridge**. Tous droits réservés.

<div align="center">
<sub>Fait avec passion pour l'excellence de l'apprentissage en ligne 🚀</sub>
</div>
