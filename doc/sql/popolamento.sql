-- Dati iniziali per provare S.A.G.E. con utenti, categorie e movimenti demo.

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- Popolamento di base: creo prima le entita' principali e poi salvo gli ID
-- che servono nelle associazioni successive.

-- Utenti di prova: due studenti normali e un amministratore.
INSERT INTO UTENTE (Email, Password, Nome, Cognome, Ruolo)
VALUES
('studente1@mail.com', SHA2('password1', 512), 'Mario', 'Rossi', 'UTENTE'),
('studente2@mail.com', SHA2('password2', 512), 'Luca', 'Bianchi', 'UTENTE'),
('admin@sage.com', SHA2('admin123', 512), 'Admin', 'Sistema', 'ADMIN');

-- Periodi usati negli esempi del report e nelle query operative.
INSERT INTO PERIODO (Mese, Anno)
VALUES 
(5, 2026),
(6, 2026),
(7, 2026);

-- Salvo l'ID di maggio 2026 per riusarlo senza scrivere numeri fissi.
SET @periodo_maggio = (
    SELECT ID_Periodo 
    FROM PERIODO 
    WHERE Mese = 5 AND Anno = 2026
);

-- Stessa cosa per giugno 2026.
SET @periodo_giugno = (
    SELECT ID_Periodo 
    FROM PERIODO 
    WHERE Mese = 6 AND Anno = 2026
);

-- Tengo pronto anche luglio 2026 per eventuali prove successive.
SET @periodo_luglio = (
    SELECT ID_Periodo 
    FROM PERIODO 
    WHERE Mese = 7 AND Anno = 2026
);

-- Categorie comuni, disponibili per tutti gli utenti.
INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario)
VALUES
('Trasporti', TRUE, NULL),
('Alimentari', TRUE, NULL),
('Casa', TRUE, NULL),
('Salute', TRUE, NULL),
('Svago', TRUE, NULL);

-- Categorie personali: appartengono a un solo studente.
INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario)
VALUES
('Mensa universitaria', FALSE, 'studente1@mail.com'),
('Libri universitari', FALSE, 'studente1@mail.com'),
('Palestra', FALSE, 'studente2@mail.com');

-- Recupero la categoria Trasporti creata come categoria di sistema.
SET @cat_trasporti = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Trasporti' AND is_system = TRUE
);

-- Recupero anche Alimentari per usarla nelle prove.
SET @cat_alimentari = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Alimentari' AND is_system = TRUE
);

-- Categoria personale di studente1 dedicata alle spese in mensa.
SET @cat_mensa = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Mensa universitaria' 
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Categoria personale di studente1 per il materiale universitario.
SET @cat_libri = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Libri universitari' 
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Categoria personale di studente2, utile per verificare la separazione utenti.
SET @cat_palestra = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Palestra' 
      AND Email_Proprietario = 'studente2@mail.com'
);

-- Fonti di entrata generali, valide per tutti.
INSERT INTO FONTE (Nome, is_system, Email_Proprietario)
VALUES
('Stipendio', TRUE, NULL),
('Regalo', TRUE, NULL),
('Rimborso', TRUE, NULL);

-- Fonti personali dello studente demo.
INSERT INTO FONTE (Nome, is_system, Email_Proprietario)
VALUES
('Borsa di studio', FALSE, 'studente1@mail.com'),
('Ripetizioni private', FALSE, 'studente1@mail.com'),
('Lavoretto weekend', FALSE, 'studente2@mail.com');

-- ID della borsa di studio, usato per l'entrata principale.
SET @fonte_borsa = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Borsa di studio'
      AND Email_Proprietario = 'studente1@mail.com'
);

-- ID della fonte legata alle ripetizioni private.
SET @fonte_ripetizioni = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Ripetizioni private'
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Fonte personale dello studente2, utile per provare un secondo portafoglio.
SET @fonte_lavoretto = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Lavoretto weekend'
      AND Email_Proprietario = 'studente2@mail.com'
);

-- Fonte di sistema usata per un'entrata occasionale.
SET @fonte_regalo = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Regalo'
      AND is_system = TRUE
);

-- Tag comuni per classificare velocemente le spese.
INSERT INTO TAG (Nome, is_system, Email_Proprietario)
VALUES
('Essenziale', TRUE, NULL),
('Università', TRUE, NULL),
('Extra', TRUE, NULL);

-- Tag personali che rendono piu' flessibile la classificazione dello studente.
INSERT INTO TAG (Nome, is_system, Email_Proprietario)
VALUES
('Esame', FALSE, 'studente1@mail.com'),
('Weekend', FALSE, 'studente1@mail.com');

-- Tag per distinguere le spese davvero necessarie.
SET @tag_essenziale = (
    SELECT ID_Tag
    FROM TAG
    WHERE Nome = 'Essenziale'
      AND is_system = TRUE
);

-- Tag legato al contesto universitario.
SET @tag_universita = (
    SELECT ID_Tag
    FROM TAG
    WHERE Nome = 'Università'
      AND is_system = TRUE
);

-- Tag personale usato per le spese collegate agli esami.
SET @tag_esame = (
    SELECT ID_Tag
    FROM TAG
    WHERE Nome = 'Esame'
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Prima spesa demo: un pranzo in mensa universitaria.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 8.50, '2026-05-13', 'Pranzo in mensa', 'studente1@mail.com', @cat_mensa, @periodo_maggio, NULL);

SET @spesa_mensa = LAST_INSERT_ID();

-- La spesa in mensa e' sia universitaria sia essenziale.
INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES
(@spesa_mensa, @tag_universita),
(@spesa_mensa, @tag_essenziale);

