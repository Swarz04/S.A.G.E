-- Seconda parte dello schema: trigger di controllo e viste di supporto.

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- Trigger: sposto nel database alcuni controlli che devono valere sempre,
-- anche se in futuro cambia la parte applicativa.

DELIMITER $$

-- Una categoria personale non può duplicare, ignorando maiuscole e spazi,
-- una categoria di sistema o un'altra categoria dello stesso utente.
CREATE TRIGGER trg_categoria_insert_no_duplicati
BEFORE INSERT ON CATEGORIA
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1
        FROM CATEGORIA C
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
        SELECT 1
        FROM CATEGORIA C
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

-- Stessa regola per i tag: nessun doppione personale rispetto ai tag di
-- sistema o agli altri tag dello stesso utente.
CREATE TRIGGER trg_tag_insert_no_duplicati
BEFORE INSERT ON TAG
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1
        FROM TAG T
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
        SELECT 1
        FROM TAG T
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

-- Quando inserisco una transazione, verifico che categoria o fonte appartengano
-- all'utente oppure siano elementi di sistema. Se nasce da una ricorrenza,
-- verifico anche il collegamento al relativo modello.
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

    IF NEW.ID_Ricorrenza IS NOT NULL THEN
        IF NEW.TipoTransazione <> 'S' OR NOT EXISTS (
            SELECT 1
            FROM SPESA_RICORRENTE SR
            WHERE SR.ID_Ricorrenza = NEW.ID_Ricorrenza
              AND SR.Email = NEW.Email
              AND SR.ID_Categoria = NEW.ID_Categoria
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Ricorrenza non coerente con la transazione';
        END IF;
    END IF;
END$$

-- Ripeto lo stesso controllo anche in modifica, per evitare incoerenze dopo
-- un aggiornamento.
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

    IF NEW.ID_Ricorrenza IS NOT NULL THEN
        IF NEW.TipoTransazione <> 'S' OR NOT EXISTS (
            SELECT 1
            FROM SPESA_RICORRENTE SR
            WHERE SR.ID_Ricorrenza = NEW.ID_Ricorrenza
              AND SR.Email = NEW.Email
              AND SR.ID_Categoria = NEW.ID_Categoria
        ) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Ricorrenza non coerente con la transazione';
        END IF;
    END IF;
END$$

-- I tag possono essere collegati solo a spese valide e visibili all'utente.
CREATE TRIGGER trg_spesatag_insert_check
BEFORE INSERT ON SPESA_TAG
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

-- Anche cambiando un'associazione, il tag deve rimanere compatibile con la
-- spesa scelta.
CREATE TRIGGER trg_spesatag_update_check
BEFORE UPDATE ON SPESA_TAG
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

-- Un documento ha senso solo per una spesa, quindi blocco l'inserimento su
-- entrate o transazioni inesistenti.
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

-- Lo stesso vincolo vale se qualcuno prova a modificare il documento dopo.
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

-- Per un budget con categoria specifica controllo che la categoria sia
-- utilizzabile dall'utente.
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

-- In aggiornamento mantengo lo stesso controllo sui budget per categoria.
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

-- Una spesa ricorrente deve puntare a una categoria effettivamente disponibile
-- per quell'utente.
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

-- Se cambio una ricorrenza, ricontrollo la categoria per non lasciare dati
-- non coerenti.
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

-- Viste: preparo alcune letture gia' pronte per dashboard, report e query demo.

-- Vista comoda per leggere una transazione con periodo, categoria/fonte,
-- eventuale ricorrenza e documento in un unico risultato.
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

-- Statistiche aggregate per l'amministratore, senza esporre i dettagli personali
-- dei singoli utenti.
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

-- Stato dei budget: confronto limite, speso e residuo per capire subito se un
-- budget e' stato superato.
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

-- Spese ricorrenti già scadute rispetto alla data corrente.
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
