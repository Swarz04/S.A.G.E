-- S.A.G.E. - query operative e dimostrative
-- Questo file non e necessario per avviare l'applicazione.

-- Query operative usate per simulare le funzioni principali dell'app.

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- Variabili di test: le imposto una volta e le riuso nel resto dello script.

-- Studente demo su cui faccio girare gli esempi.
SET @email_utente = 'studente1@mail.com';

-- Password in chiaro solo per simulare il login nello script.
SET @password_utente = 'password1';

-- Mese di riferimento scelto per le prove.
SET @mese_test = 5;
SET @anno_test = 2026;

-- Recupero l'ID del periodo, cosi' le query sotto non dipendono da valori fissi.
SET @id_periodo_test = (
    SELECT ID_Periodo
    FROM PERIODO
    WHERE Mese = @mese_test
      AND Anno = @anno_test
);

-- Categoria personale usata per le spese in mensa.
SET @id_categoria_mensa = (
    SELECT ID_Categoria
    FROM CATEGORIA
    WHERE Nome = 'Mensa universitaria'
      AND Email_Proprietario = @email_utente
);

-- Categoria di sistema utile per gli esempi sui trasporti.
SET @id_categoria_trasporti = (
    SELECT ID_Categoria
    FROM CATEGORIA
    WHERE Nome = 'Trasporti'
      AND is_system = TRUE
);

-- Fonte personale usata per simulare un'entrata dello studente.
SET @id_fonte_borsa = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Borsa di studio'
      AND Email_Proprietario = @email_utente
);

-- Tag di sistema per riconoscere le spese universitarie.
SET @id_tag_universita = (
    SELECT ID_Tag
    FROM TAG
    WHERE Nome = 'Università'
      AND is_system = TRUE
);

-- Login: controllo email e password hashata, poi leggo anche il ruolo.

SELECT Email, Nome, Cognome, Ruolo
FROM UTENTE
WHERE Email = @email_utente
  AND Password = SHA2(@password_utente, 512);

-- Categorie visibili all'utente: quelle di sistema piu' le sue personali.

SELECT ID_Categoria, Nome, is_system
FROM CATEGORIA
WHERE is_system = TRUE
   OR Email_Proprietario = @email_utente
ORDER BY is_system DESC, Nome;

-- Fonti visibili all'utente, con la stessa logica delle categorie.

SELECT ID_Fonte, Nome, is_system
FROM FONTE
WHERE is_system = TRUE
   OR Email_Proprietario = @email_utente
ORDER BY is_system DESC, Nome;

-- Tag utilizzabili dallo studente nelle proprie spese.

SELECT ID_Tag, Nome, is_system
FROM TAG
WHERE is_system = TRUE
   OR Email_Proprietario = @email_utente
ORDER BY is_system DESC, Nome;

-- Inserimento di una nuova spesa: creo/recupero il periodo e salvo la transazione.

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

-- Collegamento del tag alla spesa appena creata.

INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES (@id_spesa_test, @id_tag_universita);

-- Esempio di documento associato alla spesa, come uno scontrino digitale.

INSERT INTO DOCUMENTO
(ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento)
VALUES
(@id_spesa_test, '/documenti/spesa_test.pdf', 'PDF', CURRENT_DATE);

-- Inserimento di una nuova entrata usando una fonte valida per lo studente.

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

-- Creo una categoria personale, quindi legata all'utente corrente.

INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario)
VALUES ('Categoria personale test', FALSE, @email_utente);

SET @id_categoria_personale_test = LAST_INSERT_ID();

-- Creo una categoria di sistema: lato applicazione questa operazione va
-- concessa solo a un amministratore.

INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario)
VALUES ('Categoria sistema test', TRUE, NULL);

SET @id_categoria_sistema_test = LAST_INSERT_ID();

-- Creo una fonte personale per lo studente.

INSERT INTO FONTE (Nome, is_system, Email_Proprietario)
VALUES ('Fonte personale test', FALSE, @email_utente);

SET @id_fonte_personale_test = LAST_INSERT_ID();

-- Creo una fonte di sistema, anche qui riservata agli amministratori nell'app.

INSERT INTO FONTE (Nome, is_system, Email_Proprietario)
VALUES ('Fonte sistema test', TRUE, NULL);

SET @id_fonte_sistema_test = LAST_INSERT_ID();

-- Creo un tag personale per classificazioni piu' specifiche.

INSERT INTO TAG (Nome, is_system, Email_Proprietario)
VALUES ('Tag personale test', FALSE, @email_utente);

SET @id_tag_personale_test = LAST_INSERT_ID();

