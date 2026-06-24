-- S.A.G.E. - dati dimostrativi estesi
-- Eseguire dopo 01_schema_completo.sql soltanto se servono utenti e movimenti demo.
-- Il popolamento mantiene due account studente e un account amministratore tecnico.
-- Le transazioni coprono il periodo gennaio-giugno 2026 con almeno 7/8 movimenti al mese.

USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- Account demo: due portafogli utente più l'accesso amministratore.
INSERT INTO UTENTE (Email, Password, Nome, Cognome, Ruolo)
VALUES
('studente1@mail.com', SHA2('password1', 512), 'Mario', 'Rossi', 'UTENTE'),
('studente2@mail.com', SHA2('password2', 512), 'Luca', 'Bianchi', 'UTENTE'),
('admin@sage.com', SHA2('admin123', 512), 'Admin', 'Sistema', 'ADMIN');

-- Periodi demo: tutti i mesi da gennaio a luglio 2026. Luglio serve come prossima scadenza delle ricorrenze.
INSERT INTO PERIODO (Mese, Anno)
VALUES
(1, 2026), (2, 2026), (3, 2026), (4, 2026), (5, 2026), (6, 2026), (7, 2026);
SET @periodo_gennaio = (SELECT ID_Periodo FROM PERIODO WHERE Mese = 1 AND Anno = 2026);
SET @periodo_febbraio = (SELECT ID_Periodo FROM PERIODO WHERE Mese = 2 AND Anno = 2026);
SET @periodo_marzo = (SELECT ID_Periodo FROM PERIODO WHERE Mese = 3 AND Anno = 2026);
SET @periodo_aprile = (SELECT ID_Periodo FROM PERIODO WHERE Mese = 4 AND Anno = 2026);
SET @periodo_maggio = (SELECT ID_Periodo FROM PERIODO WHERE Mese = 5 AND Anno = 2026);
SET @periodo_giugno = (SELECT ID_Periodo FROM PERIODO WHERE Mese = 6 AND Anno = 2026);
SET @periodo_luglio = (SELECT ID_Periodo FROM PERIODO WHERE Mese = 7 AND Anno = 2026);

-- Categorie di sistema, disponibili per tutti gli utenti.
INSERT INTO CATEGORIA (Nome, Icona, is_system, Email_Proprietario)
VALUES
('Trasporti', 'transport.png', TRUE, NULL),
('Alimentari', 'food.png', TRUE, NULL),
('Casa', 'house.png', TRUE, NULL),
('Salute', 'health.png', TRUE, NULL),
('Svago', 'leisure.png', TRUE, NULL),
('Tecnologia', 'tech.png', TRUE, NULL);

-- Categorie personali dei due studenti.
INSERT INTO CATEGORIA (Nome, Icona, is_system, Email_Proprietario)
VALUES
('Mensa universitaria', 'food.png', FALSE, 'studente1@mail.com'),
('Libri universitari', 'study.png', FALSE, 'studente1@mail.com'),
('Palestra', 'gym.png', FALSE, 'studente1@mail.com'),
('Palestra', 'gym.png', FALSE, 'studente2@mail.com'),
('Università', 'study.png', FALSE, 'studente2@mail.com'),
('Casa fuori sede', 'house.png', FALSE, 'studente2@mail.com');
SET @cat_trasporti = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Trasporti' AND is_system = TRUE);
SET @cat_alimentari = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Alimentari' AND is_system = TRUE);
SET @cat_casa = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Casa' AND is_system = TRUE);
SET @cat_salute = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Salute' AND is_system = TRUE);
SET @cat_svago = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Svago' AND is_system = TRUE);
SET @cat_tecnologia = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Tecnologia' AND is_system = TRUE);
SET @cat_mensa_s1 = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Mensa universitaria' AND Email_Proprietario = 'studente1@mail.com');
SET @cat_libri_s1 = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Libri universitari' AND Email_Proprietario = 'studente1@mail.com');
SET @cat_palestra_s1 = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Palestra' AND Email_Proprietario = 'studente1@mail.com');
SET @cat_palestra_s2 = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Palestra' AND Email_Proprietario = 'studente2@mail.com');
SET @cat_universita_s2 = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Università' AND Email_Proprietario = 'studente2@mail.com');
SET @cat_casa_fuori_sede_s2 = (SELECT ID_Categoria FROM CATEGORIA WHERE Nome = 'Casa fuori sede' AND Email_Proprietario = 'studente2@mail.com');

