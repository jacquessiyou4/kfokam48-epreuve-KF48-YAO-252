# kfokam48-epreuve-KF48-YAO-252

Épreuve finale fullstack KFOKAM48 — **SIYOU NOUKIMI JACQUES** (KF48-YAO-252, centre de Yaoundé).

Application de gestion des présences et de relecture par les pairs : le formateur ouvre une session et obtient un code, l'étudiant marque sa présence et dépose son exercice, **deux camarades présents** le relisent et le notent, le formateur suit tout dans un tableau.

**Version 1.0** — voir [`CHANGELOG.md`](CHANGELOG.md).

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

### Parcours de vérification (5 minutes)

1. **Formateur** (http://localhost:5173/formateur) : choisir « Promotion Yaoundé 2026 ». Le tableau montre les moyennes de démo ; FOTSO et MBARGA ont des exercices en attente (surlignés). Ouvrir une session « Essai » : le code s'affiche en grand avec son heure d'expiration.
2. **Étudiant** (http://localhost:5173/etudiant) : choisir la promotion puis « AMOUGOU Brice », saisir le code → « Présence enregistrée ». Refaire avec « BELINGA Carine » et « ESSOMBA Hervé ».
3. Toujours en étudiant (bouton « Changer »), « FOTSO Mireille » marque sa présence puis dépose un lien pour la session « Essai » : deux camarades présents sont désignés.
4. **Relecteur** (http://localhost:5173/relecteur) : se choisir parmi les relecteurs désignés (AMOUGOU, BELINGA ou ESSOMBA — ceux dont la liste contient la session « Essai ») et rendre une note.
5. Retour en étudiant FOTSO : « Mes exercices » affiche la note **provisoire** ; après la seconde relecture, la moyenne définitive. Le tableau du formateur suit.
6. Formateur : « Clôturer » la session ; le code et le dépôt sont alors refusés.

### Fonctionnalités

| Écran | Ce qu'on peut faire |
|---|---|
| Formateur | Ouvrir une session et obtenir son code (15 min) · ajouter une présence à la main · clôturer une session · tableau : présences, exercices déposés, moyenne (provisoire ou non), exercices en attente, relectures à faire |
| Étudiant | Se choisir dans la liste (pas de mot de passe) · marquer sa présence avec le code · déposer le lien de son exercice · voir sa note retenue et les commentaires, sans le nom des relecteurs |
| Relecteur | Voir les exercices à relire · rendre une note entière sur 20 et un commentaire, définitifs |

Règles notables : deux relecteurs différents tirés au sort parmi les présents, jamais l'auteur ; note retenue = moyenne des relectures rendues, provisoire tant qu'il en manque une ; blocage de 2 minutes après 5 codes erronés. Tout est détaillé dans le cahier des charges.

## Lancer les tests

Sans base locale (H2 en mémoire) :

```bash
cd backend
./mvnw verify
```

Frontend, sans backend ni navigateur (Vitest) : tests unitaires de la couche API et du tableau du formateur, tests d'intégration des écrans étudiant et relecteur (jsdom + Testing Library, seul `fetch` est simulé) :

```bash
cd frontend
npm ci && npm test
```

## Documentation

| Document | Contenu |
|---|---|
| [`docs/CAHIER_DES_CHARGES.md`](docs/CAHIER_DES_CHARGES.md) | Exigences EF, règles de gestion RG, hypothèses et contradictions tranchées (v2) |
| [`docs/diagrammes/`](docs/diagrammes) | D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 cycle de vie d'un exercice |
| [`api/contrat.yaml`](api/contrat.yaml) | Contrat OpenAPI (v1.6) : les 5 opérations imposées et celles ajoutées |
| [`docs/JOURNAL.md`](docs/JOURNAL.md) | Journal de bord, une entrée par étape |
| [`CHANGELOG.md`](CHANGELOG.md) | Évolutions par version, avec issues et pull requests |

## Organisation du dépôt

| Dossier | Contenu |
|---|---|
| `docs/` | Cahier des charges, journal de bord, diagrammes (Mermaid) |
| `api/` | Contrat d'API OpenAPI (`contrat.yaml`) |
| `backend/` | Spring Boot 4.1, Java 17, Maven (wrapper `mvnw`), Flyway (V1 à V3), PostgreSQL |
| `frontend/` | React 19 + Vite, servi par nginx dans Docker |
