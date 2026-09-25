---
name: Bug
about: Un comportement observé qui contredit une exigence EFx ou une règle RGx
title: ""
labels: bug
---

**Constaté par** … **le** …

Réf. `EFx` · Règles `RGx` · Priorité **Must / Should / Could**

### Fonctionnement actuel

<!-- Ce qui se passe, avec la requête et la réponse exactes. -->

### Étapes de reproduction

1. …
2. …

### Résultats attendus

* …

### Critères d'acceptation

* Quand …, alors …

### Non-régression à vérifier

* …

### Definition of Done

* [ ] Un test qui reproduit le bug est commité **avant** le correctif, et échoue
* [ ] Le correctif, dans un commit séparé, fait passer ce test
* [ ] Migration Flyway versionnée si le schéma change
* [ ] Branche dédiée, PR qui ferme cette issue, `main` sain après fusion

### Références

* Cahier des charges : `EFx`, `RGx`
