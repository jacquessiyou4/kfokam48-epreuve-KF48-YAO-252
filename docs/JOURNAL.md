# Journal de bord — KF48-YAO-252

> Une entrée **par étape**, écrite **au moment où je la termine**.

Chaque entrée répond aux trois mêmes questions :

- **Fait** — ce que je viens de terminer
- **Bloqué** — ce qui m'a coûté du temps, et combien
- **IA** — ce que je lui ai demandé, et **comment j'ai vérifié sa réponse**

---

> **Note sur le premier commit.** Le dépôt commence par `[JALON] depart` : c'est ce que demandait la version du `LISEZ-MOI` remise en début d'épreuve (« Vérifie que tu peux pousser : `git commit --allow-empty -m "[JALON] depart"` »). La version révisée remise à 12h02 demande de ne pas utiliser le préfixe `[JALON]` pour ce test. Le commit est déjà poussé sur `main` : le retirer imposerait un `push --force`, que le sujet sanctionne. Il reste donc en place ; les trois jalons notés sont `[JALON] analyse`, `[JALON] v0.1` et `[JALON] v1.0`, dans cet ordre.

---

## Étape 1 — Analyse et conception (09h35 → 10h35)

**Fait :** cahier des charges v1 (14 EF, 8 ENF, 21 RG, 12 hypothèses H1–H12, contradiction Q10/Q15 tranchée), quatre diagrammes Mermaid (D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 bonus états d'un exercice), contrat d'API complété en v1.1 (8 opérations ajoutées ; le message du commit 5c4de7c en annonçait 7 par erreur, corrigé par un commit dédié sans réécrire `main`), 18 issues créées avec labels Must (12) / Should (5) / Could (1). `.gitignore` posé avant tout code.

**Bloqué :** ~10 min sur Q10 contre Q15, tranchée pour Q15 : le contrat imposé renvoie `409 RELECTURE_DEJA_RENDUE`, ce qui n'a de sens que si une relecture est définitive. ~5 min à comprendre que `POST /api/relectures/{id}` ne dit pas qui relit (pas d'authentification, Q1) : c'est le trou du sujet, résolu par un champ optionnel `relecteurId` (H1). ~25 min sur la lisibilité des diagrammes : relus dans GitHub, D1 (acteurs mal placés, libellés tronqués), D3 (quatre niveaux d'alternatives imbriquées, texte minuscule) et D4 (disposition horizontale trop large) ont été refaits et vérifiés par un rendu local (`mermaid-cli`) avant d'être poussés.

**IA :** utilisée pour rédiger le cahier des charges, les diagrammes, le contrat et les issues. Vérifications : chaque RG relue contre la question `Qx` citée ; chaque code HTTP de D3 recoupé avec le contrat (tableau de correspondance en bas de D3) ; les tables et contraintes de D2 serviront telles quelles pour la migration V1 ; contrat validé par `redocly lint` (0 erreur) ; diagrammes D2, D3, D4 validés par le rendu Mermaid, D1 validé sur une version réduite contenant toutes ses constructions ; numéros d'issues vérifiés après création pour que les renvois `#4`, `#7`, `#9` pointent au bon endroit.

---

## Étape 2 — Première version (10h37 → 11h35)

