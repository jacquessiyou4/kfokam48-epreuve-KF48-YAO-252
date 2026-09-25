# D3 — Séquence : marquer sa présence

Opération imposée `POST /api/presences { code, etudiantId }`. Chaque branche d'erreur correspond à un code HTTP et à un `code` d'erreur du contrat (`api/contrat.yaml`). Les vérifications sont faites **dans cet ordre** par le service.

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Front (écran étudiant)
    participant API as PresenceController
    participant S as PresenceService
    participant L as LimiteurTentatives
    participant DB as Repositories (JPA)
    participant A as AffectationService

    E->>F: saisit le code
    F->>API: POST /api/presences { code, etudiantId }

    alt corps invalide (code ou etudiantId manquant)
        API-->>F: 400 { code: "VALIDATION", message }
    else corps valide
        API->>S: marquer(code, etudiantId)
        S->>DB: findEtudiant(etudiantId)
        alt étudiant inconnu
            S-->>API: EtudiantInconnuException
            API-->>F: 404 { code: "ETUDIANT_INCONNU" }
        else
            S->>L: verifierNonBloque(etudiantId)
            alt bloqué depuis moins de 2 min (RG5)
                L-->>S: TropDeTentativesException
                S-->>API: TropDeTentativesException
                API-->>F: 429 { code: "TROP_DE_TENTATIVES" }
            else non bloqué
                S->>DB: findSessionByCode(code)
                alt code inconnu (RG4)
                    S->>L: enregistrerEchec(etudiantId)
                    S-->>API: CodeInconnuException
                    API-->>F: 400 { code: "CODE_INCONNU" }
                else session trouvée
                    alt étudiant d'une autre promotion (RG19)
                        S-->>API: EtudiantHorsPromotionException
                        API-->>F: 403 { code: "ETUDIANT_HORS_PROMOTION" }
                    else code expiré ou session clôturée (RG1, RG2)
                        S-->>API: CodeExpireException
                        API-->>F: 410 { code: "CODE_EXPIRE" }
                    else déjà présent (RG3)
                        S->>DB: existsPresence(sessionId, etudiantId)
                        DB-->>S: true
                        S-->>API: DejaPresentException
                        API-->>F: 409 { code: "DEJA_PRESENT" }
                    else cas nominal
                        S->>DB: save(Presence source=ETUDIANT)
                        S->>L: reinitialiser(etudiantId)
                        S->>A: reaffecterExercicesEnAttente(sessionId) (RG9)
                        S-->>API: PresenceDto
                        API-->>F: 201 { id, sessionId, etudiantId, source: "ETUDIANT" }
                    end
                end
            end
        end
    end
    F-->>E: affiche « Présence enregistrée » ou le message d'erreur
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
