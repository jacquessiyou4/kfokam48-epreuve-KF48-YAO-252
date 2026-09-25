# D3 — Séquence : marquer sa présence

Opération imposée `POST /api/presences { code, etudiantId }`. Chaque branche correspond à un code HTTP et à un `code` d'erreur du contrat (`api/contrat.yaml`). Les branches sont **numérotées dans l'ordre où le service fait ses contrôles** : la première condition vraie l'emporte (un étudiant bloqué reçoit `429` même avec un bon code).

```mermaid
sequenceDiagram
    actor E as Étudiant
    participant F as Front
    participant C as PresenceController
    participant S as PresenceService
    participant L as Limiteur
    participant DB as Repositories

    E->>F: saisit le code
    F->>C: POST /api/presences {code, etudiantId}

    alt 1. champ manquant
        C-->>F: 400 VALIDATION
    else 2. étudiant inconnu
        C->>S: marquer(code, etudiantId)
        S->>DB: findEtudiant
        DB-->>S: vide
        S-->>C: EtudiantInconnuException
        C-->>F: 404 ETUDIANT_INCONNU
    else 3. bloqué après 5 erreurs (RG5)
        C->>S: marquer(code, etudiantId)
        S->>L: verifierNonBloque
        L-->>S: bloqué
        S-->>C: TropDeTentativesException
        C-->>F: 429 TROP_DE_TENTATIVES
    else 4. code inconnu (RG4)
        C->>S: marquer(code, etudiantId)
        S->>DB: findSessionByCode
        DB-->>S: vide
        S->>L: enregistrerEchec
        S-->>C: CodeInconnuException
        C-->>F: 400 CODE_INCONNU
    else 5. autre promotion (RG19)
        C->>S: marquer(code, etudiantId)
        S-->>C: EtudiantHorsPromotionException
        C-->>F: 403 ETUDIANT_HORS_PROMOTION
    else 6. code expiré ou session clôturée (RG1, RG2)
        C->>S: marquer(code, etudiantId)
        S-->>C: CodeExpireException
        C-->>F: 410 CODE_EXPIRE
    else 7. déjà présent (RG3)
        C->>S: marquer(code, etudiantId)
        S->>DB: existsPresence
        DB-->>S: true
        S-->>C: DejaPresentException
        C-->>F: 409 DEJA_PRESENT
    else 8. cas nominal
        C->>S: marquer(code, etudiantId)
        S->>DB: save(Presence ETUDIANT)
        S->>L: reinitialiser
        S->>S: reaffecter exercices en attente (RG9)
        S-->>C: PresenceDto
        C-->>F: 201 {id, sessionId, etudiantId, source}
    end
    F-->>E: « Présence enregistrée » ou message d'erreur
```

## Correspondance avec le contrat

| Cas | HTTP | `code` d'erreur | Règle |
|---|---|---|---|
| Présence enregistrée | **201** | — | EF3 |
| Champ manquant | **400** | `VALIDATION` | B4 |
| Code inconnu | **400** | `CODE_INCONNU` | RG4 |
| Étudiant hors promotion | **403** | `ETUDIANT_HORS_PROMOTION` | RG19 |
| Étudiant inconnu | **404** | `ETUDIANT_INCONNU` | H9 |
| Déjà présent | **409** | `DEJA_PRESENT` | RG3 |
| Code expiré / session clôturée | **410** | `CODE_EXPIRE` | RG1, RG2 |
| Cinq erreurs consécutives | **429** | `TROP_DE_TENTATIVES` | RG5 |