**Fait :** les 12 issues Must (#1 à #12), une branche et une PR chacune (PR #19 à #31, dont un correctif Docker #27). Backend : migrations V1 (schéma = D2) et V2 (démo), 5 opérations imposées + 5 ajoutées, erreurs centralisées, 376 tests (unitaires sur RG1-RG10, RG17, ENF6 ; intégration sur chaque code HTTP du contrat ; nombre de requêtes du tableau mesuré pour ENF2). Frontend React : couche `src/api/client.js`, écrans formateur, étudiant, relecteur. `docker compose up --build` vérifié de bout en bout.

**Bloqué :** ~25 min sur la construction Docker : `mvnw dependency:go-offline` figé à 0 Ko/s (connexion morte, délai Maven trop long) — diagnostiqué en mesurant le trafic réseau du conteneur, corrigé par délais + tentatives + cache BuildKit (PR #27). ~5 min sur Spring Boot 4, seule version proposée par Spring Initializr (paquets de test déplacés). ~10 min sur des tests qui se polluaient via une base H2 partagée : d'abord un contournement, puis la vraie correction (une base par contexte). ~10 min sur l'extension de navigateur automatisée, instable : vérification finale faite en partie par l'API et Chrome headless.

**IA :** a écrit le code et les tests. Vérifié par : les migrations appliquées sur un vrai PostgreSQL 16 (pas seulement H2) ; chaque règle de gestion couverte par un test qui échouerait sans elle ; un piège repéré en relisant : Jackson tronquait `12.5` en `12` — la note est lue en décimal pour renvoyer `NOTE_INVALIDE` ; les écrans essayés dans le navigateur (session ouverte, présence enregistrée avec un code en minuscules, rendu en 360 px).

**À signaler :** le fichier `ENVELOPPE.md` de l'étape 3 m'a été remis à 11h09, avant le jalon `v0.1`. Je l'ai lu, mais rien n'a été conçu ni codé en conséquence avant ce jalon : la v0.1 applique toujours Q6 (un seul relecteur), comme prévu à l'étape 1.

---

## Étape 3 — Enveloppe (11h36 → 11h54)

**Fait :** bug et changement traités séparément. **Bug** : issue #32 ouverte avec l'analyse et la façon de reproduire, puis test de reproduction qui échoue (commit `2e3b577`), puis correctif (verrou `SELECT … FOR UPDATE` + re-vérification) sur une branche dédiée, PR #37. **Changement** : issues #33 à #36, analyse mise à jour d'abord (cahier des charges v2, D1, D2, D4 — PR #38), migration **V3 ajoutée** sans toucher V1/V2 (PR #39), note retenue et provisoire + contrat v1.3 (PR #40), écrans (PR #41).

**Bloqué :** ~5 min à traduire le signalement du client : nos insertions de présence ne se gênent pas entre elles ; le conflit venait de la relance d'affectation (RG9) faite dans la même transaction — deux présences simultanées affectaient le même exercice en attente et la seconde perdait sa présence au rollback. ~5 min pour rendre le test **déterministe** (générateur aléatoire de test qui fait attendre chaque transaction l'autre) plutôt que d'espérer un entrelacement. ~5 min sur V3 : sous H2, retirer l'ancienne contrainte d'unicité laissait son index (la clé étrangère s'appuyait dessus).

**IA :** a proposé la cause, le test et le correctif. Vérifié : le test **échoue avant** le correctif sur exactement la violation annoncée (`uk_relecture_exercice`) et passe après ; V3 appliquée sur une vraie base PostgreSQL **déjà remplie**, avec comparaison SQL des exercices, relectures et notes avant/après (identiques) ; le scénario du client rejoué de bout en bout (note provisoire 12 puis définitive 13,50, moyenne du tableau recalculée).

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :**
- **#18 — remplacer le lien (Could)** : la seule Could, faible valeur pour le client, et la règle Q13 (« tant que personne n'a commencé à le relire ») devient ambiguë avec deux relecteurs.
- **#17 — présence session par session (Should)** : le tableau affiche déjà le nombre de présences (champ imposé) ; le détail est un confort.
- **#15 — voir sa note (Should)** n'est pas sacrifiée mais **absorbée** par #35 : avec une note provisoire, l'étudiant doit la voir, elle devient un Must.
- Décision « à partir de maintenant » (H13) : les exercices déjà relus par un seul pair gardent leur note définitive plutôt que de devenir provisoires rétroactivement — plus simple, et fidèle aux mots du client.

---

## Étape 4 — Version finale

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 5 — Soumission

**Fait :**

**Ce que je referais autrement avec une journée de plus :**
