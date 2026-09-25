# kfokam48-epreuve-KF48-YAO-252

Épreuve finale fullstack KFOKAM48 — **SIYOU NOUKIMI JACQUES** (KF48-YAO-252, centre de Yaoundé).

Application de gestion des présences et de relecture par les pairs : le formateur ouvre une session et obtient un code, l'étudiant marque sa présence et dépose son exercice, un camarade présent le relit et le note, le formateur suit tout dans un tableau.

**Frontend : React (Vite)** — le plus léger à démarrer pour trois écrans simples, build statique rapide sans configuration lourde.

## Démarrer l'application

Prérequis : Docker avec Docker Compose.

```bash
git clone https://github.com/jacquessiyou4/kfokam48-epreuve-KF48-YAO-252.git
cd kfokam48-epreuve-KF48-YAO-252
docker compose up --build
```

Puis ouvrir :

| | Adresse |
|---|---|
| Application (frontend) | http://localhost:5173 |
| API (backend) | http://localhost:8080/api |

Le premier démarrage télécharge les dépendances Maven et npm (plusieurs minutes). La base PostgreSQL est créée par les migrations Flyway, puis **des données de démonstration** sont chargées (`backend/src/main/resources/db/migration/V2__donnees_demo.sql`).

**Si le port 8080 ou 5173 est déjà pris** sur votre machine :

```bash
BACKEND_PORT=18080 FRONTEND_PORT=15173 docker compose up --build
```

### Données de démonstration

| Promotion | Étudiants | Sessions |
|---|---|---|
| Promotion Yaoundé 2026 (id 1) | 6 (AMOUGOU Brice … NGONO Estelle) | « Introduction à Spring Boot » (clôturée), « API REST et validation » (code expiré, non clôturée) |
| Promotion Douala 2026 (id 2) | 3 | « Bases SQL » : un exercice sans relecteur éligible |

Pour tester le marquage de présence, ouvrir une nouvelle session depuis l'écran formateur : son code est valable 15 minutes.

## Lancer les tests

Sans base locale (H2 en mémoire) :

```bash
cd backend
./mvnw verify
```

## Organisation du dépôt

| Dossier | Contenu |
|---|---|
| `docs/` | Cahier des charges, journal de bord, diagrammes (Mermaid) |
| `api/` | Contrat d'API OpenAPI (`contrat.yaml`) |
| `backend/` | Spring Boot 4.1, Java 17, Maven (wrapper `mvnw`), Flyway, PostgreSQL |
| `frontend/` | React 19 + Vite, servi par nginx dans Docker |
