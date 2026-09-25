# D1 — Cas d'utilisation

Mermaid n'a pas de diagramme de cas d'utilisation natif : les acteurs sont des rectangles «acteur», les cas d'utilisation (ovales) dans le cadre du système. Chaque cas renvoie à son exigence `EFx`.

```mermaid
flowchart LR
    F["«acteur» Formateur"]
    E["«acteur» Étudiant"]
    R["«acteur» Relecteur<br/>= étudiant désigné"]
    S["«acteur» Système"]

    subgraph APP["Application Présence & Relecture"]
        UC1(["Ouvrir une session<br/>et obtenir le code — EF1"])
        UC2(["Ajouter une présence<br/>à la main — EF4"])
        UC3(["Clôturer une session — EF10"])
        UC4(["Consulter le tableau<br/>de la promotion — EF9, EF13"])
        UC5(["Se choisir dans la liste — EF2"])
        UC6(["Marquer sa présence<br/>avec le code — EF3"])
        UC7(["Déposer le lien<br/>de son exercice — EF5"])
        UC8(["Remplacer le lien — EF14"])
        UC9(["Voir sa note et<br/>son commentaire — EF11"])
        UC10(["Voir ses relectures<br/>à faire — EF7"])
        UC11(["Rendre une note<br/>et un commentaire — EF8"])
        UC12(["Affecter un relecteur<br/>au hasard — EF6"])
        UC13(["Bloquer après 5 codes<br/>erronés — EF12"])
        UC14(["Faire expirer le code<br/>après 15 min — RG1"])
    end

    F --- UC1
    F --- UC2
    F --- UC3
    F --- UC4
    E --- UC5
    E --- UC6
    E --- UC7
    E --- UC8
    E --- UC9
    R --- UC10
    R --- UC11
    S --- UC12
    S --- UC13
    S --- UC14

    UC7 -.->|«include»| UC12
    UC13 -.->|«extend» si 5 échecs| UC6
    R -.-|est un| E
```

**Lecture :**
- Le **relecteur n'est pas un acteur à part** : c'est un étudiant désigné par le système pour relire un exercice précis (voir cahier des charges §2). Le lien pointillé « est un » le rappelle.
- Déposer un exercice **inclut** l'affectation d'un relecteur (RG8) ; marquer sa présence est **étendu** par le blocage après cinq erreurs (RG5).
- Aucun cas « se connecter » : pas d'authentification (Q1).
