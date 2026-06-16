-- S.A.G.E. - installazione completa del database
-- Eseguire questo file per creare da zero database, tabelle, indici, trigger e viste.
-- ATTENZIONE: il database esistente con lo stesso nome viene eliminato.

-- Schema base di S.A.G.E.
-- Questa e' la versione pulita per MySQL: tipi sistemati, chiavi esterne
-- esplicite e controlli CHECK dove servono.
-- Mantengo il nome TRANSIZIONE per restare coerente con gli altri script del
-- progetto, anche se semanticamente rappresenta sia spese sia entrate.

DROP DATABASE IF EXISTS Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;
CREATE DATABASE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;
USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

CREATE TABLE UTENTE (
    Email VARCHAR(255) NOT NULL,
    Password VARCHAR(128) NOT NULL,
    Nome VARCHAR(80) NOT NULL,
    Cognome VARCHAR(80) NOT NULL,
    Ruolo VARCHAR(10) NOT NULL DEFAULT 'UTENTE',
    CONSTRAINT PK_UTENTE PRIMARY KEY (Email),
    CONSTRAINT CK_UTENTE_RUOLO CHECK (Ruolo IN ('UTENTE', 'ADMIN'))
);

CREATE TABLE PERIODO (
    ID_Periodo INT NOT NULL AUTO_INCREMENT,
    Mese TINYINT NOT NULL,
    Anno SMALLINT NOT NULL,
    CONSTRAINT PK_PERIODO PRIMARY KEY (ID_Periodo),
    CONSTRAINT UQ_PERIODO_MESE_ANNO UNIQUE (Mese, Anno),
    CONSTRAINT CK_PERIODO_MESE CHECK (Mese BETWEEN 1 AND 12),
    CONSTRAINT CK_PERIODO_ANNO CHECK (Anno >= 2000)
);

CREATE TABLE CATEGORIA (
    ID_Categoria INT NOT NULL AUTO_INCREMENT,
    Nome VARCHAR(80) NOT NULL,
    Icona VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    Email_Proprietario VARCHAR(255),
    CONSTRAINT PK_CATEGORIA PRIMARY KEY (ID_Categoria),
    CONSTRAINT FK_CATEGORIA_UTENTE FOREIGN KEY (Email_Proprietario)
        REFERENCES UTENTE (Email)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT CK_CATEGORIA_OWNER CHECK (
        (is_system = TRUE AND Email_Proprietario IS NULL)
        OR
        (is_system = FALSE AND Email_Proprietario IS NOT NULL)
    )
);

CREATE TABLE FONTE (
    ID_Fonte INT NOT NULL AUTO_INCREMENT,
    Nome VARCHAR(80) NOT NULL,
    Icona VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    Email_Proprietario VARCHAR(255),
    CONSTRAINT PK_FONTE PRIMARY KEY (ID_Fonte),
    CONSTRAINT FK_FONTE_UTENTE FOREIGN KEY (Email_Proprietario)
        REFERENCES UTENTE (Email)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT CK_FONTE_OWNER CHECK (
        (is_system = TRUE AND Email_Proprietario IS NULL)
        OR
        (is_system = FALSE AND Email_Proprietario IS NOT NULL)
    )
);

CREATE TABLE TAG (
    ID_Tag INT NOT NULL AUTO_INCREMENT,
    Nome VARCHAR(80) NOT NULL,
    Icona VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    Email_Proprietario VARCHAR(255),
    CONSTRAINT PK_TAG PRIMARY KEY (ID_Tag),
    CONSTRAINT FK_TAG_UTENTE FOREIGN KEY (Email_Proprietario)
        REFERENCES UTENTE (Email)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT CK_TAG_OWNER CHECK (
        (is_system = TRUE AND Email_Proprietario IS NULL)
        OR
        (is_system = FALSE AND Email_Proprietario IS NOT NULL)
    )
);

