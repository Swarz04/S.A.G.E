-- S.A.G.E. - migrazione cumulativa per database gia esistenti
-- Sostituisce i precedenti script separati per ricorrenze, icone e funzioni richieste.
-- Eseguire una sola volta su un database creato con una versione precedente.

-- Migrazione incrementale per un database S.A.G.E. già esistente.
-- Aggiunge:
--   1) nome alle spese ricorrenti;
--   2) collegamento tra ricorrenza e transazioni generate;
--   3) icona selezionabile per categorie, tag e fonti;
--   4) rimozione del tag Essenziale e blocco dei futuri duplicati.

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- MySQL Workbench puo avere SQL_SAFE_UPDATES attivo. La migrazione salva
-- l'impostazione della sessione, la disattiva solo durante gli aggiornamenti
-- massivi controllati e la ripristina al termine. Non vengono modificate
-- preferenze globali del server o di Workbench.
SET @sage_old_sql_safe_updates = @@SESSION.SQL_SAFE_UPDATES;
SET SESSION SQL_SAFE_UPDATES = 0;

-- ---------------------------------------------------------------------------
-- Nuove colonne
-- ---------------------------------------------------------------------------

SET @column_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'CATEGORIA'
      AND COLUMN_NAME = 'Icona'
);
SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE CATEGORIA ADD COLUMN Icona VARCHAR(255) NULL AFTER Nome',
    'SELECT ''CATEGORIA.Icona già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'TAG'
      AND COLUMN_NAME = 'Icona'
);
SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE TAG ADD COLUMN Icona VARCHAR(255) NULL AFTER Nome',
    'SELECT ''TAG.Icona già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'FONTE'
      AND COLUMN_NAME = 'Icona'
);
SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE FONTE ADD COLUMN Icona VARCHAR(255) NULL AFTER Nome',
    'SELECT ''FONTE.Icona già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Allargo i campi per supportare anche riferimenti a icone personalizzate.
ALTER TABLE CATEGORIA MODIFY COLUMN Icona VARCHAR(255) NULL;
ALTER TABLE TAG MODIFY COLUMN Icona VARCHAR(255) NULL;
ALTER TABLE FONTE MODIFY COLUMN Icona VARCHAR(255) NULL;

SET @column_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SPESA_RICORRENTE'
      AND COLUMN_NAME = 'Nome'
);
SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE SPESA_RICORRENTE ADD COLUMN Nome VARCHAR(100) NOT NULL DEFAULT ''Spesa ricorrente'' AFTER ID_Ricorrenza',
    'SELECT ''SPESA_RICORRENTE.Nome già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'TRANSIZIONE'
      AND COLUMN_NAME = 'ID_Ricorrenza'
);
SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE TRANSIZIONE ADD COLUMN ID_Ricorrenza INT NULL AFTER ID_Fonte',
    'SELECT ''TRANSIZIONE.ID_Ricorrenza già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Assegno nomi leggibili alle ricorrenze già presenti.
UPDATE SPESA_RICORRENTE SR
JOIN CATEGORIA C ON C.ID_Categoria = SR.ID_Categoria
SET SR.Nome = CONCAT('Spesa ricorrente - ', C.Nome)
WHERE SR.Nome IS NULL
   OR TRIM(SR.Nome) = ''
   OR SR.Nome = 'Spesa ricorrente';

-- Icone iniziali per gli elementi già presenti. Le nuove icone vengono scelte
-- dall'interfaccia e salvate direttamente nelle colonne Icona.
UPDATE CATEGORIA
SET Icona = CASE LOWER(TRIM(Nome))
    WHEN 'casa' THEN 'house.png'
    WHEN 'alimentari' THEN 'food.png'
    WHEN 'mensa' THEN 'food.png'
    WHEN 'mensa universitaria' THEN 'food.png'
    WHEN 'trasporti' THEN 'transport.png'
    WHEN 'salute' THEN 'health.png'
    WHEN 'svago' THEN 'leisure.png'
    WHEN 'palestra' THEN 'gym.png'
    WHEN 'libri' THEN 'study.png'
    WHEN 'libri universitari' THEN 'study.png'
    ELSE 'generic_category.png'
END
WHERE Icona IS NULL OR TRIM(Icona) = '';

