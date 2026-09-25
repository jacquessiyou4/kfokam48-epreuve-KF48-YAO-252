# D2 — Modèle de données

Ce diagramme correspond **exactement** aux migrations Flyway de `backend/src/main/resources/db/migration/`. Toute évolution du schéma passe par une nouvelle migration **et** une mise à jour de ce fichier dans le même commit.

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "regroupe"
    PROMOTION ||--o{ SESSION_COURS : "a"
    SESSION_COURS ||--o{ PRESENCE : "enregistre"
    ETUDIANT ||--o{ PRESENCE : "marque"
    SESSION_COURS ||--o{ EXERCICE : "reçoit"
    ETUDIANT ||--o{ EXERCICE : "dépose (auteur)"
    EXERCICE ||--o| RELECTURE : "est relu par"
    ETUDIANT ||--o{ RELECTURE : "relit (relecteur)"

    PROMOTION {
        BIGINT id PK
        VARCHAR_100 nom UK "NOT NULL"
    }
    ETUDIANT {
        BIGINT id PK
        VARCHAR_150 nom "NOT NULL"
        BIGINT promotion_id FK "NOT NULL"
    }
    SESSION_COURS {
        BIGINT id PK
        VARCHAR_200 titre "NOT NULL"
        BIGINT promotion_id FK "NOT NULL"
        VARCHAR_6 code UK "NOT NULL"
        TIMESTAMPTZ ouverture_at "NOT NULL"
        TIMESTAMPTZ expiration_at "NOT NULL, = ouverture + 15 min (RG1)"
        TIMESTAMPTZ cloturee_at "NULL tant que non clôturée"
    }
    PRESENCE {
        BIGINT id PK
        BIGINT session_id FK "NOT NULL"
        BIGINT etudiant_id FK "NOT NULL"
        VARCHAR_10 source "ETUDIANT | FORMATEUR (RG15)"
        TIMESTAMPTZ marquee_at "NOT NULL"
    }
    EXERCICE {
        BIGINT id PK
        BIGINT session_id FK "NOT NULL"
        BIGINT etudiant_id FK "NOT NULL (auteur)"
        VARCHAR_500 lien "NOT NULL (RG18)"
        VARCHAR_30 statut "EN_ATTENTE_RELECTEUR | EN_ATTENTE_RELECTURE | RELU"
        TIMESTAMPTZ depose_at "NOT NULL"
        TIMESTAMPTZ modifie_at "NULL"
    }
    RELECTURE {
        BIGINT id PK
        BIGINT exercice_id FK,UK "NOT NULL (RG7)"
        BIGINT relecteur_id FK "NOT NULL"
        INTEGER note "NULL tant que non rendue, CHECK 0..20 (RG10)"
        VARCHAR_2000 commentaire "NULL tant que non rendue"
        TIMESTAMPTZ assignee_at "NOT NULL"
        TIMESTAMPTZ rendue_at "NULL tant que non rendue"
    }
```

## Contraintes portées par la base

| Contrainte | Règle |
|---|---|
| `UNIQUE (session_id, etudiant_id)` sur `presence` | RG3 — une seule présence par session |
| `UNIQUE (session_id, etudiant_id)` sur `exercice` | RG12 — un seul exercice par session |
| `UNIQUE (exercice_id)` sur `relecture` | RG7 — un seul relecteur par exercice |
| `UNIQUE (code)` sur `session_cours` | RG4 — un code désigne une seule session |
| `CHECK (source IN ('ETUDIANT','FORMATEUR'))` | RG15 |
| `CHECK (note BETWEEN 0 AND 20)` | RG10 |
| `CHECK (statut IN (...))` | D4 — cycle de vie d'un exercice |

**Choix de modélisation :**
- La table s'appelle `session_cours` et non `session`, mot réservé ou ambigu dans plusieurs SGBD.
- Pas de table `relecteur` ni `formateur` : le relecteur est un étudiant (`relecture.relecteur_id → etudiant.id`), le formateur n'est pas identifié (Q1, cahier des charges §3).
- Une relecture est créée **au moment de l'affectation** (note et `rendue_at` à `NULL`) ; elle est « rendue » quand `rendue_at` est renseigné. C'est ce qui permet de compter les relectures en attente (RG21).
- La moyenne (RG17) n'est **pas stockée** : elle est calculée à la demande à partir de `relecture.note`.
- Le compteur de codes erronés (RG5) n'est pas en base : il est gardé en mémoire (cahier des charges H10).
