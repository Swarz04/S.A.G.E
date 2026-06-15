-- Aggiornamento incrementale per chi ha gia importato il database S.A.G.E.
-- Non aggiorna XAMPP: aggiorna solo schema/viste del database esistente.
-- Per le modifiche complete più recenti usare aggiornamento_funzioni_richieste.sql.

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

SET @nome_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SPESA_RICORRENTE'
      AND COLUMN_NAME = 'Nome'
);
SET @nome_sql = IF(
    @nome_exists = 0,
    'ALTER TABLE SPESA_RICORRENTE ADD COLUMN Nome VARCHAR(100) NOT NULL DEFAULT ''Spesa ricorrente'' AFTER ID_Ricorrenza',
    'SELECT ''Colonna SPESA_RICORRENTE.Nome gia presente'' AS Messaggio'
);
PREPARE stmt FROM @nome_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SPESA_RICORRENTE'
      AND INDEX_NAME = 'IDX_RICORRENTE_EMAIL_SCADENZA'
);
SET @idx_sql = IF(
    @idx_exists = 0,
    'CREATE INDEX IDX_RICORRENTE_EMAIL_SCADENZA ON SPESA_RICORRENTE (Email, Data_Prossima_Scadenza)',
    'SELECT ''Indice IDX_RICORRENTE_EMAIL_SCADENZA gia presente'' AS Messaggio'
);
PREPARE stmt FROM @idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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