-- Fonti di entrata di sistema e personali.
INSERT INTO FONTE (Nome, Icona, is_system, Email_Proprietario)
VALUES
('Stipendio', 'salary.png', TRUE, NULL),
('Regalo', 'gift.png', TRUE, NULL),
('Rimborso', 'refund.png', TRUE, NULL),
('Aiuto famiglia', 'gift.png', TRUE, NULL),
('Borsa di studio', 'scholarship.png', FALSE, 'studente1@mail.com'),
('Ripetizioni private', 'tutoring.png', FALSE, 'studente1@mail.com'),
('Lavoretto weekend', 'work.png', FALSE, 'studente2@mail.com'),
('Stage', 'salary.png', FALSE, 'studente2@mail.com');
SET @fonte_stipendio = (SELECT ID_Fonte FROM FONTE WHERE Nome = 'Stipendio' AND is_system = TRUE);
SET @fonte_regalo = (SELECT ID_Fonte FROM FONTE WHERE Nome = 'Regalo' AND is_system = TRUE);
SET @fonte_rimborso = (SELECT ID_Fonte FROM FONTE WHERE Nome = 'Rimborso' AND is_system = TRUE);
SET @fonte_aiuto_famiglia = (SELECT ID_Fonte FROM FONTE WHERE Nome = 'Aiuto famiglia' AND is_system = TRUE);
SET @fonte_borsa = (SELECT ID_Fonte FROM FONTE WHERE Nome = 'Borsa di studio' AND Email_Proprietario = 'studente1@mail.com');
SET @fonte_ripetizioni = (SELECT ID_Fonte FROM FONTE WHERE Nome = 'Ripetizioni private' AND Email_Proprietario = 'studente1@mail.com');
SET @fonte_lavoretto = (SELECT ID_Fonte FROM FONTE WHERE Nome = 'Lavoretto weekend' AND Email_Proprietario = 'studente2@mail.com');
SET @fonte_stage = (SELECT ID_Fonte FROM FONTE WHERE Nome = 'Stage' AND Email_Proprietario = 'studente2@mail.com');

-- Tag per classificare le spese nei grafici e negli elenchi.
INSERT INTO TAG (Nome, Icona, is_system, Email_Proprietario)
VALUES
('Università', 'study.png', TRUE, NULL),
('Extra', 'leisure.png', TRUE, NULL),
('Casa', 'house.png', TRUE, NULL),
('Trasporti', 'transport.png', TRUE, NULL),
('Esame', 'study.png', FALSE, 'studente1@mail.com'),
('Weekend', 'leisure.png', FALSE, 'studente1@mail.com'),
('Lavoro', 'work.png', FALSE, 'studente2@mail.com'),
('Sport', 'gym.png', FALSE, 'studente2@mail.com');
SET @tag_universita = (SELECT ID_Tag FROM TAG WHERE Nome = 'Università' AND is_system = TRUE);
SET @tag_extra = (SELECT ID_Tag FROM TAG WHERE Nome = 'Extra' AND is_system = TRUE);
SET @tag_casa = (SELECT ID_Tag FROM TAG WHERE Nome = 'Casa' AND is_system = TRUE);
SET @tag_trasporti = (SELECT ID_Tag FROM TAG WHERE Nome = 'Trasporti' AND is_system = TRUE);
SET @tag_esame = (SELECT ID_Tag FROM TAG WHERE Nome = 'Esame' AND Email_Proprietario = 'studente1@mail.com');
SET @tag_weekend = (SELECT ID_Tag FROM TAG WHERE Nome = 'Weekend' AND Email_Proprietario = 'studente1@mail.com');
SET @tag_lavoro = (SELECT ID_Tag FROM TAG WHERE Nome = 'Lavoro' AND Email_Proprietario = 'studente2@mail.com');
SET @tag_sport = (SELECT ID_Tag FROM TAG WHERE Nome = 'Sport' AND Email_Proprietario = 'studente2@mail.com');

-- Spese ricorrenti: vengono create prima delle transazioni così posso collegare le rate già generate.
INSERT INTO SPESA_RICORRENTE
(Nome, Importo_Previsto, Frequenza_Giorni, Data_Inizio, Data_Prossima_Scadenza, Scadenza, ID_Categoria, Email)
VALUES
('Affitto', 450.00, 30, '2026-01-15', '2026-07-15', '2026-12-31', @cat_casa, 'studente1@mail.com'),
('Netflix', 9.99, 30, '2026-01-23', '2026-07-23', '2026-12-31', @cat_svago, 'studente1@mail.com'),
('Abbonamento autobus', 25.00, 30, '2026-01-01', '2026-07-01', '2026-12-31', @cat_trasporti, 'studente1@mail.com'),
('Palestra', 55.00, 30, '2026-01-15', '2026-07-15', '2026-12-31', @cat_palestra_s1, 'studente1@mail.com'),
('Abbonamento palestra', 35.00, 30, '2026-01-05', '2026-07-05', '2026-12-31', @cat_palestra_s2, 'studente2@mail.com'),
('Stanza in affitto', 320.00, 30, '2026-01-15', '2026-07-15', '2026-12-31', @cat_casa_fuori_sede_s2, 'studente2@mail.com');

