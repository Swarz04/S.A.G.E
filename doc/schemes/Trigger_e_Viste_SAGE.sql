-- =====================================================
-- SEGMENTO 02: TRIGGER E VISTE
-- =====================================================

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- ======================
-- TRIGGER
-- ======================

DELIMITER $$

-- Controllo categoria/fonte nelle transazioni in inserimento
CREATE TRIGGER trg_transizione_insert_check
BEFORE INSERT ON TRANSIZIONE
FOR EACH ROW
BEGIN
    IF NEW.TipoTransazione = 'S' THEN
        IF NOT EXISTS (
            SELECT 1
            FROM CATEGORIA C
            WHERE C.ID_Categoria = NEW.ID_Categoria
              AND (
                    C.is_system = TRUE
                    OR C.Email_Proprietario = NEW.Email
                  )
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Categoria non valida per questo utente';
        END IF;
    END IF;

    IF NEW.TipoTransazione = 'E' THEN
        IF NOT EXISTS (
            SELECT 1
            FROM FONTE F
            WHERE F.ID_Fonte = NEW.ID_Fonte
              AND (
                    F.is_system = TRUE
                    OR F.Email_Proprietario = NEW.Email
                  )
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Fonte non valida per questo utente';
        END IF;
    END IF;
END$$

-- Controllo categoria/fonte nelle transazioni in modifica
CREATE TRIGGER trg_transizione_update_check
BEFORE UPDATE ON TRANSIZIONE
FOR EACH ROW
BEGIN
    IF NEW.TipoTransazione = 'S' THEN
        IF NOT EXISTS (
            SELECT 1
            FROM CATEGORIA C
            WHERE C.ID_Categoria = NEW.ID_Categoria
              AND (
                    C.is_system = TRUE
                    OR C.Email_Proprietario = NEW.Email
                  )
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Categoria non valida per questo utente';
        END IF;
    END IF;

    IF NEW.TipoTransazione = 'E' THEN
        IF NOT EXISTS (
            SELECT 1
            FROM FONTE F
            WHERE F.ID_Fonte = NEW.ID_Fonte
              AND (
                    F.is_system = TRUE
                    OR F.Email_Proprietario = NEW.Email
                  )
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Fonte non valida per questo utente';
        END IF;
    END IF;
END$$

-- Controllo validità dei tag associati alle spese in inserimento
CREATE TRIGGER trg_spesatag_insert_check
BEFORE INSERT ON spesa_tag
FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM TRANSIZIONE T
        JOIN TAG TA ON TA.ID_Tag = NEW.ID_Tag
        WHERE T.ID_Transizione = NEW.ID_Transizione
          AND T.TipoTransazione = 'S'
          AND (
                TA.is_system = TRUE
                OR TA.Email_Proprietario = T.Email
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tag non valido oppure transizione non di tipo spesa';
    END IF;
END$$

-- Controllo validità dei tag associati alle spese in modifica
CREATE TRIGGER trg_spesatag_update_check
BEFORE UPDATE ON spesa_tag
FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM TRANSIZIONE T
        JOIN TAG TA ON TA.ID_Tag = NEW.ID_Tag
        WHERE T.ID_Transizione = NEW.ID_Transizione
          AND T.TipoTransazione = 'S'
          AND (
                TA.is_system = TRUE
                OR TA.Email_Proprietario = T.Email
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tag non valido oppure transizione non di tipo spesa';
    END IF;
END$$

-- Controllo documento associabile solo a una spesa in inserimento
CREATE TRIGGER trg_documento_solo_spese_insert
BEFORE INSERT ON DOCUMENTO
FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM TRANSIZIONE
        WHERE ID_Transizione = NEW.ID_Transizione
          AND TipoTransazione = 'S'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Un documento può essere associato solo a una spesa';
    END IF;
END$$

-- Controllo documento associabile solo a una spesa in modifica
CREATE TRIGGER trg_documento_solo_spese_update
BEFORE UPDATE ON DOCUMENTO
FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM TRANSIZIONE
        WHERE ID_Transizione = NEW.ID_Transizione
          AND TipoTransazione = 'S'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Un documento può essere associato solo a una spesa';
    END IF;
END$$

-- Controllo categoria valida nei budget in inserimento
CREATE TRIGGER trg_budget_insert_check
BEFORE INSERT ON BUDGET
FOR EACH ROW
BEGIN
    IF NEW.ID_Categoria IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM CATEGORIA C
            WHERE C.ID_Categoria = NEW.ID_Categoria
              AND (
                    C.is_system = TRUE
                    OR C.Email_Proprietario = NEW.Email
                  )
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Categoria del budget non valida per questo utente';
        END IF;
    END IF;
END$$

-- Controllo categoria valida nei budget in modifica
CREATE TRIGGER trg_budget_update_check
BEFORE UPDATE ON BUDGET
FOR EACH ROW
BEGIN
    IF NEW.ID_Categoria IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM CATEGORIA C
            WHERE C.ID_Categoria = NEW.ID_Categoria
              AND (
                    C.is_system = TRUE
                    OR C.Email_Proprietario = NEW.Email
                  )
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Categoria del budget non valida per questo utente';
        END IF;
    END IF;
END$$

-- Controllo categoria valida nelle spese ricorrenti in inserimento
CREATE TRIGGER trg_spesa_ricorrente_insert_check
BEFORE INSERT ON SPESA_RICORRENTE
FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM CATEGORIA C
        WHERE C.ID_Categoria = NEW.ID_Categoria
          AND (
                C.is_system = TRUE
                OR C.Email_Proprietario = NEW.Email
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Categoria della spesa ricorrente non valida per questo utente';
    END IF;
END$$

-- Controllo categoria valida nelle spese ricorrenti in modifica
CREATE TRIGGER trg_spesa_ricorrente_update_check
BEFORE UPDATE ON SPESA_RICORRENTE
FOR EACH ROW
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM CATEGORIA C
        WHERE C.ID_Categoria = NEW.ID_Categoria
          AND (
                C.is_system = TRUE
                OR C.Email_Proprietario = NEW.Email
              )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Categoria della spesa ricorrente non valida per questo utente';
    END IF;
END$$

DELIMITER ;

-- ======================
-- VISTE
-- ======================

-- Vista dettagliata delle transizioni
CREATE VIEW v_transizioni_dettaglio AS
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
    P.Mese,
    P.Anno,
    C.Nome AS Categoria,
    F.Nome AS Fonte,
    D.Path_File AS Documento
FROM TRANSIZIONE T
JOIN PERIODO P ON T.ID_Periodo = P.ID_Periodo
LEFT JOIN CATEGORIA C ON T.ID_Categoria = C.ID_Categoria
LEFT JOIN FONTE F ON T.ID_Fonte = F.ID_Fonte
LEFT JOIN DOCUMENTO D ON T.ID_Transizione = D.ID_Transizione;

-- Vista statistiche aggregate per amministratore
CREATE VIEW v_statistiche_aggregate_admin AS
SELECT
    P.Anno,
    P.Mese,
    COUNT(*) AS Numero_Transazioni,
    SUM(CASE WHEN T.TipoTransazione = 'S' THEN T.Importo ELSE 0 END) AS Totale_Spese,
    SUM(CASE WHEN T.TipoTransazione = 'E' THEN T.Importo ELSE 0 END) AS Totale_Entrate
FROM TRANSIZIONE T
JOIN PERIODO P ON T.ID_Periodo = P.ID_Periodo
GROUP BY P.Anno, P.Mese;

-- Vista stato dei budget
CREATE VIEW v_budget_stato AS
SELECT
    B.ID_Budget,
    B.Email,
    P.Mese,
    P.Anno,
    COALESCE(C.Nome, 'Budget globale') AS Ambito,
    B.ID_Categoria,
    B.Importo_Limite,
    B.Alert_Soglia,
    COALESCE(SUM(T.Importo), 0) AS Totale_Speso,
    B.Importo_Limite - COALESCE(SUM(T.Importo), 0) AS Residuo,
    CASE
        WHEN COALESCE(SUM(T.Importo), 0) > B.Importo_Limite THEN TRUE
        ELSE FALSE
    END AS Superato
FROM BUDGET B
JOIN PERIODO P ON B.ID_Periodo = P.ID_Periodo
LEFT JOIN CATEGORIA C ON B.ID_Categoria = C.ID_Categoria
LEFT JOIN TRANSIZIONE T
    ON T.Email = B.Email
    AND T.ID_Periodo = B.ID_Periodo
    AND T.TipoTransazione = 'S'
    AND (
        B.ID_Categoria IS NULL
        OR T.ID_Categoria = B.ID_Categoria
    )
GROUP BY
    B.ID_Budget,
    B.Email,
    P.Mese,
    P.Anno,
    C.Nome,
    B.ID_Categoria,
    B.Importo_Limite,
    B.Alert_Soglia;