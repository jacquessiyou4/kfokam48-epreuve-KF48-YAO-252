# Cahier des charges — Présence & Relecture KFOKAM48

**Auteur :** SIYOU NOUKIMI JACQUES · KF48-YAO-252
**Version :** 2 · **Date :** 25/09/2026 — mise à jour après le changement de besoin de l'étape 3 (relecture par deux pairs)
**Frontend choisi :** React (Vite), parce que c'est le framework le plus léger à démarrer pour trois écrans simples, avec un build statique rapide et sans configuration lourde.

---

## 1. Contexte et objectif

La formation KFOKAM48 gère aujourd'hui la présence aux cours et l'évaluation des exercices « à la main ». Le formateur perd du temps à faire l'appel, ne sait pas qui a rendu quoi, et la relecture entre étudiants n'est ni organisée ni traçable.

L'application doit permettre :
- au **formateur** d'ouvrir une session de cours et d'obtenir un code de présence temporaire ;
- à l'**étudiant** de prouver sa présence en saisissant ce code depuis son téléphone, puis de déposer le lien de son exercice ;
- au **système** de confier chaque exercice à **deux pairs présents différents**, qui le notent et le commentent (v2 — changement de besoin de l'étape 3) ;
- au **formateur** de suivre, par étudiant, la présence, les dépôts, la moyenne reçue et les relectures en retard.

Objectif mesurable : à la fin d'une séance, le formateur lit dans un seul tableau qui était présent et où en est chaque exercice, sans aucune saisie manuelle hors exception (Q14).

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| **Formateur** | Ouvrir une session et obtenir son code ; ajouter une présence à la main ; clôturer une session ; consulter le tableau d'une promotion | Noter un exercice à la place d'un relecteur ; voir le code d'une autre application (pas de multi-formateur) |
| **Étudiant** | Se choisir dans la liste de sa promotion ; marquer sa présence avec un code ; déposer le lien de son exercice ; voir la note retenue (provisoire ou définitive) et les commentaires reçus | Relire son propre exercice ; voir le nom de son relecteur ; marquer sa présence après expiration du code ou clôture |
| **Relecteur** | Voir les exercices qui lui sont assignés ; rendre une note entière 0–20 et un commentaire, une seule fois | Choisir l'exercice qu'il relit ; modifier une relecture rendue |
| **Système** | Générer le code ; faire expirer le code ; tirer au sort **deux relecteurs différents** parmi les présents ; calculer la note retenue | — |

**Décision : le relecteur n'est pas un acteur distinct, c'est un étudiant dans un certain état** (il est désigné relecteur d'un exercice précis). Conséquence sur le modèle : pas de table `relecteur` ; la table `relecture` porte une clé étrangère `relecteur_id` vers `etudiant`. Depuis la v2, un exercice a **jusqu'à deux** relectures, jamais deux fois par le même étudiant. Un même étudiant est à la fois auteur de ses exercices et relecteur de ceux des autres.

## 3. Périmètre

**Inclus dans cette version :**
- ouverture de session avec code de présence expirant (15 min), clôture de session ;
- marquage de présence par code, présence manuelle par le formateur ;
- blocage temporaire après cinq codes erronés ;
- dépôt d'un lien d'exercice par session ;
- affectation aléatoire de **deux relecteurs différents**, relectures notées et commentées ;
- **note retenue** = moyenne des relectures rendues, marquée **provisoire** tant qu'il en manque une ;
- consultation par l'étudiant de sa note retenue et des commentaires (sans le nom des relecteurs) ;
- tableau récapitulatif du formateur par promotion ;
- données de démonstration chargées au démarrage (promotions, étudiants, sessions).

**Explicitement exclu :**
- toute authentification (mot de passe, jeton, SSO) — Q1 ;
- la gestion (création, modification, suppression) des promotions et des étudiants : ils sont fournis par les données de démonstration ;
- plusieurs formateurs, droits et rôles côté serveur ;
- le téléversement de fichiers : on dépose un **lien**, pas un fichier ;
- les notifications (e-mail, SMS, push) ;
- la modification d'une relecture déjà rendue (voir contradiction Q10/Q15) ;
- l'export (PDF, Excel) du tableau ;
- **(v2, sacrifié)** le remplacement du lien d'un exercice (ex-EF14, issue #18) : faible valeur, et la règle Q13 devient ambiguë avec deux relecteurs ;
- **(v2, sacrifié)** le détail de la présence session par session dans le tableau (ex-EF13, issue #17) : le nombre de présences reste affiché ;
- l'internationalisation : interface en français uniquement ;
- le rendu visuel soigné (non noté).

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session pour une promotion et obtient un code de présence | Quand j'envoie `POST /api/sessions {titre, promotionId}`, alors je reçois `201` avec un `code` de 6 caractères, `ouvertureAt` et `expirationAt = ouvertureAt + 15 min` ; sans `titre` je reçois `400` | Must |
| EF2 | L'étudiant se choisit dans la liste des étudiants de sa promotion, sans mot de passe | Quand j'ouvre l'écran étudiant et que je choisis une promotion, alors la liste de ses étudiants s'affiche et je peux me sélectionner | Must |
| EF3 | L'étudiant marque sa présence avec le code | Quand je saisis un code valide et non expiré, alors je reçois `201` avec `source = ETUDIANT` et ma présence est comptée dans le tableau du formateur | Must |
| EF4 | Le formateur ajoute une présence à la main | Quand le formateur ajoute un étudiant à une session, alors la présence est créée avec `source = FORMATEUR`, même après expiration du code, et le tableau la montre comme « ajoutée par le formateur » | Should |
| EF5 | L'étudiant dépose le lien de son exercice pour une session | Quand j'envoie un lien `http(s)` valide pour une session non clôturée, alors je reçois `201 {id, statut}` ; un second dépôt pour la même session renvoie `409` | Must |
| EF6 | Le système affecte **deux relecteurs différents** au hasard (v2) | Quand un exercice est déposé et qu'au moins deux autres étudiants sont présents, alors deux relectures sont assignées à deux étudiants différents, jamais à l'auteur, et l'exercice passe `EN_ATTENTE_RELECTURE` ; avec un seul présent éligible, une relecture est assignée et la seconde le sera à la présence suivante | Must |
| EF7 | Le relecteur voit la liste des relectures qui lui sont assignées | Quand je me sélectionne sur l'écran relecteur, alors je vois chaque exercice à relire avec son lien et son statut | Must |
| EF8 | Le relecteur rend une note et un commentaire | Quand j'envoie une note entière entre 0 et 20 et un commentaire, alors je reçois `200` ; l'exercice passe `RELU_PARTIELLEMENT` à la première relecture rendue et `RELU` quand toutes les relectures requises sont rendues ; une note de `21` ou `12.5` renvoie `400`, un second envoi renvoie `409` | Must |
| EF9 | Le formateur consulte le tableau d'une promotion | Quand je demande le tableau d'une promotion, alors je vois par étudiant : nombre de présences, exercices déposés, moyenne des **notes retenues** (calculée par l'API) **marquée provisoire si l'une d'elles l'est**, relectures qu'il doit encore faire, et ses exercices encore en attente ; promotion inconnue → `404` | Must |
| EF10 | Le formateur clôture une session | Quand je clôture une session, alors plus aucune présence, aucun dépôt, aucune relecture n'y est accepté (`409 SESSION_CLOTUREE`, ou `410` pour le code) | Should |
| EF11 | L'étudiant voit sa **note retenue** et les commentaires reçus, sans le nom des relecteurs | Quand je consulte mes exercices, alors je vois pour chacun son statut, la note retenue avec la mention **provisoire** s'il manque une relecture, et les commentaires rendus ; la réponse de l'API ne contient aucun identifiant ni nom de relecteur | **Must** (v2, était Should) |
| EF12 | Un étudiant qui se trompe cinq fois de code est bloqué deux minutes | Quand je saisis 5 codes inconnus d'affilée, alors la 6ᵉ tentative, même avec un bon code, renvoie `429` pendant 2 minutes, puis je peux réessayer | Should |
| ~~EF13~~ | ~~Le formateur voit la présence de chaque étudiant à chaque session~~ | **Sacrifiée en v2** pour absorber le changement de besoin (issue #17) | — |
| ~~EF14~~ | ~~L'étudiant remplace le lien de son exercice~~ | **Sacrifiée en v2** pour absorber le changement de besoin (issue #18) | — |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'écran étudiant (présence, dépôt) est utilisable sur un téléphone de 360 px de large | Ouvrir l'écran avec l'émulation mobile du navigateur en 360×640 : aucun défilement horizontal, champ de code et bouton utilisables au doigt |
| ENF2 | Le tableau répond en moins de 2 s pour une promotion de 60 étudiants et 30 sessions | Charger ce volume en base de test et mesurer `GET /api/tableau` (`curl -w "%{time_total}"`) ; pas de requête par étudiant (pas de N+1) |
| ENF3 | Toutes les erreurs ont le format `{code, message}`, en JSON | Tests d'intégration sur chaque code d'erreur du contrat ; aucune réponse d'erreur au format par défaut de Spring |
| ENF4 | Aucune stack trace n'est renvoyée au client | Provoquer une erreur inattendue (ex. JSON mal formé) : la réponse est `400`/`500` au format `{code, message}` |
| ENF5 | L'application démarre chez un tiers en trois commandes maximum, avec des données de démonstration | Suivre le `README` depuis un clone vierge dans un dossier vide |
| ENF6 | Le code de présence est lisible à voix haute et au tableau : 6 caractères majuscules/chiffres, sans caractères ambigus (`0/O`, `1/I/L`) | Test unitaire sur le générateur de code |
| ENF7 | Les dates échangées sont au format ISO-8601 avec fuseau (UTC côté serveur) | Lecture des réponses `201` de `POST /api/sessions` |
| ENF8 | Volumétrie cible : 5 promotions, 60 étudiants par promotion, 2 sessions par jour | Dimensionne ENF2 ; aucune pagination nécessaire dans cette version |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session. Passé ce délai, `POST /api/presences` renvoie `410 CODE_EXPIRE` | Q2 |
| RG2 | On ne peut plus marquer sa présence avec le code une fois la session clôturée, même dans les 15 minutes : `410 CODE_EXPIRE` | Q3 |
| RG3 | Un étudiant n'a qu'une seule présence par session. Une seconde tentative (code ou formateur) renvoie `409 DEJA_PRESENT` | Déduite de Q14 / contrat |
| RG4 | Un code qui ne correspond à aucune session renvoie `400 CODE_INCONNU`. Un code est unique parmi toutes les sessions | Contrat |
| RG5 | Après 5 codes inconnus consécutifs, l'étudiant est bloqué 2 minutes (`429 TROP_DE_TENTATIVES`). Le compteur est remis à zéro par une présence réussie ou à la fin du blocage | Q4 |
| RG6 | Un étudiant ne peut jamais relire son propre exercice : `403 AUTO_RELECTURE` | Q5 |
| RG7 | **(v2)** Un exercice déposé depuis le changement est relu par **deux étudiants différents** ; un exercice déposé avant garde sa relecture unique. Un étudiant n'est jamais affecté deux fois au même exercice | Enveloppe (remplace Q6), H13 |
| RG8 | Les relecteurs sont tirés au hasard par le système parmi les étudiants **présents à la session de l'exercice**, auteur exclu, sans remise | Q7 |
| RG9 | Tant qu'un exercice a moins de relecteurs que requis, l'affectation est retentée à chaque nouvelle présence enregistrée dans la session ; sans aucun relecteur, il est `EN_ATTENTE_RELECTEUR` | Hypothèse H4, H15 |
| RG10 | Une note est un entier compris entre 0 et 20 inclus. Sinon `400 NOTE_INVALIDE` | Q9 |
| RG11 | Une relecture rendue est définitive : un second envoi renvoie `409 RELECTURE_DEJA_RENDUE`. Toujours vrai en v2 : chaque relecture est individuelle | Q15 (contradiction avec Q10, voir §7) |
| RG12 | Un étudiant dépose au plus un exercice par session : `409 EXERCICE_DEJA_DEPOSE` | Contrat |
| RG13 | Le dépôt est possible après la fin du cours, jusqu'à la clôture de la session par le formateur ; ensuite `409 SESSION_CLOTUREE` | Q12 |
| ~~RG14~~ | ~~Remplacement du lien~~ — **sans objet en v2** (EF14 sacrifiée) | Q13 |
| RG15 | Une présence ajoutée par le formateur porte `source = FORMATEUR` ; par code, `source = ETUDIANT` | Q14 |
| RG16 | Le nom et l'identifiant **des** relecteurs ne sont jamais exposés à l'auteur de l'exercice | Q8 |
| RG17 | **(v2)** La moyenne d'un étudiant est la moyenne arithmétique des **notes retenues** de ses exercices ayant au moins une relecture rendue, arrondie à 2 décimales ; `null` sans note ; **provisoire** si l'une des notes retenues l'est. Calculée uniquement par l'API | Q16, F3, enveloppe |
| RG18 | Un lien est valide s'il est une URL absolue en `http` ou `https` de moins de 500 caractères ; sinon `400 LIEN_INVALIDE` | Contrat |
| RG19 | Un étudiant ne peut agir (présence, dépôt) que sur une session de sa propre promotion ; sinon `403 ETUDIANT_HORS_PROMOTION` | Hypothèse H6 |
| RG20 | Après clôture d'une session, plus aucune relecture de ses exercices ne peut être rendue (`409 SESSION_CLOTUREE`) ; les relectures non rendues restent visibles « en attente » dans le tableau | Q10, Q11 |
| RG21 | Une relecture assignée mais non rendue compte dans `relecturesEnAttente` du relecteur ; un exercice non `RELU` (y compris relu partiellement) apparaît en attente chez son auteur | Q11, Q16 |
| RG22 | **(v2)** La **note retenue** d'un exercice est la moyenne des notes de ses relectures rendues, arrondie à 2 décimales | Enveloppe |
| RG23 | **(v2)** La note retenue est **provisoire** tant que toutes les relectures requises ne sont pas rendues ; l'exercice est alors `RELU_PARTIELLEMENT`. Elle devient définitive à la dernière relecture (`RELU`) | Enveloppe |

## 7. Zones d'ombre, hypothèses et contradictions

**Points que la demande ne tranche pas :**

| # | Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|---|
| H1 | **Qui envoie la relecture ?** Sans authentification (Q1), `POST /api/relectures/{id}` ne dit pas qui relit : impossible de détecter une auto-relecture (`403` du contrat) | **Trou non vu par le client** | Le corps accepte un champ **optionnel** `relecteurId`. S'il est fourni et égal à l'auteur → `403 AUTO_RELECTURE` ; s'il est fourni et différent du relecteur assigné → `403 RELECTEUR_NON_ASSIGNE`. S'il est absent, on fait confiance à l'affectation (le système n'assigne jamais l'auteur, RG8) | Champ ajouté au contrat, sans casser le corps imposé `{note, commentaire}` |
| H2 | **Comment une session se termine-t-elle ?** Q3, Q10, Q12 parlent de « fin » et de « clôture », mais aucune opération n'existe | Aucune | La « fin de session » = la **clôture** explicite par le formateur (`POST /api/sessions/{id}/cloture`). L'expiration du code (15 min) ne ferme que la présence | Nouvelle opération au contrat, colonne `cloturee_at` |
| H3 | **Promotion.** Le contrat exige `promotionId`, le client n'en parle jamais | Aucune | Une promotion regroupe des étudiants ; chaque session appartient à une promotion. Promotions et étudiants sont fournis par les données de démonstration | Tables `promotion`, `etudiant.promotion_id` |
| H4 | **Aucun relecteur éligible** : l'auteur est seul présent, ou il n'y a encore personne (dépôt en début de session) | Q7 ne le prévoit pas | L'exercice reste `EN_ATTENTE_RELECTEUR` ; l'affectation est retentée à chaque nouvelle présence de la session (RG9) | Un statut de plus dans le cycle de vie (D4) |
| H5 | **« Personne n'a commencé à le relire » (Q13)** : on ne sait pas quand un relecteur « commence » | Q13 | Sans brouillon de relecture, « commencé » = relecture **rendue**. Le lien est remplaçable jusque-là | Le relecteur peut voir un lien changer avant de noter — risque accepté |
| H6 | Un étudiant d'une autre promotion saisit le code | Aucune | Refusé : `403 ETUDIANT_HORS_PROMOTION` (RG19) | Contrôle dans le service |
| H7 | Un étudiant **absent** peut-il déposer un exercice ? | Q12 ne le dit pas | Oui : le dépôt concerne le travail, pas la présence (un étudiant sans connexion a pu suivre à distance). En revanche il ne peut pas être relecteur de cette session (RG8) | Pas de contrôle de présence au dépôt |
| H8 | **Q16 vs contrat** : le client veut « sa présence à chaque session », le contrat impose `presences` entier | Q16 | On garde `presences` (nombre) comme imposé, et on **ajoute** à chaque ligne un tableau `detailPresences` (session par session, avec la source) et `exercicesEnAttente` pour Q11 | Champs ajoutés, aucun champ imposé modifié |
| H9 | Une session inexistante / un étudiant inexistant dans une requête | Contrat muet | `404 SESSION_INCONNUE` / `404 ETUDIANT_INCONNU` / `404 RELECTURE_INCONNUE` | Codes ajoutés au contrat |
| H10 | Le blocage de Q4 porte sur qui ? | Q4 | Sur l'**étudiant** (`etudiantId`), pas sur l'adresse IP : tous les étudiants partagent souvent le même Wi-Fi | Compteur en mémoire, perdu au redémarrage — acceptable |
| H11 | Répartition des relectures | Q7 « au hasard » | Tirage uniforme parmi les éligibles ; aucune règle d'équilibrage | Un étudiant peut avoir plusieurs relectures à faire |
| H12 | Promotion connue mais sans étudiant | Contrat | `200` avec une liste vide ; `404` seulement si la promotion n'existe pas | — |
| H13 | **(v2)** Que deviennent les exercices déjà relus par un seul pair ? | Enveloppe : « **à partir de maintenant** » | Ils gardent leur relecture unique et leur note définitive ; seuls les dépôts postérieurs demandent deux relecteurs. Colonne `exercice.relecteurs_requis` (1 pour l'existant, 2 ensuite) | La base déjà remplie survit à la migration V3 sans changer aucune note |
| H14 | **(v2)** Comment signaler une note provisoire ? | Enveloppe | Nouveau statut `RELU_PARTIELLEMENT` et champs `noteProvisoire` (exercice) / `moyenneProvisoire` (tableau) | Contrat mis à jour, champs imposés inchangés |
| H15 | **(v2)** Un seul présent éligible au moment du dépôt | Enveloppe muette | On affecte ce relecteur tout de suite, le second est tiré à la prochaine présence (RG9 étendue) | L'étudiant peut obtenir une note provisoire sans attendre |

**Contradictions relevées :**

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| **Q10** (« le relecteur peut corriger sa note tant que la session n'est pas clôturée ») **contre Q15** (« une fois validée, c'est fini, il ne peut plus y revenir ») | **Q15 : une relecture rendue est définitive** (RG11) | 1) Le contrat imposé prévoit `409 RELECTURE_DEJA_RENDUE` sur `POST /api/relectures/{id}` : il n'a de sens que si une relecture ne peut être rendue qu'une fois. 2) Q15 est justifiée par le client lui-même (« plus honnête pour tout le monde »), Q10 non. 3) Une note qui ne bouge plus rend la moyenne du tableau stable. Ce qui reste de Q10 : la clôture fige la session (RG20) |

**Changement de besoin (étape 3) :** la réponse à **Q6** (« un seul relecteur ») est **remplacée** par la nouvelle demande du client : deux relecteurs, note retenue = moyenne, provisoire s'il en manque une. Ce n'est pas une contradiction à trancher mais une décision plus récente du client : elle l'emporte. La contradiction Q10/Q15 reste tranchée pour Q15 (chaque relecture reste définitive).

**Questions sans impact sur la conception :** Q1 sert uniquement à sortir l'authentification du périmètre.

## 8. Contraintes techniques

Imposées par le sujet :

| # | Contrainte | Mise en œuvre prévue |
|---|---|---|
| B1 | Java 17+, Maven, wrapper `mvnw` commité | Java 17 (cible), Spring Boot 3, `mvnw` commité |
| B2 | Contrat `api/contrat.yaml` respecté à la lettre | Chemins, verbes, codes et format d'erreur vérifiés par les tests d'intégration |
| B3 | Couches contrôleur / service / repository, DTO, aucune entité JPA en JSON | Paquets `controller`, `service`, `repository`, `domain`, `dto` ; `record` Java pour les DTO |
| B4 | Validation (`jakarta.validation`) + `@RestControllerAdvice` | Exceptions métier typées → `{code, message}` ; gestionnaire de repli pour toute exception inattendue |
| B5 | Schéma versionné (Flyway), `ddl-auto=validate` | `src/main/resources/db/migration/V1__...sql`, jamais `update` |
| B6 | Un test unitaire sur une règle métier, un test d'intégration sur un endpoint, qui tournent sans base locale | JUnit 5 + Mockito (RG1, RG6, RG10…) ; `@SpringBootTest` + MockMvc sur H2 en mémoire |
| F1 | Framework déclaré et justifié, le build passe | React + Vite, `npm run build` |
| F2 | Trois écrans : formateur, étudiant, relecteur | Routes `/formateur`, `/etudiant`, `/relecteur` |
| F3 | Couche API dédiée, chargement/erreur gérés, aucune règle métier dupliquée | `src/api/client.js` unique ; la moyenne vient de l'API |

Contraintes que je m'impose :
- **Base de données :** PostgreSQL 16 (via Docker) en exécution ; H2 en mémoire (mode PostgreSQL) pour les tests, afin qu'ils tournent sur un poste vierge. Migrations SQL écrites pour être compatibles avec les deux.
- **Démarrage :** `docker compose up --build` (PostgreSQL + backend + frontend).
- **Données de démonstration :** migration Flyway dédiée (`V2__donnees_demo.sql`).
- **Horloge injectable** (`java.time.Clock`) pour tester l'expiration du code sans attendre 15 minutes.
- **Git :** une branche par issue (`feat/<n>-...`, `fix/<n>-...`), une PR par branche, messages de commit citant `EFx`/`RGx` et `#issue`.

## 9. Livrables

- Dépôt public `kfokam48-epreuve-KF48-YAO-252` avec la structure imposée (`/docs`, `/api`, `/backend`, `/frontend`) ;
- `docs/CAHIER_DES_CHARGES.md` (ce document), `docs/JOURNAL.md` ;
- `docs/diagrammes/` : D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 (bonus) états d'un exercice — en Mermaid ;
- `api/contrat.yaml` complété ;
- backlog en issues GitHub, PR liées ;
- backend Spring Boot avec migrations Flyway et tests ;
- frontend React avec les trois écrans ;
- `README.md` (installation testée depuis un clone vierge), `CHANGELOG.md` ;
- trois commits `[JALON] analyse`, `[JALON] v0.1`, `[JALON] v1.0` ;
- `SOUMISSION.md` téléversé sur la plateforme.

## 10. Démarche prévue

1. **Analyse (étape 1)** — ce cahier, les diagrammes, le contrat complété, les issues ; puis `[JALON] analyse`. Aucun code avant.
2. **v0.1 (étape 2)** — uniquement les issues **Must**, dans l'ordre des dépendances : socle backend (migrations + données démo) → sessions → présences → exercices + affectation → relectures → tableau → frontend. Une branche et une PR par issue, fusion dans `main` seulement si le build et les tests passent. Puis `[JALON] v0.1`.
3. **Enveloppe (étape 3)** — lire, ouvrir **d'abord** une issue pour le bug et une pour le changement, re-prioriser le backlog par écrit, reproduire le bug par un test, puis corriger ; l'évolution passe par une nouvelle migration et une mise à jour du contrat ; mise à jour de ce document et des diagrammes dans un commit dédié.
4. **v1.0 (étape 4)** — issues Should restantes si le temps le permet, `CHANGELOG.md`, `README` testé depuis un clone vierge, backlog restant trié ; `[JALON] v1.0`.
5. **Soumission (étape 5)** — relever le hash du dernier commit, vérifier le lien en navigation privée, téléverser avant 17h30.

*Mise à jour (sujet révisé, remis à 12h02) : l'épreuve Git séparée a été retirée du sujet ; il reste cinq étapes et un seul dépôt à déclarer.*

**Si je prends du retard :** je sacrifie dans l'ordre EF14 (Could), puis EF12 et EF13 (Should). Je ne sacrifie jamais les jalons, le journal, les tests ni le `README`.

**Re-priorisation écrite après l'enveloppe (v2) :** EF14 (#18) et EF13 (#17) sont **sacrifiées** ; EF11 (#15) devient Must et est absorbée par #35. Détail et justification dans le commentaire de l'issue #33 et dans le journal.

**Definition of Done — un ticket est terminé quand :**
- ses critères d'acceptation sont vérifiés (test automatisé ou vérification manuelle décrite dans la PR) ;
- le code est sur une branche dédiée, fusionné par une PR qui référence l'issue (`Closes #n`) ;
- `./mvnw verify` et `npm run build` passent sur `main` après la fusion ;
- le contrat d'API et, si besoin, les diagrammes sont à jour ;
- aucune stack trace, aucun fichier généré, aucun secret dans le commit.

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 25/09/2026, étape 1 | Version initiale |
| 2 | 25/09/2026, étape 3 | **Conséquence du changement de besoin** (deux relecteurs, note retenue, note provisoire) : EF6, EF8, EF9, EF11 revues ; RG7, RG8, RG9, RG16, RG17, RG21 révisées ; RG22, RG23 ajoutées ; RG14 sans objet ; H13–H15 ; EF13 et EF14 sacrifiées (issues #17, #18) |
