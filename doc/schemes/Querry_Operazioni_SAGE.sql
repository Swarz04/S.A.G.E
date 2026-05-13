-- =====================================================
-- SEGMENTO 04: QUERY DELLE OPERAZIONI PRINCIPALI
-- =====================================================

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- ======================
-- VARIABILI DI TEST
-- ======================

-- Utente usato per testare le query
SET @email_utente = 'studente1@mail.com';

-- Password usata per il login di test
SET @password_utente = 'password1';

-- Periodo usato per i test
SET @mese_test = 5;
SET @anno_test = 2026;

-- Recupero identificativo del periodo di test
SET @id_periodo_test = (
    SELECT ID_Periodo
    FROM PERIODO
    WHERE Mese = @mese_test
      AND Anno = @anno_test
);

-- Recupero categoria personale Mensa universitaria
SET @id_categoria_mensa = (
    SELECT ID_Categoria
    FROM CATEGORIA
    WHERE Nome = 'Mensa universitaria'
      AND Email_Proprietario = @email_utente
);

-- Recupero categoria di sistema Trasporti
SET @id_categoria_trasporti = (
    SELECT ID_Categoria
    FROM CATEGORIA
    WHERE Nome = 'Trasporti'
      AND is_system = TRUE
);

-- Recupero fonte personale Borsa di studio
SET @id_fonte_borsa = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Borsa di studio'
      AND Email_Proprietario = @email_utente
);

-- Recupero tag Università
SET @id_tag_universita = (
    SELECT ID_Tag
    FROM TAG
    WHERE Nome = 'Università'
      AND is_system = TRUE
);

-- ======================
-- LOGIN UTENTE O AMMINISTRATORE
-- ======================

SELECT Email, Nome, Cognome, Ruolo
FROM UTENTE
WHERE Email = @email_utente
  AND Password = SHA2(@password_utente, 512);

-- ======================
-- VISUALIZZAZIONE CATEGORIE DISPONIBILI PER UN UTENTE
-- ======================

SELECT ID_Categoria, Nome, is_system
FROM CATEGORIA
WHERE is_system = TRUE
   OR Email_Proprietario = @email_utente
ORDER BY is_system DESC, Nome;

-- ======================
-- VISUALIZZAZIONE FONTI DISPONIBILI PER UN UTENTE
-- ======================

SELECT ID_Fonte, Nome, is_system
FROM FONTE
WHERE is_system = TRUE
   OR Email_Proprietario = @email_utente
ORDER BY is_system DESC, Nome;

-- ======================
-- VISUALIZZAZIONE TAG DISPONIBILI PER UN UTENTE
-- ======================

SELECT ID_Tag, Nome, is_system
FROM TAG
WHERE is_system = TRUE
   OR Email_Proprietario = @email_utente
ORDER BY is_system DESC, Nome;

-- ======================
-- INSERIMENTO NUOVA SPESA
-- ======================

START TRANSACTION;

INSERT INTO PERIODO (Mese, Anno)
VALUES (5, 2026)
ON DUPLICATE KEY UPDATE ID_Periodo = LAST_INSERT_ID(ID_Periodo);

SET @id_periodo_spesa = LAST_INSERT_ID();

INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 12.00, '2026-05-25', 'Spesa di test in mensa', @email_utente, @id_categoria_mensa, @id_periodo_spesa, NULL);

SET @id_spesa_test = LAST_INSERT_ID();

COMMIT;

-- ======================
-- ASSOCIAZIONE TAG ALLA SPESA APPENA INSERITA
-- ======================

INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES (@id_spesa_test, @id_tag_universita);

-- ======================
-- ASSOCIAZIONE DOCUMENTO ALLA SPESA APPENA INSERITA
-- ======================

INSERT INTO DOCUMENTO
(ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento)
VALUES
(@id_spesa_test, '/documenti/spesa_test.pdf', 'PDF', CURRENT_DATE);

-- ======================
-- INSERIMENTO NUOVA ENTRATA
-- ======================

START TRANSACTION;

INSERT INTO PERIODO (Mese, Anno)
VALUES (5, 2026)
ON DUPLICATE KEY UPDATE ID_Periodo = LAST_INSERT_ID(ID_Periodo);

SET @id_periodo_entrata = LAST_INSERT_ID();

INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('E', 75.00, '2026-05-26', 'Entrata di test', @email_utente, NULL, @id_periodo_entrata, @id_fonte_borsa);

SET @id_entrata_test = LAST_INSERT_ID();

COMMIT;

-- ======================
-- CREAZIONE CATEGORIA PERSONALE
-- ======================

INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario)
VALUES ('Categoria personale test', FALSE, @email_utente);

SET @id_categoria_personale_test = LAST_INSERT_ID();

-- ======================
-- CREAZIONE CATEGORIA DI SISTEMA
-- Da usare solo se l'utente loggato è ADMIN lato applicazione.
-- ======================

INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario)
VALUES ('Categoria sistema test', TRUE, NULL);

SET @id_categoria_sistema_test = LAST_INSERT_ID();

-- ======================
-- CREAZIONE FONTE PERSONALE
-- ======================

INSERT INTO FONTE (Nome, is_system, Email_Proprietario)
VALUES ('Fonte personale test', FALSE, @email_utente);

SET @id_fonte_personale_test = LAST_INSERT_ID();

-- ======================
-- CREAZIONE FONTE DI SISTEMA
-- Da usare solo se l'utente loggato è ADMIN lato applicazione.
-- ======================

INSERT INTO FONTE (Nome, is_system, Email_Proprietario)
VALUES ('Fonte sistema test', TRUE, NULL);

SET @id_fonte_sistema_test = LAST_INSERT_ID();

-- ======================
-- CREAZIONE TAG PERSONALE
-- ======================

INSERT INTO TAG (Nome, is_system, Email_Proprietario)
VALUES ('Tag personale test', FALSE, @email_utente);

SET @id_tag_personale_test = LAST_INSERT_ID();

-- ======================
-- CREAZIONE TAG DI SISTEMA
-- Da usare solo se l'utente loggato è ADMIN lato applicazione.
-- ======================

INSERT INTO TAG (Nome, is_system, Email_Proprietario)
VALUES ('Tag sistema test', TRUE, NULL);

SET @id_tag_sistema_test = LAST_INSERT_ID();

-- ======================
-- DEFINIZIONE O AGGIORNAMENTO BUDGET GLOBALE
-- ======================

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, ID_Periodo, ID_Categoria, Email)
VALUES
(350.00, TRUE, @id_periodo_test, NULL, @email_utente)
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia);

-- ======================
-- DEFINIZIONE O AGGIORNAMENTO BUDGET PER CATEGORIA
-- ======================

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, ID_Periodo, ID_Categoria, Email)
VALUES
(150.00, TRUE, @id_periodo_test, @id_categoria_mensa, @email_utente)
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia);

-- ======================
-- MODIFICA TRANSAZIONE
-- ======================

UPDATE TRANSIZIONE
SET Importo = 13.50,
    Data = '2026-05-25',
    Descrizione = 'Spesa di test modificata',
    ID_Periodo = @id_periodo_test
WHERE ID_Transizione = @id_spesa_test
  AND Email = @email_utente;

-- ======================
-- RIEPILOGO TRANSAZIONI PERSONALI PER PERIODO
-- ======================

SELECT
    T.ID_Transizione,
    CASE
        WHEN T.TipoTransazione = 'S' THEN 'Spesa'
        WHEN T.TipoTransazione = 'E' THEN 'Entrata'
    END AS Tipo,
    T.Importo,
    T.Data,
    T.Descrizione,
    COALESCE(C.Nome, F.Nome) AS Categoria_o_Fonte
FROM TRANSIZIONE T
LEFT JOIN CATEGORIA C ON T.ID_Categoria = C.ID_Categoria
LEFT JOIN FONTE F ON T.ID_Fonte = F.ID_Fonte
WHERE T.Email = @email_utente
  AND T.Data BETWEEN '2026-05-01' AND '2026-05-31'
ORDER BY T.Data DESC;

-- ======================
-- TOTALE SPESE PER CATEGORIA
-- ======================

SELECT
    C.Nome AS Categoria,
    SUM(T.Importo) AS Totale_Speso
FROM TRANSIZIONE T
JOIN CATEGORIA C ON T.ID_Categoria = C.ID_Categoria
WHERE T.Email = @email_utente
  AND T.TipoTransazione = 'S'
  AND T.Data BETWEEN '2026-05-01' AND '2026-05-31'