SET @ricorrenza_affitto_s1 = (SELECT ID_Ricorrenza FROM SPESA_RICORRENTE WHERE Nome = 'Affitto' AND Email = 'studente1@mail.com');
SET @ricorrenza_netflix_s1 = (SELECT ID_Ricorrenza FROM SPESA_RICORRENTE WHERE Nome = 'Netflix' AND Email = 'studente1@mail.com');
SET @ricorrenza_bus_s1 = (SELECT ID_Ricorrenza FROM SPESA_RICORRENTE WHERE Nome = 'Abbonamento autobus' AND Email = 'studente1@mail.com');
SET @ricorrenza_palestra_s1 = (SELECT ID_Ricorrenza FROM SPESA_RICORRENTE WHERE Nome = 'Palestra' AND Email = 'studente1@mail.com');
SET @ricorrenza_palestra_s2 = (SELECT ID_Ricorrenza FROM SPESA_RICORRENTE WHERE Nome = 'Abbonamento palestra' AND Email = 'studente2@mail.com');
SET @ricorrenza_affitto_s2 = (SELECT ID_Ricorrenza FROM SPESA_RICORRENTE WHERE Nome = 'Stanza in affitto' AND Email = 'studente2@mail.com');

-- Transazioni studente 1: gennaio-giugno 2026, almeno dieci movimenti per mese.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte, ID_Ricorrenza)
VALUES
('E', 250.00, '2026-01-03', 'Aiuto famiglia gennaio', 'studente1@mail.com', NULL, @periodo_gennaio, @fonte_aiuto_famiglia, NULL),
('E', 120.00, '2026-01-10', 'Ripetizioni private gennaio', 'studente1@mail.com', NULL, @periodo_gennaio, @fonte_ripetizioni, NULL),
('E', 500.00, '2026-01-20', 'Borsa di studio gennaio', 'studente1@mail.com', NULL, @periodo_gennaio, @fonte_borsa, NULL),
('S', 450.00, '2026-01-15', 'Affitto', 'studente1@mail.com', @cat_casa, @periodo_gennaio, NULL, @ricorrenza_affitto_s1),
('S', 25.00, '2026-01-01', 'Abbonamento autobus', 'studente1@mail.com', @cat_trasporti, @periodo_gennaio, NULL, @ricorrenza_bus_s1),
('S', 9.99, '2026-01-23', 'Netflix', 'studente1@mail.com', @cat_svago, @periodo_gennaio, NULL, @ricorrenza_netflix_s1),
('S', 76.40, '2026-01-07', 'Spesa supermercato', 'studente1@mail.com', @cat_alimentari, @periodo_gennaio, NULL, NULL),
('S', 12.50, '2026-01-12', 'Pranzo in mensa', 'studente1@mail.com', @cat_mensa_s1, @periodo_gennaio, NULL, NULL),
('S', 42.00, '2026-01-18', 'Manuale universitario', 'studente1@mail.com', @cat_libri_s1, @periodo_gennaio, NULL, NULL),
('S', 55.00, '2026-01-15', 'Palestra', 'studente1@mail.com', @cat_palestra_s1, @periodo_gennaio, NULL, @ricorrenza_palestra_s1),
('E', 250.00, '2026-02-03', 'Aiuto famiglia febbraio', 'studente1@mail.com', NULL, @periodo_febbraio, @fonte_aiuto_famiglia, NULL),
('E', 90.00, '2026-02-09', 'Ripetizioni private febbraio', 'studente1@mail.com', NULL, @periodo_febbraio, @fonte_ripetizioni, NULL),
('E', 40.00, '2026-02-21', 'Rimborso materiale corso', 'studente1@mail.com', NULL, @periodo_febbraio, @fonte_rimborso, NULL),
('S', 450.00, '2026-02-15', 'Affitto', 'studente1@mail.com', @cat_casa, @periodo_febbraio, NULL, @ricorrenza_affitto_s1),
('S', 25.00, '2026-02-01', 'Abbonamento autobus', 'studente1@mail.com', @cat_trasporti, @periodo_febbraio, NULL, @ricorrenza_bus_s1),
('S', 9.99, '2026-02-23', 'Netflix', 'studente1@mail.com', @cat_svago, @periodo_febbraio, NULL, @ricorrenza_netflix_s1),
('S', 84.30, '2026-02-06', 'Spesa supermercato', 'studente1@mail.com', @cat_alimentari, @periodo_febbraio, NULL, NULL),
('S', 15.00, '2026-02-13', 'Mensa universitaria', 'studente1@mail.com', @cat_mensa_s1, @periodo_febbraio, NULL, NULL),
('S', 28.00, '2026-02-18', 'Fotocopie appunti', 'studente1@mail.com', @cat_libri_s1, @periodo_febbraio, NULL, NULL),
('S', 18.90, '2026-02-25', 'Farmacia', 'studente1@mail.com', @cat_salute, @periodo_febbraio, NULL, NULL),
('S', 55.00, '2026-02-15', 'Palestra', 'studente1@mail.com', @cat_palestra_s1, @periodo_febbraio, NULL, @ricorrenza_palestra_s1),
('E', 250.00, '2026-03-03', 'Aiuto famiglia marzo', 'studente1@mail.com', NULL, @periodo_marzo, @fonte_aiuto_famiglia, NULL),
('E', 110.00, '2026-03-11', 'Ripetizioni private marzo', 'studente1@mail.com', NULL, @periodo_marzo, @fonte_ripetizioni, NULL),
('E', 55.00, '2026-03-24', 'Regalo compleanno', 'studente1@mail.com', NULL, @periodo_marzo, @fonte_regalo, NULL),
('S', 450.00, '2026-03-15', 'Affitto', 'studente1@mail.com', @cat_casa, @periodo_marzo, NULL, @ricorrenza_affitto_s1),
('S', 25.00, '2026-03-01', 'Abbonamento autobus', 'studente1@mail.com', @cat_trasporti, @periodo_marzo, NULL, @ricorrenza_bus_s1),
('S', 9.99, '2026-03-23', 'Netflix', 'studente1@mail.com', @cat_svago, @periodo_marzo, NULL, @ricorrenza_netflix_s1),
('S', 93.20, '2026-03-05', 'Spesa supermercato', 'studente1@mail.com', @cat_alimentari, @periodo_marzo, NULL, NULL),
('S', 11.80, '2026-03-12', 'Pranzo in mensa', 'studente1@mail.com', @cat_mensa_s1, @periodo_marzo, NULL, NULL),
('S', 36.50, '2026-03-17', 'Libro per esame', 'studente1@mail.com', @cat_libri_s1, @periodo_marzo, NULL, NULL),
('S', 21.00, '2026-03-29', 'Cinema con amici', 'studente1@mail.com', @cat_svago, @periodo_marzo, NULL, NULL),
('S', 55.00, '2026-03-15', 'Palestra', 'studente1@mail.com', @cat_palestra_s1, @periodo_marzo, NULL, @ricorrenza_palestra_s1),
('E', 250.00, '2026-04-03', 'Aiuto famiglia aprile', 'studente1@mail.com', NULL, @periodo_aprile, @fonte_aiuto_famiglia, NULL),
('E', 140.00, '2026-04-12', 'Ripetizioni private aprile', 'studente1@mail.com', NULL, @periodo_aprile, @fonte_ripetizioni, NULL),
('E', 60.00, '2026-04-26', 'Rimborso treno', 'studente1@mail.com', NULL, @periodo_aprile, @fonte_rimborso, NULL),
('S', 450.00, '2026-04-15', 'Affitto', 'studente1@mail.com', @cat_casa, @periodo_aprile, NULL, @ricorrenza_affitto_s1),
('S', 25.00, '2026-04-01', 'Abbonamento autobus', 'studente1@mail.com', @cat_trasporti, @periodo_aprile, NULL, @ricorrenza_bus_s1),
('S', 9.99, '2026-04-23', 'Netflix', 'studente1@mail.com', @cat_svago, @periodo_aprile, NULL, @ricorrenza_netflix_s1),
('S', 101.10, '2026-04-07', 'Spesa supermercato', 'studente1@mail.com', @cat_alimentari, @periodo_aprile, NULL, NULL),
('S', 13.40, '2026-04-14', 'Pranzo in mensa', 'studente1@mail.com', @cat_mensa_s1, @periodo_aprile, NULL, NULL),
('S', 65.00, '2026-04-20', 'Corso online universitario', 'studente1@mail.com', @cat_libri_s1, @periodo_aprile, NULL, NULL),
('S', 32.00, '2026-04-28', 'Cena fuori', 'studente1@mail.com', @cat_svago, @periodo_aprile, NULL, NULL),
('S', 55.00, '2026-04-15', 'Palestra', 'studente1@mail.com', @cat_palestra_s1, @periodo_aprile, NULL, @ricorrenza_palestra_s1),
('E', 250.00, '2026-05-03', 'Aiuto famiglia maggio', 'studente1@mail.com', NULL, @periodo_maggio, @fonte_aiuto_famiglia, NULL),
('E', 120.00, '2026-05-20', 'Pagamento ripetizioni private', 'studente1@mail.com', NULL, @periodo_maggio, @fonte_ripetizioni, NULL),
('E', 500.00, '2026-05-01', 'Accredito borsa di studio', 'studente1@mail.com', NULL, @periodo_maggio, @fonte_borsa, NULL),
('S', 450.00, '2026-05-15', 'Affitto', 'studente1@mail.com', @cat_casa, @periodo_maggio, NULL, @ricorrenza_affitto_s1),
('S', 25.00, '2026-05-01', 'Abbonamento autobus', 'studente1@mail.com', @cat_trasporti, @periodo_maggio, NULL, @ricorrenza_bus_s1),
('S', 9.99, '2026-05-23', 'Netflix', 'studente1@mail.com', @cat_svago, @periodo_maggio, NULL, @ricorrenza_netflix_s1),
('S', 8.50, '2026-05-13', 'Pranzo in mensa', 'studente1@mail.com', @cat_mensa_s1, @periodo_maggio, NULL, NULL),
('S', 42.00, '2026-05-15', 'Acquisto libro universitario', 'studente1@mail.com', @cat_libri_s1, @periodo_maggio, NULL, NULL),
('S', 118.00, '2026-05-08', 'Spesa alimentare mensile', 'studente1@mail.com', @cat_alimentari, @periodo_maggio, NULL, NULL),
('S', 19.50, '2026-05-27', 'Aperitivo universitario', 'studente1@mail.com', @cat_svago, @periodo_maggio, NULL, NULL),
('S', 55.00, '2026-05-15', 'Palestra', 'studente1@mail.com', @cat_palestra_s1, @periodo_maggio, NULL, @ricorrenza_palestra_s1),
('E', 250.00, '2026-06-03', 'Aiuto famiglia giugno', 'studente1@mail.com', NULL, @periodo_giugno, @fonte_aiuto_famiglia, NULL),
('E', 50.00, '2026-06-02', 'Regalo famiglia', 'studente1@mail.com', NULL, @periodo_giugno, @fonte_regalo, NULL),
('E', 180.00, '2026-06-21', 'Ripetizioni private giugno', 'studente1@mail.com', NULL, @periodo_giugno, @fonte_ripetizioni, NULL),
('S', 450.00, '2026-06-15', 'Affitto', 'studente1@mail.com', @cat_casa, @periodo_giugno, NULL, @ricorrenza_affitto_s1),
('S', 25.00, '2026-06-01', 'Abbonamento autobus', 'studente1@mail.com', @cat_trasporti, @periodo_giugno, NULL, @ricorrenza_bus_s1),
('S', 9.99, '2026-06-23', 'Netflix', 'studente1@mail.com', @cat_svago, @periodo_giugno, NULL, @ricorrenza_netflix_s1),
('S', 129.00, '2026-06-04', 'Spesa supermercato', 'studente1@mail.com', @cat_alimentari, @periodo_giugno, NULL, NULL),
('S', 14.20, '2026-06-11', 'Pranzo in mensa', 'studente1@mail.com', @cat_mensa_s1, @periodo_giugno, NULL, NULL),
('S', 55.00, '2026-06-15', 'Palestra', 'studente1@mail.com', @cat_palestra_s1, @periodo_giugno, NULL, @ricorrenza_palestra_s1),
('S', 29.30, '2026-06-26', 'Medicinali', 'studente1@mail.com', @cat_salute, @periodo_giugno, NULL, NULL);

