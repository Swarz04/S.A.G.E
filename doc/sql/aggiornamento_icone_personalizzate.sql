-- Aggiornamento incrementale per la scelta icone nelle Fonti e per le
-- immagini personalizzate trascinate dall'utente.
-- Eseguire su phpMyAdmin selezionando il database S.A.G.E.

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

SET @column_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'CATEGORIA'
      AND COLUMN_NAME = 'Icona'
);
SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE CATEGORIA ADD COLUMN Icona VARCHAR(255) NULL AFTER Nome',
    'ALTER TABLE CATEGORIA MODIFY COLUMN Icona VARCHAR(255) NULL'
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
    'ALTER TABLE TAG MODIFY COLUMN Icona VARCHAR(255) NULL'
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
    'ALTER TABLE FONTE MODIFY COLUMN Icona VARCHAR(255) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE CATEGORIA
SET Icona = 'generic_category.png'
WHERE Icona IS NULL OR TRIM(Icona) = '';

UPDATE TAG
SET Icona = 'generic_tag.png'
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
