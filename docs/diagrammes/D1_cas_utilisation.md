# D1 — Cas d'utilisation

Mermaid n'a pas de diagramme de cas d'utilisation natif. Conventions : les **acteurs** sont des rectangles «acteur», les **cas d'utilisation** sont les ovales — tous appartiennent au système « Application Présence & Relecture ». Chaque cas renvoie à son exigence `EFx`. Les acteurs humains sont à gauche, l'acteur **Système** (traitements automatiques) à droite.

```mermaid
flowchart LR
    F["«acteur»<br/>Formateur"]
    E["«acteur»<br/>Étudiant"]
    R["«acteur»<br/>Relecteur<br/>(étudiant désigné)"]
    S["«acteur»<br/>Système"]

        UC1(["Ouvrir une session<br/>et obtenir le code<br/>EF1"])
        UC2(["Ajouter une présence<br/>à la main<br/>EF4"])
        UC3(["Clôturer<br/>une session<br/>EF10"])
        UC4(["Consulter<br/>le tableau<br/>EF9 · EF13"])
        UC5(["Se choisir<br/>dans la liste<br/>EF2"])
        UC6(["Marquer sa présence<br/>avec le code<br/>EF3"])
        UC7(["Déposer le lien<br/>de son exercice<br/>EF5"])
        UC9(["Voir sa note<br/>et son commentaire<br/>EF11"])
        UC10(["Voir ses relectures<br/>à faire<br/>EF7"])
        UC11(["Rendre une note<br/>et un commentaire<br/>EF8"])
        UC12(["Affecter deux relecteurs<br/>au hasard (v2)<br/>EF6"])
        UC13(["Bloquer après<br/>5 codes erronés<br/>EF12"])
        UC14(["Faire expirer le code<br/>après 15 min<br/>RG1"])

    F --- UC1
    F --- UC2
    F --- UC3
    F --- UC4
    E --- UC5
    E --- UC6
    E --- UC7
    E --- UC9
    R --- UC10
    R --- UC11
    UC6 -.-|«extend»| UC13
    UC7 -.->|«include»| UC12
    UC12 --- S
    UC13 --- S
    UC14 --- S
```

**Lecture :**
- Le **relecteur n'est pas un acteur à part** : c'est un étudiant désigné par le système pour relire un exercice précis (voir cahier des charges §2). Il est dessiné séparément parce que ses cas d'utilisation n'ont de sens que dans cet état.
- « Déposer le lien » **inclut** toujours l'affectation des relecteurs — **deux** depuis le changement de besoin de l'étape 3 (RG7, RG8) : flèche «include» de EF5 vers EF6.
- « Bloquer après 5 codes erronés » **étend** « Marquer sa présence » (RG5), seulement après 5 échecs : la relation «extend» va de EF12 vers EF3 (le trait pointillé est sans pointe pour garder un rendu lisible).
- Aucun cas « se connecter » : pas d'authentification (Q1).
- **v2 :** « Remplacer le lien » (EF14) a été retiré, sacrifié pour absorber le changement de besoin (issue #18).