-- Transazioni studente 2: gennaio-giugno 2026, almeno otto movimenti per mese.
INSERT INTO TRANSIZIONE
(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte, ID_Ricorrenza)
VALUES
('E', 180.00, '2026-01-06', 'Lavoretto weekend gennaio', 'studente2@mail.com', NULL, @periodo_gennaio, @fonte_lavoretto, NULL),
('E', 300.00, '2026-01-28', 'Stage gennaio', 'studente2@mail.com', NULL, @periodo_gennaio, @fonte_stage, NULL),
('S', 320.00, '2026-01-15', 'Stanza in affitto', 'studente2@mail.com', @cat_casa_fuori_sede_s2, @periodo_gennaio, NULL, @ricorrenza_affitto_s2),
('S', 35.00, '2026-01-05', 'Abbonamento palestra', 'studente2@mail.com', @cat_palestra_s2, @periodo_gennaio, NULL, @ricorrenza_palestra_s2),
('S', 72.00, '2026-01-08', 'Spesa alimentare', 'studente2@mail.com', @cat_alimentari, @periodo_gennaio, NULL, NULL),
('S', 22.00, '2026-01-12', 'Biglietto autobus', 'studente2@mail.com', @cat_trasporti, @periodo_gennaio, NULL, NULL),
('S', 18.00, '2026-01-19', 'Cinema', 'studente2@mail.com', @cat_svago, @periodo_gennaio, NULL, NULL),
('S', 30.00, '2026-01-25', 'Dispense universitarie', 'studente2@mail.com', @cat_universita_s2, @periodo_gennaio, NULL, NULL),
('E', 200.00, '2026-02-07', 'Lavoretto weekend febbraio', 'studente2@mail.com', NULL, @periodo_febbraio, @fonte_lavoretto, NULL),
('E', 300.00, '2026-02-28', 'Stage febbraio', 'studente2@mail.com', NULL, @periodo_febbraio, @fonte_stage, NULL),
('S', 320.00, '2026-02-15', 'Stanza in affitto', 'studente2@mail.com', @cat_casa_fuori_sede_s2, @periodo_febbraio, NULL, @ricorrenza_affitto_s2),
('S', 35.00, '2026-02-05', 'Abbonamento palestra', 'studente2@mail.com', @cat_palestra_s2, @periodo_febbraio, NULL, @ricorrenza_palestra_s2),
('S', 68.50, '2026-02-09', 'Spesa alimentare', 'studente2@mail.com', @cat_alimentari, @periodo_febbraio, NULL, NULL),
('S', 22.00, '2026-02-12', 'Biglietto autobus', 'studente2@mail.com', @cat_trasporti, @periodo_febbraio, NULL, NULL),
('S', 16.00, '2026-02-18', 'Pizza con amici', 'studente2@mail.com', @cat_svago, @periodo_febbraio, NULL, NULL),
('S', 24.00, '2026-02-26', 'Quaderno e cancelleria', 'studente2@mail.com', @cat_universita_s2, @periodo_febbraio, NULL, NULL),
('E', 210.00, '2026-03-07', 'Lavoretto weekend marzo', 'studente2@mail.com', NULL, @periodo_marzo, @fonte_lavoretto, NULL),
('E', 300.00, '2026-03-28', 'Stage marzo', 'studente2@mail.com', NULL, @periodo_marzo, @fonte_stage, NULL),
('S', 320.00, '2026-03-15', 'Stanza in affitto', 'studente2@mail.com', @cat_casa_fuori_sede_s2, @periodo_marzo, NULL, @ricorrenza_affitto_s2),
('S', 35.00, '2026-03-05', 'Abbonamento palestra', 'studente2@mail.com', @cat_palestra_s2, @periodo_marzo, NULL, @ricorrenza_palestra_s2),
('S', 74.20, '2026-03-08', 'Spesa alimentare', 'studente2@mail.com', @cat_alimentari, @periodo_marzo, NULL, NULL),
('S', 22.00, '2026-03-12', 'Biglietto autobus', 'studente2@mail.com', @cat_trasporti, @periodo_marzo, NULL, NULL),
('S', 20.00, '2026-03-18', 'Serata svago', 'studente2@mail.com', @cat_svago, @periodo_marzo, NULL, NULL),
('S', 38.00, '2026-03-22', 'Libro laboratorio', 'studente2@mail.com', @cat_universita_s2, @periodo_marzo, NULL, NULL),
('E', 190.00, '2026-04-07', 'Lavoretto weekend aprile', 'studente2@mail.com', NULL, @periodo_aprile, @fonte_lavoretto, NULL),
('E', 300.00, '2026-04-28', 'Stage aprile', 'studente2@mail.com', NULL, @periodo_aprile, @fonte_stage, NULL),
('S', 320.00, '2026-04-15', 'Stanza in affitto', 'studente2@mail.com', @cat_casa_fuori_sede_s2, @periodo_aprile, NULL, @ricorrenza_affitto_s2),
('S', 35.00, '2026-04-05', 'Abbonamento palestra', 'studente2@mail.com', @cat_palestra_s2, @periodo_aprile, NULL, @ricorrenza_palestra_s2),
('S', 82.70, '2026-04-08', 'Spesa alimentare', 'studente2@mail.com', @cat_alimentari, @periodo_aprile, NULL, NULL),
('S', 22.00, '2026-04-12', 'Biglietto autobus', 'studente2@mail.com', @cat_trasporti, @periodo_aprile, NULL, NULL),
('S', 25.00, '2026-04-18', 'Concerto universitario', 'studente2@mail.com', @cat_svago, @periodo_aprile, NULL, NULL),
('S', 19.00, '2026-04-24', 'Stampa appunti', 'studente2@mail.com', @cat_universita_s2, @periodo_aprile, NULL, NULL),
('E', 180.00, '2026-05-22', 'Pagamento lavoretto weekend', 'studente2@mail.com', NULL, @periodo_maggio, @fonte_lavoretto, NULL),
('E', 300.00, '2026-05-28', 'Stage maggio', 'studente2@mail.com', NULL, @periodo_maggio, @fonte_stage, NULL),
('S', 320.00, '2026-05-15', 'Stanza in affitto', 'studente2@mail.com', @cat_casa_fuori_sede_s2, @periodo_maggio, NULL, @ricorrenza_affitto_s2),
('S', 35.00, '2026-05-09', 'Abbonamento palestra', 'studente2@mail.com', @cat_palestra_s2, @periodo_maggio, NULL, @ricorrenza_palestra_s2),
('S', 18.00, '2026-05-11', 'Spesa alimentare', 'studente2@mail.com', @cat_alimentari, @periodo_maggio, NULL, NULL),
('S', 22.00, '2026-05-12', 'Biglietto autobus', 'studente2@mail.com', @cat_trasporti, @periodo_maggio, NULL, NULL),
('S', 14.00, '2026-05-18', 'Gelato con amici', 'studente2@mail.com', @cat_svago, @periodo_maggio, NULL, NULL),
('S', 27.00, '2026-05-24', 'Materiale universitario', 'studente2@mail.com', @cat_universita_s2, @periodo_maggio, NULL, NULL),
('E', 220.00, '2026-06-07', 'Lavoretto weekend giugno', 'studente2@mail.com', NULL, @periodo_giugno, @fonte_lavoretto, NULL),
('E', 300.00, '2026-06-28', 'Stage giugno', 'studente2@mail.com', NULL, @periodo_giugno, @fonte_stage, NULL),
('S', 320.00, '2026-06-15', 'Stanza in affitto', 'studente2@mail.com', @cat_casa_fuori_sede_s2, @periodo_giugno, NULL, @ricorrenza_affitto_s2),
('S', 35.00, '2026-06-05', 'Abbonamento palestra', 'studente2@mail.com', @cat_palestra_s2, @periodo_giugno, NULL, @ricorrenza_palestra_s2),
('S', 79.90, '2026-06-08', 'Spesa alimentare', 'studente2@mail.com', @cat_alimentari, @periodo_giugno, NULL, NULL),
('S', 22.00, '2026-06-12', 'Biglietto autobus', 'studente2@mail.com', @cat_trasporti, @periodo_giugno, NULL, NULL),
('S', 21.00, '2026-06-18', 'Cinema', 'studente2@mail.com', @cat_svago, @periodo_giugno, NULL, NULL),
('S', 33.00, '2026-06-24', 'Libro corso estivo', 'studente2@mail.com', @cat_universita_s2, @periodo_giugno, NULL, NULL);