-- Associo uno scontrino demo alla spesa in mensa.
INSERT INTO DOCUMENTO
(ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento)
VALUES
(@spesa_mensa, '/documenti/scontrino_mensa.pdf', 'PDF', '2026-05-13');

-- Seconda spesa demo: acquisto di un libro per l'universita'.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 42.00, '2026-05-15', 'Acquisto libro universitario', 'studente1@mail.com', @cat_libri, @periodo_maggio, NULL);

SET @spesa_libro = LAST_INSERT_ID();

-- Il libro viene marcato come spesa universitaria e collegata a un esame.
INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES
(@spesa_libro, @tag_universita),
(@spesa_libro, @tag_esame);

-- Terza spesa demo: abbonamento ai trasporti.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 25.00, '2026-05-18', 'Abbonamento autobus', 'studente1@mail.com', @cat_trasporti, @periodo_maggio, NULL);

SET @spesa_autobus = LAST_INSERT_ID();

-- L'abbonamento viene considerato una spesa essenziale.
INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES
(@spesa_autobus, @tag_essenziale);

-- Entrata principale dello studente: accredito della borsa di studio.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('E', 500.00, '2026-05-01', 'Accredito borsa di studio', 'studente1@mail.com', NULL, @periodo_maggio, @fonte_borsa);

-- Entrata extra ottenuta con ripetizioni private.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('E', 120.00, '2026-05-20', 'Pagamento ripetizioni private', 'studente1@mail.com', NULL, @periodo_maggio, @fonte_ripetizioni);

-- Entrata occasionale inserita nel mese successivo.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('E', 50.00, '2026-06-02', 'Regalo famiglia', 'studente1@mail.com', NULL, @periodo_giugno, @fonte_regalo);

-- Budget globale per maggio: limite complessivo del mese.
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
VALUES
(300.00, TRUE, (
    SELECT COALESCE(SUM(Importo), 0)
    FROM TRANSIZIONE
    WHERE Email = 'studente1@mail.com'
      AND ID_Periodo = @periodo_maggio
      AND TipoTransazione = 'S'
), @periodo_maggio, NULL, 'studente1@mail.com')
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia),
Totale_Speso_Attuale = VALUES(Totale_Speso_Attuale);

-- Budget specifico per tenere sotto controllo la mensa.
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
VALUES
(120.00, TRUE, (
    SELECT COALESCE(SUM(Importo), 0)
    FROM TRANSIZIONE
    WHERE Email = 'studente1@mail.com'
      AND ID_Periodo = @periodo_maggio
      AND TipoTransazione = 'S'
      AND ID_Categoria = @cat_mensa
), @periodo_maggio, @cat_mensa, 'studente1@mail.com')
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia),
Totale_Speso_Attuale = VALUES(Totale_Speso_Attuale);

-- Budget dedicato ai libri universitari.
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
VALUES
(60.00, TRUE, (
    SELECT COALESCE(SUM(Importo), 0)
    FROM TRANSIZIONE
    WHERE Email = 'studente1@mail.com'
      AND ID_Periodo = @periodo_maggio
      AND TipoTransazione = 'S'
      AND ID_Categoria = @cat_libri
), @periodo_maggio, @cat_libri, 'studente1@mail.com')
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia),
Totale_Speso_Attuale = VALUES(Totale_Speso_Attuale);

-- Dati demo dello studente2: servono a mostrare l'isolamento dei portafogli.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 35.00, '2026-05-09', 'Abbonamento palestra', 'studente2@mail.com', @cat_palestra, @periodo_maggio, NULL);

SET @spesa_palestra = LAST_INSERT_ID();

INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES
(@spesa_palestra, @tag_essenziale);

INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 18.00, '2026-05-11', 'Spesa alimentare', 'studente2@mail.com', @cat_alimentari, @periodo_maggio, NULL);

INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('E', 180.00, '2026-05-22', 'Pagamento lavoretto weekend', 'studente2@mail.com', NULL, @periodo_maggio, @fonte_lavoretto);

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
VALUES
(150.00, TRUE, (
    SELECT COALESCE(SUM(Importo), 0)
    FROM TRANSIZIONE
    WHERE Email = 'studente2@mail.com'
      AND ID_Periodo = @periodo_maggio
      AND TipoTransazione = 'S'
), @periodo_maggio, NULL, 'studente2@mail.com')
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia),
Totale_Speso_Attuale = VALUES(Totale_Speso_Attuale);

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
VALUES
(50.00, TRUE, (
    SELECT COALESCE(SUM(Importo), 0)
    FROM TRANSIZIONE
    WHERE Email = 'studente2@mail.com'
      AND ID_Periodo = @periodo_maggio
      AND TipoTransazione = 'S'
      AND ID_Categoria = @cat_palestra
), @periodo_maggio, @cat_palestra, 'studente2@mail.com')
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia),
Totale_Speso_Attuale = VALUES(Totale_Speso_Attuale);

-- Spesa ricorrente demo per rappresentare l'abbonamento mensile ai trasporti.
INSERT INTO SPESA_RICORRENTE
(Importo_Previsto, Frequenza_Giorni, Data_Inizio, Data_Prossima_Scadenza,
 Scadenza, ID_Categoria, Email)
VALUES
(25.00, 30, '2026-05-01', '2026-06-01', '2026-12-31',
 @cat_trasporti, 'studente1@mail.com');

INSERT INTO SPESA_RICORRENTE
(Importo_Previsto, Frequenza_Giorni, Data_Inizio, Data_Prossima_Scadenza,
 Scadenza, ID_Categoria, Email)
VALUES
(35.00, 30, '2026-05-01', '2026-06-01', '2026-12-31',
 @cat_palestra, 'studente2@mail.com');
