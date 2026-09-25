# Changelog

Toutes les évolutions notables du projet. Chaque ligne renvoie à son issue et à sa pull request.

## [1.0.1] — 25/09/2026

Revue après soumission : aucune fonctionnalité nouvelle, aucune migration, contrat inchangé (v1.4).

### Ajouté
- Modèles d'issue (story, bug) et de pull request au format de ticket complet : fonctionnement actuel, résultats attendus, non-régression, Definition of Done (#47, PR #50).
- Tests automatiques du frontend avec Vitest : couche API et tableau du formateur, dont la preuve que la moyenne affichée vient de l'API (F3, RG17) ; `npm test` (#48, PR #51).

### Documentation
- Les PR de livraison #45 et #46, ouvertes sans issue, sont rattachées au ticket #49 (#49, PR #52).

## [1.0] — 25/09/2026

Version finale : le changement de besoin de l'étape 3 est intégré, le bug signalé par le client est corrigé, les trois exigences Should restantes sont livrées.

### Changement de besoin — relecture par deux pairs
- **Analyse mise à jour** avant le code : cahier des charges v2, diagrammes D1, D2, D4 (#33, PR #38).
- Chaque exercice est relu par **deux pairs différents**, tirés au sort parmi les présents ; le second relecteur manquant est affecté à la présence suivante (#34, PR #39).
- Nouvelle migration **V3** (V1 et V2 inchangées) : `relecteurs_requis`, statut `RELU_PARTIELLEMENT`, unicité `(exercice_id, relecteur_id)`. Les exercices existants gardent leur relecture unique (« à partir de maintenant ») (#34, PR #39).
- **Note retenue** = moyenne des relectures rendues, **provisoire** tant qu'il en manque une ; tableau : moyenne des notes retenues et `moyenneProvisoire` ; `GET /api/etudiants/{id}/exercices` (#35, PR #40).
- Écrans : « Mes exercices » côté étudiant, moyenne marquée provisoire côté formateur (#36, PR #41).
- Contrat d'API v1.2 → v1.4.

### Corrigé
- Deux étudiants qui marquent leur présence au même instant sont désormais **tous les deux** enregistrés : l'affectation d'un exercice en attente se fait sous verrou (#32, PR #37 — test de reproduction commité avant le correctif).
- Test intermittent : l'ordre des commentaires ne dépend plus du tirage aléatoire (PR #43).

### Ajouté
- Clôture d'une session par le formateur, qui fige présences, dépôts et relectures (#14, PR #42).
- Présence ajoutée à la main par le formateur, marquée `FORMATEUR` et visible dans le tableau (#13, PR #43).
- Blocage de 2 minutes après 5 codes erronés (#16, PR #44).

### Documentation
- `CHANGELOG.md` 0.1 et 1.0, README de livraison avec parcours de vérification (PR #45) ; analyse et journal alignés sur le sujet révisé à cinq étapes (PR #46). Ces deux PR n'avaient pas d'issue : rattachées après coup à #49.

### Retiré du périmètre (re-priorisation de l'étape 3)
- Remplacement du lien d'un exercice (#18) et présence session par session dans le tableau (#17) : sacrifiés pour absorber le changement de besoin — justification dans le cahier des charges v2 et le journal.

## [0.1] — 25/09/2026

Première version : les 12 exigences Must.

### Ajouté
- Socle : Spring Boot 4.1 (Java 17, wrapper `mvnw`), migrations Flyway V1 (schéma conforme à D2) et V2 (données de démonstration), React + Vite, `docker compose` (#1, PR #19, correctif de construction PR #27).
- Format d'erreur `{ code, message }` centralisé, sans stack trace (#2, PR #20).
- Ouverture de session avec un code de 6 caractères valable 15 minutes (#3, PR #21).
- Choix de l'étudiant dans la liste de sa promotion, couche d'accès à l'API côté front (#4, PR #22).
- Marquage de présence par code, contrôles dans l'ordre du diagramme D3 (#5, PR #23).
- Dépôt d'exercice et affectation d'un relecteur tiré au sort parmi les présents (#6, PR #24).
- Liste des relectures à faire (#7, PR #25) et relecture notée, définitive (#8, PR #26).
- Tableau du formateur, en nombre de requêtes fixe (#9, PR #28).
- Écrans formateur (#10, PR #29), étudiant (#11, PR #30) et relecteur (#12, PR #31).