-- Le transazioni ricorrenti hanno ID_Ricorrenza valorizzato direttamente nell'INSERT.
-- In termini logici equivale a: SET ID_Ricorrenza = modello della ricorrenza.

-- Tag automatici sulle spese demo: servono a rendere più ricca la schermata categorie/tag.
INSERT IGNORE INTO SPESA_TAG (ID_Transizione, ID_Tag)
SELECT ID_Transizione, @tag_universita
FROM TRANSIZIONE
WHERE TipoTransazione = 'S'
  AND Email = 'studente1@mail.com'
  AND ID_Categoria IN (@cat_mensa_s1, @cat_libri_s1);

INSERT IGNORE INTO SPESA_TAG (ID_Transizione, ID_Tag)
SELECT ID_Transizione, @tag_trasporti
FROM TRANSIZIONE
WHERE TipoTransazione = 'S' AND ID_Categoria = @cat_trasporti;

INSERT IGNORE INTO SPESA_TAG (ID_Transizione, ID_Tag)
SELECT ID_Transizione, @tag_extra
FROM TRANSIZIONE
WHERE TipoTransazione = 'S' AND ID_Categoria = @cat_svago;

INSERT IGNORE INTO SPESA_TAG (ID_Transizione, ID_Tag)
SELECT ID_Transizione, @tag_sport
FROM TRANSIZIONE
WHERE TipoTransazione = 'S'
  AND Email = 'studente2@mail.com'
  AND ID_Categoria = @cat_palestra_s2;