UPDATE TAG
SET Icona = CASE LOWER(TRIM(Nome))
    WHEN 'università' THEN 'study.png'
    WHEN 'universita' THEN 'study.png'
    WHEN 'studio' THEN 'study.png'
    WHEN 'esame' THEN 'study.png'
    WHEN 'extra' THEN 'leisure.png'
    WHEN 'weekend' THEN 'leisure.png'
    WHEN 'urgente' THEN 'urgent.png'
    ELSE 'generic_tag.png'
END
WHERE Icona IS NULL OR TRIM(Icona) = '';

UPDATE FONTE
SET Icona = CASE LOWER(TRIM(Nome))
    WHEN 'stipendio' THEN 'salary.png'
    WHEN 'borsa di studio' THEN 'scholarship.png'
    WHEN 'regalo' THEN 'gift.png'
    WHEN 'rimborso' THEN 'refund.png'
    WHEN 'ripetizioni private' THEN 'tutoring.png'
    WHEN 'lavoretto weekend' THEN 'work.png'
    WHEN 'aiuto famiglia' THEN 'family.png'
    ELSE 'generic_source.png'
END
WHERE Icona IS NULL OR TRIM(Icona) = '';

-- Essenziale non deve esistere né come tag di sistema né come tag personale.
-- Le associazioni in SPESA_TAG vengono eliminate in cascata dalla FK.
DELETE FROM TAG
WHERE LOWER(TRIM(Nome)) = 'essenziale';

-- ---------------------------------------------------------------------------
-- Vincoli e indice per la relazione ricorrenza -> transazioni
-- ---------------------------------------------------------------------------

