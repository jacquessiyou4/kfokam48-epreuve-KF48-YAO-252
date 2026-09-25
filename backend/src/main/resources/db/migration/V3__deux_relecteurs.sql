-- V3 — Changement de besoin de l'étape 3 : chaque exercice est relu par deux pairs différents.
-- Conforme à D2 v2. V1 et V2 ne sont pas modifiées ; une base déjà remplie survit :
-- les exercices existants gardent leur relecture unique et leur note (H13, « à partir de maintenant »).

-- Nombre de relectures requises par exercice : 2 désormais, 1 pour tout ce qui existe déjà.
ALTER TABLE exercice ADD COLUMN relecteurs_requis INTEGER DEFAULT 2 NOT NULL;
UPDATE exercice SET relecteurs_requis = 1;
ALTER TABLE exercice ADD CONSTRAINT ck_exercice_relecteurs_requis CHECK (relecteurs_requis BETWEEN 1 AND 2);

-- Nouvel état : une relecture rendue sur deux, note retenue provisoire (D4 v2, RG23).
ALTER TABLE exercice DROP CONSTRAINT ck_exercice_statut;
ALTER TABLE exercice ADD CONSTRAINT ck_exercice_statut
    CHECK (statut IN ('EN_ATTENTE_RELECTEUR', 'EN_ATTENTE_RELECTURE', 'RELU_PARTIELLEMENT', 'RELU'));

-- RG7 v2 : plusieurs relectures par exercice, jamais deux fois par le même étudiant.
-- La clé étrangère est retirée puis reposée : sous H2 elle s'appuie sur l'index unique de V1,
-- qui ne disparaîtrait pas sinon.
ALTER TABLE relecture DROP CONSTRAINT fk_relecture_exercice;
ALTER TABLE relecture DROP CONSTRAINT uk_relecture_exercice;
ALTER TABLE relecture ADD CONSTRAINT uk_relecture_exercice_relecteur UNIQUE (exercice_id, relecteur_id);
ALTER TABLE relecture ADD CONSTRAINT fk_relecture_exercice FOREIGN KEY (exercice_id) REFERENCES exercice (id);
