-- =====================================================
-- SEGMENTO 03: POPOLAMENTO INIZIALE DEL DATABASE
-- =====================================================

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- ======================
-- POPOLAMENTO INIZIALE
-- ======================

-- Inserimento utenti
INSERT INTO UTENTE (Email, Password, Nome, Cognome, Ruolo)
VALUES
('studente1@mail.com', SHA2('password1', 512), 'Mario', 'Rossi', 'UTENTE'),
('studente2@mail.com', SHA2('password2', 512), 'Luca', 'Bianchi', 'UTENTE'),
('admin@sage.com', SHA2('admin123', 512), 'Admin', 'Sistema', 'ADMIN');

-- Inserimento periodi
INSERT INTO PERIODO (Mese, Anno)
VALUES 
(5, 2026),
(6, 2026),
(7, 2026);

-- Recupero identificativo periodo maggio 2026
SET @periodo_maggio = (
    SELECT ID_Periodo 
    FROM PERIODO 
    WHERE Mese = 5 AND Anno = 2026
);

-- Recupero identificativo periodo giugno 2026
SET @periodo_giugno = (
    SELECT ID_Periodo 
    FROM PERIODO 
    WHERE Mese = 6 AND Anno = 2026
);

-- Recupero identificativo periodo luglio 2026
SET @periodo_luglio = (
    SELECT ID_Periodo 
    FROM PERIODO 
    WHERE Mese = 7 AND Anno = 2026
);

-- Inserimento categorie di sistema
INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario)
VALUES
('Trasporti', TRUE, NULL),
('Alimentari', TRUE, NULL),
('Casa', TRUE, NULL),
('Salute', TRUE, NULL),
('Svago', TRUE, NULL);

-- Inserimento categorie personali
INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario)
VALUES
('Mensa universitaria', FALSE, 'studente1@mail.com'),
('Libri universitari', FALSE, 'studente1@mail.com'),
('Palestra', FALSE, 'studente2@mail.com');

-- Recupero identificativo categoria Trasporti
SET @cat_trasporti = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Trasporti' AND is_system = TRUE
);

-- Recupero identificativo categoria Alimentari
SET @cat_alimentari = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Alimentari' AND is_system = TRUE
);

-- Recupero identificativo categoria Mensa universitaria
SET @cat_mensa = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Mensa universitaria' 
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Recupero identificativo categoria Libri universitari
SET @cat_libri = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Libri universitari' 
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Recupero identificativo categoria Palestra
SET @cat_palestra = (
    SELECT ID_Categoria 
    FROM CATEGORIA 
    WHERE Nome = 'Palestra' 
      AND Email_Proprietario = 'studente2@mail.com'
);

-- Inserimento fonti di sistema
INSERT INTO FONTE (Nome, is_system, Email_Proprietario)
VALUES
('Stipendio', TRUE, NULL),
('Regalo', TRUE, NULL),
('Rimborso', TRUE, NULL);

-- Inserimento fonti personali
INSERT INTO FONTE (Nome, is_system, Email_Proprietario)
VALUES
('Borsa di studio', FALSE, 'studente1@mail.com'),
('Ripetizioni private', FALSE, 'studente1@mail.com');

-- Recupero identificativo fonte Borsa di studio
SET @fonte_borsa = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Borsa di studio'
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Recupero identificativo fonte Ripetizioni private
SET @fonte_ripetizioni = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Ripetizioni private'
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Recupero identificativo fonte Regalo
SET @fonte_regalo = (
    SELECT ID_Fonte
    FROM FONTE
    WHERE Nome = 'Regalo'
      AND is_system = TRUE
);

-- Inserimento tag di sistema
INSERT INTO TAG (Nome, is_system, Email_Proprietario)
VALUES
('Essenziale', TRUE, NULL),
('Università', TRUE, NULL),
('Extra', TRUE, NULL);

-- Inserimento tag personali
INSERT INTO TAG (Nome, is_system, Email_Proprietario)
VALUES
('Esame', FALSE, 'studente1@mail.com'),
('Weekend', FALSE, 'studente1@mail.com');