SET @fk_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'TRANSIZIONE'
      AND CONSTRAINT_NAME = 'FK_TRANSIZIONE_RICORRENZA'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql = IF(
    @fk_exists = 0,
    'ALTER TABLE TRANSIZIONE ADD CONSTRAINT FK_TRANSIZIONE_RICORRENZA FOREIGN KEY (ID_Ricorrenza) REFERENCES SPESA_RICORRENTE (ID_Ricorrenza) ON UPDATE CASCADE ON DELETE SET NULL',
    'SELECT ''FK_TRANSIZIONE_RICORRENZA già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'TRANSIZIONE'
      AND INDEX_NAME = 'UQ_TRANSIZIONE_RICORRENZA_DATA'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE UNIQUE INDEX UQ_TRANSIZIONE_RICORRENZA_DATA ON TRANSIZIONE (ID_Ricorrenza, Data)',
    'SELECT ''UQ_TRANSIZIONE_RICORRENZA_DATA già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'DOCUMENTO'
      AND INDEX_NAME = 'UQ_DOCUMENTO_TRANSIZIONE'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE UNIQUE INDEX UQ_DOCUMENTO_TRANSIZIONE ON DOCUMENTO (ID_Transizione)',
    'SELECT ''UQ_DOCUMENTO_TRANSIZIONE gia presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @check_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'TRANSIZIONE'
      AND CONSTRAINT_NAME = 'CK_TRANSIZIONE_RICORRENZA'
      AND CONSTRAINT_TYPE = 'CHECK'
);
SET @sql = IF(
    @check_exists = 0,
    'ALTER TABLE TRANSIZIONE ADD CONSTRAINT CK_TRANSIZIONE_RICORRENZA CHECK (ID_Ricorrenza IS NULL OR TipoTransazione = ''S'')',
    'SELECT ''CK_TRANSIZIONE_RICORRENZA già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SPESA_RICORRENTE'
      AND INDEX_NAME = 'IDX_RICORRENTE_EMAIL_SCADENZA'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE INDEX IDX_RICORRENTE_EMAIL_SCADENZA ON SPESA_RICORRENTE (Email, Data_Prossima_Scadenza)',
    'SELECT ''IDX_RICORRENTE_EMAIL_SCADENZA già presente'' AS Messaggio'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- Trigger anti-duplicato per categorie, tag e fonti
-- ---------------------------------------------------------------------------

DROP TRIGGER IF EXISTS trg_categoria_insert_no_duplicati;
DROP TRIGGER IF EXISTS trg_categoria_update_no_duplicati;
DROP TRIGGER IF EXISTS trg_tag_insert_no_duplicati;
DROP TRIGGER IF EXISTS trg_tag_update_no_duplicati;
DROP TRIGGER IF EXISTS trg_fonte_insert_no_duplicati;
DROP TRIGGER IF EXISTS trg_fonte_update_no_duplicati;
DROP TRIGGER IF EXISTS trg_transizione_insert_check;
DROP TRIGGER IF EXISTS trg_transizione_update_check;

DELIMITER $$

CREATE TRIGGER trg_categoria_insert_no_duplicati
BEFORE INSERT ON CATEGORIA
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM CATEGORIA C
        WHERE LOWER(TRIM(C.Nome)) = LOWER(TRIM(NEW.Nome))
          AND (
                NEW.is_system = TRUE
                OR
                (NEW.is_system = FALSE AND (
                    C.is_system = TRUE
                    OR (C.is_system = FALSE
                        AND C.Email_Proprietario = NEW.Email_Proprietario)
                ))
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esiste già una categoria con questo nome';
    END IF;
END$$

CREATE TRIGGER trg_categoria_update_no_duplicati
BEFORE UPDATE ON CATEGORIA
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM CATEGORIA C
        WHERE C.ID_Categoria <> NEW.ID_Categoria
          AND LOWER(TRIM(C.Nome)) = LOWER(TRIM(NEW.Nome))
          AND (
                NEW.is_system = TRUE
                OR
                (NEW.is_system = FALSE AND (
                    C.is_system = TRUE
                    OR (C.is_system = FALSE
                        AND C.Email_Proprietario = NEW.Email_Proprietario)
                ))
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esiste già una categoria con questo nome';
    END IF;
END$$

CREATE TRIGGER trg_tag_insert_no_duplicati
BEFORE INSERT ON TAG
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM TAG T
        WHERE LOWER(TRIM(T.Nome)) = LOWER(TRIM(NEW.Nome))
          AND (
                NEW.is_system = TRUE
                OR
                (NEW.is_system = FALSE AND (
                    T.is_system = TRUE
                    OR (T.is_system = FALSE
                        AND T.Email_Proprietario = NEW.Email_Proprietario)
                ))
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esiste già un tag con questo nome';
    END IF;
END$$

CREATE TRIGGER trg_tag_update_no_duplicati
BEFORE UPDATE ON TAG
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM TAG T
        WHERE T.ID_Tag <> NEW.ID_Tag
          AND LOWER(TRIM(T.Nome)) = LOWER(TRIM(NEW.Nome))
          AND (
                NEW.is_system = TRUE
                OR
                (NEW.is_system = FALSE AND (
                    T.is_system = TRUE
                    OR (T.is_system = FALSE
                        AND T.Email_Proprietario = NEW.Email_Proprietario)
                ))
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esiste già un tag con questo nome';
    END IF;
END$$

CREATE TRIGGER trg_fonte_insert_no_duplicati
BEFORE INSERT ON FONTE
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM FONTE F
        WHERE LOWER(TRIM(F.Nome)) = LOWER(TRIM(NEW.Nome))
          AND (
                NEW.is_system = TRUE
                OR
                (NEW.is_system = FALSE AND (
                    F.is_system = TRUE
                    OR (F.is_system = FALSE
                        AND F.Email_Proprietario = NEW.Email_Proprietario)
                ))
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esiste gia'' una fonte con questo nome';
    END IF;
END$$

CREATE TRIGGER trg_fonte_update_no_duplicati
BEFORE UPDATE ON FONTE
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM FONTE F
        WHERE F.ID_Fonte <> NEW.ID_Fonte
          AND LOWER(TRIM(F.Nome)) = LOWER(TRIM(NEW.Nome))
          AND (
                NEW.is_system = TRUE
                OR
                (NEW.is_system = FALSE AND (
                    F.is_system = TRUE
                    OR (F.is_system = FALSE
                        AND F.Email_Proprietario = NEW.Email_Proprietario)
                ))
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Esiste gia'' una fonte con questo nome';
    END IF;
END$$

-- Ricreo i trigger sulle transazioni aggiungendo la validazione del riferimento
-- alla ricorrenza. Mantengo anche i controlli originali su categoria e fonte.
CREATE TRIGGER trg_transizione_insert_check
BEFORE INSERT ON TRANSIZIONE
FOR EACH ROW
BEGIN
    IF NEW.TipoTransazione = 'S' THEN
        IF NOT EXISTS (
            SELECT 1 FROM CATEGORIA C
            WHERE C.ID_Categoria = NEW.ID_Categoria
              AND (C.is_system = TRUE OR C.Email_Proprietario = NEW.Email)
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Categoria non valida per questo utente';
        END IF;
    END IF;

    IF NEW.TipoTransazione = 'E' THEN
        IF NOT EXISTS (
            SELECT 1 FROM FONTE F
            WHERE F.ID_Fonte = NEW.ID_Fonte
              AND (F.is_system = TRUE OR F.Email_Proprietario = NEW.Email)
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Fonte non valida per questo utente';
        END IF;
    END IF;

    IF NEW.ID_Ricorrenza IS NOT NULL THEN
        IF NEW.TipoTransazione <> 'S' OR NOT EXISTS (
            SELECT 1 FROM SPESA_RICORRENTE SR
            WHERE SR.ID_Ricorrenza = NEW.ID_Ricorrenza
              AND SR.Email = NEW.Email
              AND SR.ID_Categoria = NEW.ID_Categoria
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Ricorrenza non coerente con la transazione';
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_transizione_update_check
BEFORE UPDATE ON TRANSIZIONE
FOR EACH ROW
BEGIN
    IF NEW.TipoTransazione = 'S' THEN
        IF NOT EXISTS (
            SELECT 1 FROM CATEGORIA C
            WHERE C.ID_Categoria = NEW.ID_Categoria
              AND (C.is_system = TRUE OR C.Email_Proprietario = NEW.Email)
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Categoria non valida per questo utente';
        END IF;
    END IF;

    IF NEW.TipoTransazione = 'E' THEN
        IF NOT EXISTS (
            SELECT 1 FROM FONTE F
            WHERE F.ID_Fonte = NEW.ID_Fonte
              AND (F.is_system = TRUE OR F.Email_Proprietario = NEW.Email)
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Fonte non valida per questo utente';
        END IF;
    END IF;

    IF NEW.ID_Ricorrenza IS NOT NULL THEN
        IF NEW.TipoTransazione <> 'S' OR NOT EXISTS (
            SELECT 1 FROM SPESA_RICORRENTE SR
            WHERE SR.ID_Ricorrenza = NEW.ID_Ricorrenza
              AND SR.Email = NEW.Email
              AND SR.ID_Categoria = NEW.ID_Categoria
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Ricorrenza non coerente con la transazione';
        END IF;
    END IF;
END$$

DELIMITER ;

-- ---------------------------------------------------------------------------
-- Viste aggiornate
-- ---------------------------------------------------------------------------

CREATE OR REPLACE VIEW v_transizioni_dettaglio AS
SELECT
    T.ID_Transizione,
    T.Email,
    CASE
        WHEN T.TipoTransazione = 'S' THEN 'Spesa'
        WHEN T.TipoTransazione = 'E' THEN 'Entrata'
    END AS Tipo,
    T.Importo,
    T.Data,
    T.Descrizione,
    T.ID_Ricorrenza,
    SR.Nome AS Nome_Ricorrenza,
    P.Mese,
    P.Anno,
    C.Nome AS Categoria,
    F.Nome AS Fonte,
    D.Path_File AS Documento
FROM TRANSIZIONE T
JOIN PERIODO P ON T.ID_Periodo = P.ID_Periodo
LEFT JOIN CATEGORIA C ON T.ID_Categoria = C.ID_Categoria
LEFT JOIN FONTE F ON T.ID_Fonte = F.ID_Fonte
LEFT JOIN SPESA_RICORRENTE SR ON T.ID_Ricorrenza = SR.ID_Ricorrenza
LEFT JOIN DOCUMENTO D ON T.ID_Transizione = D.ID_Transizione;

CREATE OR REPLACE VIEW v_spese_ricorrenti_scadute AS
SELECT
    SR.ID_Ricorrenza,
    SR.Nome,
    SR.Email,
    SR.Importo_Previsto,
    SR.Frequenza_Giorni,
    SR.Data_Inizio,
    SR.Data_Prossima_Scadenza,
    SR.Scadenza,
    SR.ID_Categoria,
    C.Nome AS Categoria
FROM SPESA_RICORRENTE SR
JOIN CATEGORIA C ON SR.ID_Categoria = C.ID_Categoria
WHERE SR.Data_Prossima_Scadenza <= CURRENT_DATE
  AND (SR.Scadenza IS NULL OR SR.Data_Prossima_Scadenza <= SR.Scadenza);

-- Ripristino della modalita safe update al valore presente prima della migrazione.
SET SESSION SQL_SAFE_UPDATES = @sage_old_sql_safe_updates;