-- Creo un tag di sistema, disponibile poi per tutti gli utenti.

INSERT INTO TAG (Nome, is_system, Email_Proprietario)
VALUES ('Tag sistema test', TRUE, NULL);

SET @id_tag_sistema_test = LAST_INSERT_ID();

-- Budget globale del periodo: se esiste gia', aggiorno il limite.

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, ID_Periodo, ID_Categoria, Email)
VALUES
(350.00, TRUE, @id_periodo_test, NULL, @email_utente)
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia);

-- Budget specifico su una categoria, sempre con upsert per evitare duplicati.

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, ID_Periodo, ID_Categoria, Email)
VALUES
(150.00, TRUE, @id_periodo_test, @id_categoria_mensa, @email_utente)
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia);

-- Modifico una transazione di test mantenendo il controllo sull'utente proprietario.

UPDATE TRANSIZIONE
SET Importo = 13.50,
    Data = '2026-05-25',
    Descrizione = 'Spesa di test modificata',
    ID_Periodo = @id_periodo_test
WHERE ID_Transizione = @id_spesa_test
  AND Email = @email_utente;

-- Riepilogo delle transazioni dello studente nel mese scelto.

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

-- Quanto e' stato speso per ogni categoria nel periodo.

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

-- Andamento mensile: sommo spese ed entrate per vedere l'evoluzione nel tempo.

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

-- Percentuale di spese rispetto alle entrate del periodo.

SELECT
    (
        SUM(CASE WHEN TipoTransazione = 'S' THEN Importo ELSE 0 END)
        /
        NULLIF(SUM(CASE WHEN TipoTransazione = 'E' THEN Importo ELSE 0 END), 0)
    ) * 100 AS Percentuale_Spese_Su_Entrate
FROM TRANSIZIONE
WHERE Email = @email_utente
  AND ID_Periodo = @id_periodo_test;

-- Elenco dei budget gia' superati, utile per eventuali avvisi.

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

-- Stato completo dei budget dell'utente, anche quelli ancora sotto soglia.

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

-- Tag piu' usati: aiuta a capire come lo studente classifica le spese.

SELECT
    TA.Nome AS Tag,
    COUNT(*) AS Numero_Utilizzi
FROM spesa_tag ST
JOIN TAG TA ON ST.ID_Tag = TA.ID_Tag
JOIN TRANSIZIONE T ON ST.ID_Transizione = T.ID_Transizione
WHERE T.Email = @email_utente
GROUP BY TA.ID_Tag, TA.Nome
ORDER BY Numero_Utilizzi DESC;

-- Spese ricorrenti impostate dall'utente.

SELECT
    SR.ID_Ricorrenza,
    SR.Nome,
    SR.Importo_Previsto,
    SR.Frequenza_Giorni,
    SR.Data_Inizio,
    SR.Data_Prossima_Scadenza,
    SR.Scadenza,
    C.Nome AS Categoria,
    COUNT(T.ID_Transizione) AS Transazioni_Generate
FROM SPESA_RICORRENTE SR
JOIN CATEGORIA C ON SR.ID_Categoria = C.ID_Categoria
LEFT JOIN TRANSIZIONE T ON T.ID_Ricorrenza = SR.ID_Ricorrenza
WHERE SR.Email = @email_utente
GROUP BY
    SR.ID_Ricorrenza,
    SR.Nome,
    SR.Importo_Previsto,
    SR.Frequenza_Giorni,
    SR.Data_Inizio,
    SR.Data_Prossima_Scadenza,
    SR.Scadenza,
    C.Nome
ORDER BY SR.Data_Prossima_Scadenza;

-- Statistiche per l'amministratore: aggregate, quindi senza dettagli personali.

SELECT
    Anno,
    Mese,
    Numero_Transazioni,
    Totale_Spese,
    Totale_Entrate
FROM v_statistiche_aggregate_admin
ORDER BY Anno DESC, Mese DESC;

-- Pulizia della spesa di test: documento e tag collegati vengono rimossi in cascata.

DELETE FROM TRANSIZIONE
WHERE ID_Transizione = @id_spesa_test
  AND Email = @email_utente;

-- Pulizia dell'entrata di test.

DELETE FROM TRANSIZIONE
WHERE ID_Transizione = @id_entrata_test
  AND Email = @email_utente;

-- Pulizia della categoria personale di test. Riesce solo se non e' usata da
-- transazioni, budget o ricorrenze.

DELETE FROM CATEGORIA
WHERE ID_Categoria = @id_categoria_personale_test
  AND is_system = FALSE
  AND Email_Proprietario = @email_utente;
