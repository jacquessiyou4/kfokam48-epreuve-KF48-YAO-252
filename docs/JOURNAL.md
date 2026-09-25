# Journal de bord — KF48-YAO-252

> Une entrée **par étape**, écrite **au moment où je la termine**.

Chaque entrée répond aux trois mêmes questions :

- **Fait** — ce que je viens de terminer
- **Bloqué** — ce qui m'a coûté du temps, et combien
- **IA** — ce que je lui ai demandé, et **comment j'ai vérifié sa réponse**

---

## Étape 1 — Analyse et conception (09h35 → 10h05)

**Fait :** cahier des charges v1 (14 EF, 8 ENF, 21 RG, 12 hypothèses H1–H12, contradiction Q10/Q15 tranchée), quatre diagrammes Mermaid (D1 cas d'utilisation, D2 modèle de données, D3 séquence « marquer sa présence », D4 bonus états d'un exercice), contrat d'API complété en v1.1 (8 opérations ajoutées ; le message du commit 5c4de7c en annonçait 7 par erreur, corrigé par un commit dédié sans réécrire `main`), 18 issues créées avec labels Must (12) / Should (5) / Could (1). `.gitignore` posé avant tout code.

**Bloqué :** ~10 min sur Q10 contre Q15, tranchée pour Q15 : le contrat imposé renvoie `409 RELECTURE_DEJA_RENDUE`, ce qui n'a de sens que si une relecture est définitive. ~5 min à comprendre que `POST /api/relectures/{id}` ne dit pas qui relit (pas d'authentification, Q1) : c'est le trou du sujet, résolu par un champ optionnel `relecteurId` (H1). ~5 min sur le rendu du diagramme D1, trop gros pour le validateur en ligne.

**IA :** utilisée pour rédiger le cahier des charges, les diagrammes, le contrat et les issues. Vérifications : chaque RG relue contre la question `Qx` citée ; chaque code HTTP de D3 recoupé avec le contrat (tableau de correspondance en bas de D3) ; les tables et contraintes de D2 serviront telles quelles pour la migration V1 ; contrat validé par `redocly lint` (0 erreur) ; diagrammes D2, D3, D4 validés par le rendu Mermaid, D1 validé sur une version réduite contenant toutes ses constructions ; numéros d'issues vérifiés après création pour que les renvois `#4`, `#7`, `#9` pointent au bon endroit.

---

## Étape 2 — Première version

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 3 — Enveloppe

**Fait :**

**Bloqué :**

**IA :**

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :**

---

## Étape 4 — Version finale

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 5 — Épreuve Git

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 6 — Soumission

**Fait :**

**Ce que je referais autrement avec une journée de plus :**