CREATE TABLE BUDGET (
    ID_Budget INT NOT NULL AUTO_INCREMENT,
    Importo_Limite DECIMAL(10,2) NOT NULL,
    Alert_Soglia BOOLEAN NOT NULL DEFAULT TRUE,
    Totale_Speso_Attuale DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    ID_Periodo INT NOT NULL,
    ID_Categoria INT,
    ID_Categoria_Key INT GENERATED ALWAYS AS (IFNULL(ID_Categoria, 0)) STORED,
    Email VARCHAR(255) NOT NULL,
    CONSTRAINT PK_BUDGET PRIMARY KEY (ID_Budget),
    CONSTRAINT FK_BUDGET_PERIODO FOREIGN KEY (ID_Periodo)
        REFERENCES PERIODO (ID_Periodo)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT FK_BUDGET_CATEGORIA FOREIGN KEY (ID_Categoria)
        REFERENCES CATEGORIA (ID_Categoria)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT FK_BUDGET_UTENTE FOREIGN KEY (Email)
        REFERENCES UTENTE (Email)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT CK_BUDGET_IMPORTO CHECK (Importo_Limite >= 0),
    CONSTRAINT CK_BUDGET_TOTALE CHECK (Totale_Speso_Attuale >= 0),
    CONSTRAINT UQ_BUDGET_AMBITO UNIQUE (Email, ID_Periodo, ID_Categoria_Key)
);

CREATE TABLE TRANSIZIONE (
    ID_Transizione INT NOT NULL AUTO_INCREMENT,
    TipoTransazione CHAR(1) NOT NULL,
    Importo DECIMAL(10,2) NOT NULL,
    Data DATE NOT NULL,
    Descrizione VARCHAR(255) NOT NULL,
    Email VARCHAR(255) NOT NULL,
    ID_Categoria INT,
    ID_Periodo INT NOT NULL,
    ID_Fonte INT,
    ID_Ricorrenza INT,
    CONSTRAINT PK_TRANSIZIONE PRIMARY KEY (ID_Transizione),
    CONSTRAINT FK_TRANSIZIONE_UTENTE FOREIGN KEY (Email)
        REFERENCES UTENTE (Email)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT FK_TRANSIZIONE_CATEGORIA FOREIGN KEY (ID_Categoria)
        REFERENCES CATEGORIA (ID_Categoria)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT FK_TRANSIZIONE_PERIODO FOREIGN KEY (ID_Periodo)
        REFERENCES PERIODO (ID_Periodo)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT FK_TRANSIZIONE_FONTE FOREIGN KEY (ID_Fonte)
        REFERENCES FONTE (ID_Fonte)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT CK_TRANSIZIONE_TIPO CHECK (TipoTransazione IN ('S', 'E')),
    CONSTRAINT CK_TRANSIZIONE_IMPORTO CHECK (Importo > 0),
    CONSTRAINT CK_TRANSIZIONE_RICORRENZA CHECK (
        ID_Ricorrenza IS NULL OR TipoTransazione = 'S'
    ),
    CONSTRAINT CK_TRANSIZIONE_COLLASSO CHECK (
        (TipoTransazione = 'S' AND ID_Categoria IS NOT NULL AND ID_Fonte IS NULL)
        OR
        (TipoTransazione = 'E' AND ID_Categoria IS NULL AND ID_Fonte IS NOT NULL)
    )
);