-- Recupero identificativo tag Essenziale
SET @tag_essenziale = (
    SELECT ID_Tag
    FROM TAG
    WHERE Nome = 'Essenziale'
      AND is_system = TRUE
);

-- Recupero identificativo tag Università
SET @tag_universita = (
    SELECT ID_Tag
    FROM TAG
    WHERE Nome = 'Università'
      AND is_system = TRUE
);

-- Recupero identificativo tag Esame
SET @tag_esame = (
    SELECT ID_Tag
    FROM TAG
    WHERE Nome = 'Esame'
      AND Email_Proprietario = 'studente1@mail.com'
);

-- Inserimento spesa: pranzo in mensa
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 8.50, '2026-05-13', 'Pranzo in mensa', 'studente1@mail.com', @cat_mensa, @periodo_maggio, NULL);

SET @spesa_mensa = LAST_INSERT_ID();

-- Associazione tag alla spesa mensa
INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES
(@spesa_mensa, @tag_universita),
(@spesa_mensa, @tag_essenziale);

-- Associazione documento alla spesa mensa
INSERT INTO DOCUMENTO
(ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento)
VALUES
(@spesa_mensa, '/documenti/scontrino_mensa.pdf', 'PDF', '2026-05-13');

-- Inserimento spesa: acquisto libro universitario
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 42.00, '2026-05-15', 'Acquisto libro universitario', 'studente1@mail.com', @cat_libri, @periodo_maggio, NULL);

SET @spesa_libro = LAST_INSERT_ID();

-- Associazione tag alla spesa libro
INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES
(@spesa_libro, @tag_universita),
(@spesa_libro, @tag_esame);

-- Inserimento spesa: abbonamento autobus
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('S', 25.00, '2026-05-18', 'Abbonamento autobus', 'studente1@mail.com', @cat_trasporti, @periodo_maggio, NULL);

SET @spesa_autobus = LAST_INSERT_ID();

-- Associazione tag alla spesa autobus
INSERT INTO spesa_tag (ID_Transizione, ID_Tag)
VALUES
(@spesa_autobus, @tag_essenziale);

-- Inserimento entrata: borsa di studio
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('E', 500.00, '2026-05-01', 'Accredito borsa di studio', 'studente1@mail.com', NULL, @periodo_maggio, @fonte_borsa);

-- Inserimento entrata: ripetizioni private
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('E', 120.00, '2026-05-20', 'Pagamento ripetizioni private', 'studente1@mail.com', NULL, @periodo_maggio, @fonte_ripetizioni);

-- Inserimento entrata: regalo famiglia
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte)
VALUES
('E', 50.00, '2026-06-02', 'Regalo famiglia', 'studente1@mail.com', NULL, @periodo_giugno, @fonte_regalo);

-- Inserimento budget globale mensile
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, ID_Periodo, ID_Categoria, Email)
VALUES
(300.00, TRUE, @periodo_maggio, NULL, 'studente1@mail.com')
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia);

-- Inserimento budget specifico per mensa
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, ID_Periodo, ID_Categoria, Email)
VALUES
(120.00, TRUE, @periodo_maggio, @cat_mensa, 'studente1@mail.com')
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia);

-- Inserimento budget specifico per libri universitari
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, ID_Periodo, ID_Categoria, Email)
VALUES
(60.00, TRUE, @periodo_maggio, @cat_libri, 'studente1@mail.com')
ON DUPLICATE KEY UPDATE
Importo_Limite = VALUES(Importo_Limite),
Alert_Soglia = VALUES(Alert_Soglia);

-- Inserimento spesa ricorrente
INSERT INTO SPESA_RICORRENTE
(Importo_Previsto, Frequenza_Giorni, Data_Inizio, Scadenza, ID_Categoria, Email)
VALUES
(25.00, 30, '2026-05-01', '2026-12-31', @cat_trasporti, 'studente1@mail.com');