INSERT IGNORE INTO SPESA_TAG (ID_Transizione, ID_Tag)
SELECT ID_Transizione, @tag_casa
FROM TRANSIZIONE
WHERE TipoTransazione = 'S'
  AND ID_Categoria IN (@cat_casa, @cat_casa_fuori_sede_s2);

-- Alcuni documenti demo su spese realmente presenti.
INSERT INTO DOCUMENTO (ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento)
SELECT ID_Transizione, '/documenti/scontrino_mensa_gennaio.pdf', 'PDF', Data
FROM TRANSIZIONE
WHERE Email = 'studente1@mail.com' AND Descrizione = 'Pranzo in mensa' AND Data = '2026-01-12'
LIMIT 1;

INSERT INTO DOCUMENTO (ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento)
SELECT ID_Transizione, '/documenti/manuale_universitario.pdf', 'PDF', Data
FROM TRANSIZIONE
WHERE Email = 'studente1@mail.com' AND Descrizione = 'Manuale universitario' AND Data = '2026-01-18'
LIMIT 1;

INSERT INTO DOCUMENTO (ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento)
SELECT ID_Transizione, '/documenti/scontrino_supermercato_giugno.jpg', 'JPG', Data
FROM TRANSIZIONE
WHERE Email = 'studente1@mail.com' AND Descrizione = 'Spesa supermercato' AND Data = '2026-06-04'
LIMIT 1;

