-- V2 — Données de démonstration (ENF5), pour que le correcteur n'ouvre pas une application vide.
-- Les dates sont relatives au moment de la migration.
--
-- Promotion Yaoundé 2026 (id 1) : 6 étudiants
--   Session 1 « Introduction à Spring Boot » : il y a 7 jours, CLÔTURÉE
--     présents : 1, 2, 3, 4, 5 (5 ajouté par le formateur, Q14) — 6 absent
--     exercices : 1 relu 15/20 · 2 relu 12/20 · 3 relu 18/20 · 4 jamais relu par 5 (reste en attente, Q11)
--   Session 2 « API REST et validation » : hier, NON clôturée (code expiré)
--     présents : 1, 2, 3, 4, 6
--     exercices : 1 en attente (relecteur 6) · 6 relu 14/20 · 5 absent mais a déposé (H7), relecteur 2
-- Promotion Douala 2026 (id 2) : 3 étudiants
--   Session 3 « Bases SQL » : il y a 2 jours, NON clôturée — seul l'étudiant 7 est présent :
--     son exercice n'a aucun relecteur éligible → EN_ATTENTE_RELECTEUR (RG9, H4)

INSERT INTO promotion (id, nom) VALUES
    (1, 'Promotion Yaoundé 2026'),
    (2, 'Promotion Douala 2026');

INSERT INTO etudiant (id, nom, promotion_id) VALUES
    (1, 'AMOUGOU Brice',   1),
    (2, 'BELINGA Carine',  1),
    (3, 'ESSOMBA Hervé',   1),
    (4, 'FOTSO Mireille',  1),
    (5, 'MBARGA Paul',     1),
    (6, 'NGONO Estelle',   1),
    (7, 'DIKOUME Serge',   2),
    (8, 'EKWALLA Rita',    2),
    (9, 'NJOH Alain',      2);

INSERT INTO session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at, cloturee_at) VALUES
    (1, 'Introduction à Spring Boot', 1, 'HSTA2B',
        CURRENT_TIMESTAMP - INTERVAL '7' DAY,
        CURRENT_TIMESTAMP - INTERVAL '7' DAY + INTERVAL '15' MINUTE,
        CURRENT_TIMESTAMP - INTERVAL '6' DAY),
    (2, 'API REST et validation', 1, 'HSTC3D',
        CURRENT_TIMESTAMP - INTERVAL '1' DAY,
        CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '15' MINUTE,
        NULL),
    (3, 'Bases SQL', 2, 'HSTE4F',
        CURRENT_TIMESTAMP - INTERVAL '2' DAY,
        CURRENT_TIMESTAMP - INTERVAL '2' DAY + INTERVAL '15' MINUTE,
        NULL);

INSERT INTO presence (id, session_id, etudiant_id, source, marquee_at) VALUES
    (1,  1, 1, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (2,  1, 2, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (3,  1, 3, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (4,  1, 4, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (5,  1, 5, 'FORMATEUR', CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (6,  2, 1, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    (7,  2, 2, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    (8,  2, 3, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    (9,  2, 4, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    (10, 2, 6, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    (11, 3, 7, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '2' DAY);

INSERT INTO exercice (id, session_id, etudiant_id, lien, statut, depose_at) VALUES
    (1, 1, 1, 'https://github.com/amougou/spring-intro',   'RELU',                 CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (2, 1, 2, 'https://github.com/belinga/spring-intro',   'RELU',                 CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (3, 1, 3, 'https://github.com/essomba/spring-intro',   'RELU',                 CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (4, 1, 4, 'https://github.com/fotso/spring-intro',     'EN_ATTENTE_RELECTURE', CURRENT_TIMESTAMP - INTERVAL '7' DAY),
    (5, 2, 1, 'https://github.com/amougou/api-rest',       'EN_ATTENTE_RELECTURE', CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    (6, 2, 6, 'https://github.com/ngono/api-rest',         'RELU',                 CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    (7, 2, 5, 'https://github.com/mbarga/api-rest',        'EN_ATTENTE_RELECTURE', CURRENT_TIMESTAMP - INTERVAL '1' DAY),
    (8, 3, 7, 'https://github.com/dikoume/bases-sql',      'EN_ATTENTE_RELECTEUR', CURRENT_TIMESTAMP - INTERVAL '2' DAY);

INSERT INTO relecture (id, exercice_id, relecteur_id, note, commentaire, assignee_at, rendue_at) VALUES
    (1, 1, 2, 15,   'Code propre, il manque des tests.',            CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY - INTERVAL '2' HOUR),
    (2, 2, 3, 12,   'Fonctionne, mais contrôleur trop chargé.',     CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY - INTERVAL '3' HOUR),
    (3, 3, 1, 18,   'Très bon travail, couches bien séparées.',     CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '6' DAY - INTERVAL '4' HOUR),
    (4, 4, 5, NULL, NULL,                                          CURRENT_TIMESTAMP - INTERVAL '7' DAY, NULL),
    (5, 5, 6, NULL, NULL,                                          CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL),
    (6, 6, 4, 14,   'Validation complète, bonnes réponses 400.',    CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '20' HOUR),
    (7, 7, 2, NULL, NULL,                                          CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL);

-- Les identifiants ci-dessus sont explicites : on repousse les compteurs pour que l'API
-- ne crée jamais une ligne avec un identifiant déjà pris.
ALTER TABLE promotion     ALTER COLUMN id RESTART WITH 100;
ALTER TABLE etudiant      ALTER COLUMN id RESTART WITH 100;
ALTER TABLE session_cours ALTER COLUMN id RESTART WITH 100;
ALTER TABLE presence      ALTER COLUMN id RESTART WITH 100;
ALTER TABLE exercice      ALTER COLUMN id RESTART WITH 100;
ALTER TABLE relecture     ALTER COLUMN id RESTART WITH 100;