CREATE TABLE SPESA_TAG (
    ID_Transizione INT NOT NULL,
    ID_Tag INT NOT NULL,
    CONSTRAINT PK_SPESA_TAG PRIMARY KEY (ID_Transizione, ID_Tag),
    CONSTRAINT FK_SPESA_TAG_TRANSIZIONE FOREIGN KEY (ID_Transizione)
        REFERENCES TRANSIZIONE (ID_Transizione)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT FK_SPESA_TAG_TAG FOREIGN KEY (ID_Tag)
        REFERENCES TAG (ID_Tag)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE DOCUMENTO (
    ID_Documento INT NOT NULL AUTO_INCREMENT,
    ID_Transizione INT NOT NULL,
    Path_File VARCHAR(500) NOT NULL,
    Tipo_File VARCHAR(20) NOT NULL,
    Data_Acquisizione_Documento DATE NOT NULL,
    CONSTRAINT PK_DOCUMENTO PRIMARY KEY (ID_Documento),
    CONSTRAINT FK_DOCUMENTO_TRANSIZIONE FOREIGN KEY (ID_Transizione)
        REFERENCES TRANSIZIONE (ID_Transizione)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE SPESA_RICORRENTE (
    ID_Ricorrenza INT NOT NULL AUTO_INCREMENT,
    Nome VARCHAR(100) NOT NULL,
    Importo_Previsto DECIMAL(10,2) NOT NULL,
    Frequenza_Giorni INT NOT NULL,
    Data_Inizio DATE NOT NULL,
    Data_Prossima_Scadenza DATE NOT NULL,
    Scadenza DATE,
    ID_Categoria INT NOT NULL,
    Email VARCHAR(255) NOT NULL,
    CONSTRAINT PK_SPESA_RICORRENTE PRIMARY KEY (ID_Ricorrenza),
    CONSTRAINT FK_RICORRENTE_CATEGORIA FOREIGN KEY (ID_Categoria)
        REFERENCES CATEGORIA (ID_Categoria)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT FK_RICORRENTE_UTENTE FOREIGN KEY (Email)
        REFERENCES UTENTE (Email)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT CK_RICORRENTE_IMPORTO CHECK (Importo_Previsto > 0),
    CONSTRAINT CK_RICORRENTE_FREQUENZA CHECK (Frequenza_Giorni > 0),
    CONSTRAINT CK_RICORRENTE_NOME CHECK (CHAR_LENGTH(TRIM(Nome)) > 0)
);

-- La transazione generata conserva il riferimento al modello ricorrente.
-- Il vincolo viene aggiunto dopo la creazione di SPESA_RICORRENTE perché la
-- tabella TRANSIZIONE viene definita prima nello schema.
ALTER TABLE TRANSIZIONE
    ADD CONSTRAINT FK_TRANSIZIONE_RICORRENZA
    FOREIGN KEY (ID_Ricorrenza)
    REFERENCES SPESA_RICORRENTE (ID_Ricorrenza)
    ON UPDATE CASCADE
    ON DELETE SET NULL;

CREATE INDEX IDX_TRANSIZIONE_UTENTE_DATA ON TRANSIZIONE (Email, Data);
CREATE INDEX IDX_TRANSIZIONE_PERIODO ON TRANSIZIONE (ID_Periodo);
CREATE INDEX IDX_TRANSIZIONE_CATEGORIA ON TRANSIZIONE (ID_Categoria);
CREATE UNIQUE INDEX UQ_TRANSIZIONE_RICORRENZA_DATA
    ON TRANSIZIONE (ID_Ricorrenza, Data);
CREATE INDEX IDX_BUDGET_UTENTE_PERIODO ON BUDGET (Email, ID_Periodo);
CREATE UNIQUE INDEX UQ_DOCUMENTO_TRANSIZIONE ON DOCUMENTO (ID_Transizione);
CREATE INDEX IDX_RICORRENTE_EMAIL_SCADENZA ON SPESA_RICORRENTE (Email, Data_Prossima_Scadenza);


-- ===========================================================================
-- TRIGGER E VISTE
-- ===========================================================================

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

-- Stessa regola anche per le fonti: una fonte personale non deve duplicare
-- una fonte di sistema o un'altra fonte dello stesso utente.
CREATE TRIGGER trg_fonte_insert_no_duplicati
BEFORE INSERT ON FONTE
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1
        FROM FONTE F
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
        SELECT 1
        FROM FONTE F
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