INSERT INTO DOCUMENTO (ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento)
SELECT ID_Transizione, '/documenti/libro_laboratorio.pdf', 'PDF', Data
FROM TRANSIZIONE
WHERE Email = 'studente2@mail.com' AND Descrizione = 'Libro laboratorio' AND Data = '2026-03-22'
LIMIT 1;

-- Budget demo unici: un solo limite per ciascun ambito.
-- L'applicazione li ricalcola mese per mese usando il periodo corrente.
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 750.00, TRUE, (
    SELECT COALESCE(SUM(T.Importo), 0)
    FROM TRANSIZIONE T
    WHERE T.Email = 'studente1@mail.com'
      AND T.ID_Periodo = @periodo_giugno
      AND T.TipoTransazione = 'S'
), @periodo_giugno, NULL, 'studente1@mail.com';

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 750.00, TRUE, (
    SELECT COALESCE(SUM(T.Importo), 0)
    FROM TRANSIZIONE T
    WHERE T.Email = 'studente2@mail.com'
      AND T.ID_Periodo = @periodo_giugno
      AND T.TipoTransazione = 'S'
), @periodo_giugno, NULL, 'studente2@mail.com';

-- Budget per categoria dello studente 1: una sola riga per categoria, non una per mese.
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 500.00, TRUE, (SELECT COALESCE(SUM(T.Importo), 0) FROM TRANSIZIONE T WHERE T.Email = 'studente1@mail.com' AND T.ID_Periodo = @periodo_giugno AND T.TipoTransazione = 'S' AND T.ID_Categoria = @cat_casa), @periodo_giugno, @cat_casa, 'studente1@mail.com';

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 180.00, TRUE, (SELECT COALESCE(SUM(T.Importo), 0) FROM TRANSIZIONE T WHERE T.Email = 'studente1@mail.com' AND T.ID_Periodo = @periodo_giugno AND T.TipoTransazione = 'S' AND T.ID_Categoria = @cat_alimentari), @periodo_giugno, @cat_alimentari, 'studente1@mail.com';

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 120.00, TRUE, (SELECT COALESCE(SUM(T.Importo), 0) FROM TRANSIZIONE T WHERE T.Email = 'studente1@mail.com' AND T.ID_Periodo = @periodo_giugno AND T.TipoTransazione = 'S' AND T.ID_Categoria = @cat_mensa_s1), @periodo_giugno, @cat_mensa_s1, 'studente1@mail.com';

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 110.00, TRUE, (SELECT COALESCE(SUM(T.Importo), 0) FROM TRANSIZIONE T WHERE T.Email = 'studente1@mail.com' AND T.ID_Periodo = @periodo_giugno AND T.TipoTransazione = 'S' AND T.ID_Categoria = @cat_libri_s1), @periodo_giugno, @cat_libri_s1, 'studente1@mail.com';