GROUP BY C.ID_Categoria, C.Nome
ORDER BY Totale_Speso DESC;

-- ======================
-- ANDAMENTO MENSILE SPESE ED ENTRATE
-- ======================

SELECT
    P.Anno,
    P.Mese,
    SUM(CASE WHEN T.TipoTransazione = 'S' THEN T.Importo ELSE 0 END) AS Totale_Spese,
    SUM(CASE WHEN T.TipoTransazione = 'E' THEN T.Importo ELSE 0 END) AS Totale_Entrate
FROM TRANSIZIONE T
JOIN PERIODO P ON T.ID_Periodo = P.ID_Periodo
WHERE T.Email = @email_utente
GROUP BY P.Anno, P.Mese
ORDER BY P.Anno DESC, P.Mese DESC;

-- ======================
-- PERCENTUALE SPESE SU ENTRATE
-- ======================

SELECT
    (
        SUM(CASE WHEN TipoTransazione = 'S' THEN Importo ELSE 0 END)
        /
        NULLIF(SUM(CASE WHEN TipoTransazione = 'E' THEN Importo ELSE 0 END), 0)
    ) * 100 AS Percentuale_Spese_Su_Entrate
FROM TRANSIZIONE
WHERE Email = @email_utente
  AND ID_Periodo = @id_periodo_test;

-- ======================
-- BUDGET SUPERATI
-- ======================

SELECT
    ID_Budget,
    Mese,
    Anno,
    Ambito,
    Importo_Limite,
    Totale_Speso,
    Residuo
FROM v_budget_stato
WHERE Email = @email_utente
  AND Superato = TRUE
ORDER BY Anno DESC, Mese DESC;

-- ======================
-- STATO DI TUTTI I BUDGET DI UN UTENTE
-- ======================

SELECT
    ID_Budget,
    Mese,
    Anno,
    Ambito,
    Importo_Limite,
    Totale_Speso,
    Residuo,
    Superato
FROM v_budget_stato
WHERE Email = @email_utente
ORDER BY Anno DESC, Mese DESC, Ambito;

-- ======================
-- TAG PIU' UTILIZZATI
-- ======================

SELECT
    TA.Nome AS Tag,
    COUNT(*) AS Numero_Utilizzi
FROM spesa_tag ST
JOIN TAG TA ON ST.ID_Tag = TA.ID_Tag
JOIN TRANSIZIONE T ON ST.ID_Transizione = T.ID_Transizione
WHERE T.Email = @email_utente
GROUP BY TA.ID_Tag, TA.Nome
ORDER BY Numero_Utilizzi DESC;

-- ======================
-- SPESE RICORRENTI DI UN UTENTE
-- ======================

SELECT
    SR.ID_Ricorrenza,
    SR.Importo_Previsto,
    SR.Frequenza_Giorni,
    SR.Data_Inizio,
    SR.Scadenza,
    C.Nome AS Categoria
FROM SPESA_RICORRENTE SR
JOIN CATEGORIA C ON SR.ID_Categoria = C.ID_Categoria
WHERE SR.Email = @email_utente
ORDER BY SR.Scadenza;

-- ======================
-- STATISTICHE AGGREGATE AMMINISTRATORE
-- Non mostra dettagli personali degli utenti.
-- ======================

SELECT
    Anno,
    Mese,
    Numero_Transazioni,
    Totale_Spese,
    Totale_Entrate
FROM v_statistiche_aggregate_admin
ORDER BY Anno DESC, Mese DESC;

-- ======================
-- ELIMINAZIONE TRANSAZIONE DI TEST
-- Il documento e i tag associati vengono eliminati in cascata.
-- ======================

DELETE FROM TRANSIZIONE
WHERE ID_Transizione = @id_spesa_test
  AND Email = @email_utente;

-- ======================
-- ELIMINAZIONE ENTRATA DI TEST
-- ======================

DELETE FROM TRANSIZIONE
WHERE ID_Transizione = @id_entrata_test
  AND Email = @email_utente;

-- ======================
-- ELIMINAZIONE CATEGORIA PERSONALE DI TEST
-- Funziona solo se la categoria non è usata da transazioni, budget o ricorrenze.
-- ======================

DELETE FROM CATEGORIA
WHERE ID_Categoria = @id_categoria_personale_test
  AND is_system = FALSE
  AND Email_Proprietario = @email_utente;