---
name: Story
about: Un résultat attendu par un utilisateur, rattaché à une exigence EFx ou à une règle RGx
title: ""
labels: story
---

**En tant qu'** …, **je souhaite** …, **afin de** …

Réf. `EFx` · Règles `RGx` · Priorité **Must / Should / Could** · Estimation …

### Résumé de la tâche

<!-- Deux ou trois phrases : pourquoi cette issue existe. -->

### Fonctionnement actuel

<!-- Ce qui existe aujourd'hui, même pour une nouvelle fonctionnalité (« rien : l'endpoint n'existe pas »). -->

### Résultats attendus

* …

### Règles métier

<!-- Les RGx appliquées, avec le code d'erreur prévu. Écrire « aucune » si c'est le cas. -->

### Critères d'acceptation

* Quand …, alors …
* Quand …, alors je reçois une erreur `4xx CODE`

### Non-régression à vérifier

* …

### Definition of Done

* [ ] Tests : une règle métier en unitaire, l'endpoint en intégration
* [ ] Contrat `api/contrat.yaml` et diagrammes à jour si le comportement change
* [ ] Migration Flyway versionnée si le schéma change
* [ ] Branche dédiée, PR qui ferme cette issue, `main` sain après fusion

### Références

* Cahier des charges : `EFx`, `RGx`, question `Qx` de `CLIENT.md`