-- Budget per categoria dello studente 2: una sola riga per categoria, non una per mese.
INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 360.00, TRUE, (SELECT COALESCE(SUM(T.Importo), 0) FROM TRANSIZIONE T WHERE T.Email = 'studente2@mail.com' AND T.ID_Periodo = @periodo_giugno AND T.TipoTransazione = 'S' AND T.ID_Categoria = @cat_casa_fuori_sede_s2), @periodo_giugno, @cat_casa_fuori_sede_s2, 'studente2@mail.com';

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 120.00, TRUE, (SELECT COALESCE(SUM(T.Importo), 0) FROM TRANSIZIONE T WHERE T.Email = 'studente2@mail.com' AND T.ID_Periodo = @periodo_giugno AND T.TipoTransazione = 'S' AND T.ID_Categoria = @cat_alimentari), @periodo_giugno, @cat_alimentari, 'studente2@mail.com';

INSERT INTO BUDGET
(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email)
SELECT 60.00, TRUE, (SELECT COALESCE(SUM(T.Importo), 0) FROM TRANSIZIONE T WHERE T.Email = 'studente2@mail.com' AND T.ID_Periodo = @periodo_giugno AND T.TipoTransazione = 'S' AND T.ID_Categoria = @cat_palestra_s2), @periodo_giugno, @cat_palestra_s2, 'studente2@mail.com';
