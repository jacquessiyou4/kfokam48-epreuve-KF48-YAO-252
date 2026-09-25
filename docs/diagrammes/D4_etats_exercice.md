# D4 — États-transitions : cycle de vie d'un exercice (bonus)

La colonne `exercice.statut` (D2) prend exactement les trois valeurs ci-dessous.

```mermaid
stateDiagram-v2
    direction LR
    [*] --> Depose : POST /api/exercices (EF5)

    state choix_relecteur <<choice>>
    Depose --> choix_relecteur : AffectationService (RG8)

    choix_relecteur --> EN_ATTENTE_RELECTURE : un étudiant présent, autre que l'auteur, est tiré au sort
    choix_relecteur --> EN_ATTENTE_RELECTEUR : aucun relecteur éligible (RG9)

    EN_ATTENTE_RELECTEUR --> EN_ATTENTE_RELECTURE : nouvelle présence dans la session → affectation retentée (RG9)
    EN_ATTENTE_RELECTEUR --> EN_ATTENTE_RELECTEUR : lien remplacé (RG14)
    EN_ATTENTE_RELECTURE --> EN_ATTENTE_RELECTURE : lien remplacé (RG14)

    EN_ATTENTE_RELECTURE --> RELU : POST /api/relectures/{id} note entière 0–20 (RG10)

    RELU --> [*]

    note right of RELU
        État final : la relecture est définitive (RG11, Q15).
        Un nouvel envoi renvoie 409 RELECTURE_DEJA_RENDUE.
    end note

    note left of EN_ATTENTE_RELECTEUR
        Visible « en attente » dans le tableau du formateur (Q11, RG21).
        La clôture de la session fige l'exercice dans son état (RG20).
    end note
```

| État (`statut`) | Signification | Remplacer le lien ? | Rendre une note ? |
|---|---|---|---|
| `EN_ATTENTE_RELECTEUR` | Déposé, aucun relecteur disponible | Oui (si session non clôturée) | Non, aucune relecture n'existe |
| `EN_ATTENTE_RELECTURE` | Relecteur assigné, relecture non rendue | Oui (si session non clôturée) | Oui (si session non clôturée) |
| `RELU` | Note et commentaire rendus — définitif | Non → `409` | Non → `409 RELECTURE_DEJA_RENDUE` |

« Déposé » n'est pas un statut persistant : l'affectation a lieu dans la même transaction que le dépôt, la réponse `201 { id, statut }` renvoie donc directement `EN_ATTENTE_RELECTURE` ou `EN_ATTENTE_RELECTEUR`.
