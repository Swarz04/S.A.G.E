-- *****
-- * Standard SQL generation - fixed for MySQL
-- *****

DROP DATABASE IF EXISTS Concettuale_Sistema_di_Analisi_e_Gestione_delle_spese_personali;
CREATE DATABASE Concettuale_Sistema_di_Analisi_e_Gestione_delle_spese_personali;
USE Concettuale_Sistema_di_Analisi_e_Gestione_delle_spese_personali;

-- Tables Section

CREATE TABLE BUDGETMENSILE (
     ID_BUD NUMERIC(10) NOT NULL,
     importo_massimo CHAR(1) NOT NULL,
     mese CHAR(1) NOT NULL,
     anno CHAR(1) NOT NULL,
     email_utente CHAR(1) NOT NULL,
     CONSTRAINT ID_BUDGETMENSILE_ID PRIMARY KEY (ID_BUD)
);

CREATE TABLE CATEGORIA (
     nome CHAR(1) NOT NULL,
     descrizione CHAR(1) NOT NULL,
     email_utente CHAR(1),
     CONSTRAINT ID_CATEGORIA_ID PRIMARY KEY (nome)
);

CREATE TABLE PERIODO (
     mese CHAR(1) NOT NULL,
     anno CHAR(1) NOT NULL,
     CONSTRAINT ID_PERIODO_ID PRIMARY KEY (mese, anno)
);

CREATE TABLE TAG (
     nome CHAR(1) NOT NULL,
     email_utente CHAR(1),
     CONSTRAINT ID_TAG_ID PRIMARY KEY (nome)
);

CREATE TABLE Utente (
     nome CHAR(1) NOT NULL,
     cognome CHAR(1) NOT NULL,
     email_utente CHAR(1) NOT NULL,
     password CHAR(1) NOT NULL,
     CONSTRAINT ID_Utente_ID PRIMARY KEY (email_utente)
);

CREATE TABLE FonteEntrata (
     nome CHAR(1) NOT NULL,
     tipo_stabilita CHAR(1) NOT NULL,
     email_utente CHAR(1) NOT NULL,
     CONSTRAINT ID_FonteEntrata_ID PRIMARY KEY (nome)
);

CREATE TABLE BUDGETCATEGORIA (
     importo_limite CHAR(1) NOT NULL,
     nome CHAR(1) NOT NULL,
     ID_BUD NUMERIC(10) NOT NULL
);

CREATE TABLE ENTRATA (
     email CHAR(1) NOT NULL,
     importo CHAR(1) NOT NULL,
     data CHAR(1) NOT NULL,
     descrizione CHAR(1) NOT NULL,
     email_utente CHAR(1) NOT NULL,
     mese CHAR(1) NOT NULL,
     anno CHAR(1) NOT NULL,
     nome CHAR(1) NOT NULL,
     CONSTRAINT ID_ENTRATA_ID PRIMARY KEY (email, importo, data)
);

CREATE TABLE SPESA (
     email CHAR(1) NOT NULL,
     importo CHAR(1) NOT NULL,
     data CHAR(1) NOT NULL,
     descrizione CHAR(1) NOT NULL,
     email_utente CHAR(1) NOT NULL,
     mese CHAR(1) NOT NULL,
     anno CHAR(1) NOT NULL,
     nome CHAR(1) NOT NULL,
     CONSTRAINT ID_SPESA_ID PRIMARY KEY (email, importo, data)
);

CREATE TABLE HA (
     email CHAR(1) NOT NULL,
     importo CHAR(1) NOT NULL,
     data CHAR(1) NOT NULL,
     nome CHAR(1) NOT NULL,
     CONSTRAINT ID_HA_ID PRIMARY KEY (nome, email, importo, data)
);

-- Constraints Section

ALTER TABLE BUDGETMENSILE ADD CONSTRAINT REF_BUDGE_PERIO_FK
     FOREIGN KEY (mese, anno)
     REFERENCES PERIODO (mese, anno);

ALTER TABLE BUDGETMENSILE ADD CONSTRAINT REF_BUDGE_Utent_FK
     FOREIGN KEY (email_utente)
     REFERENCES Utente (email_utente);

ALTER TABLE CATEGORIA ADD CONSTRAINT REF_CATEG_Utent_FK
     FOREIGN KEY (email_utente)
     REFERENCES Utente (email_utente);

ALTER TABLE TAG ADD CONSTRAINT REF_TAG_Utent_FK
     FOREIGN KEY (email_utente)
     REFERENCES Utente (email_utente);

ALTER TABLE FonteEntrata ADD CONSTRAINT REF_Fonte_Utent_FK
     FOREIGN KEY (email_utente)
     REFERENCES Utente (email_utente);

ALTER TABLE BUDGETCATEGORIA ADD CONSTRAINT REF_BUDGE_CATEG_FK
     FOREIGN KEY (nome)
     REFERENCES CATEGORIA (nome);

ALTER TABLE BUDGETCATEGORIA ADD CONSTRAINT EQU_BUDGE_BUDGE_FK
     FOREIGN KEY (ID_BUD)
     REFERENCES BUDGETMENSILE (ID_BUD);

ALTER TABLE ENTRATA ADD CONSTRAINT REF_ENTRA_Utent_FK
     FOREIGN KEY (email_utente)
     REFERENCES Utente (email_utente);

ALTER TABLE ENTRATA ADD CONSTRAINT REF_ENTRA_PERIO_FK
     FOREIGN KEY (mese, anno)
     REFERENCES PERIODO (mese, anno);

ALTER TABLE ENTRATA ADD CONSTRAINT REF_ENTRA_Fonte_FK
     FOREIGN KEY (nome)
     REFERENCES FonteEntrata (nome);

ALTER TABLE SPESA ADD CONSTRAINT REF_SPESA_Utent_FK
     FOREIGN KEY (email_utente)
     REFERENCES Utente (email_utente);

ALTER TABLE SPESA ADD CONSTRAINT REF_SPESA_PERIO_FK
     FOREIGN KEY (mese, anno)
     REFERENCES PERIODO (mese, anno);

ALTER TABLE SPESA ADD CONSTRAINT REF_SPESA_CATEG_FK
     FOREIGN KEY (nome)
     REFERENCES CATEGORIA (nome);

ALTER TABLE HA ADD CONSTRAINT REF_HA_TAG
     FOREIGN KEY (nome)
     REFERENCES TAG (nome);

ALTER TABLE HA ADD CONSTRAINT REF_HA_SPESA_FK
     FOREIGN KEY (email, importo, data)
     REFERENCES SPESA (email, importo, data);

-- Index Section

CREATE INDEX REF_BUDGE_CATEG_IND
     ON BUDGETCATEGORIA (nome);

CREATE INDEX EQU_BUDGE_BUDGE_IND
     ON BUDGETCATEGORIA (ID_BUD);

CREATE UNIQUE INDEX ID_BUDGETMENSILE_IND
     ON BUDGETMENSILE (ID_BUD);

CREATE INDEX REF_BUDGE_PERIO_IND
     ON BUDGETMENSILE (mese, anno);

CREATE INDEX REF_BUDGE_Utent_IND
     ON BUDGETMENSILE (email_utente);

CREATE UNIQUE INDEX ID_CATEGORIA_IND
     ON CATEGORIA (nome);

CREATE INDEX REF_CATEG_Utent_IND
     ON CATEGORIA (email_utente);

CREATE UNIQUE INDEX ID_ENTRATA_IND
     ON ENTRATA (email, importo, data);

CREATE INDEX REF_ENTRA_Utent_IND
     ON ENTRATA (email_utente);

CREATE INDEX REF_ENTRA_PERIO_IND
     ON ENTRATA (mese, anno);

CREATE INDEX REF_ENTRA_Fonte_IND
     ON ENTRATA (nome);

CREATE UNIQUE INDEX ID_FonteEntrata_IND
     ON FonteEntrata (nome);

CREATE INDEX REF_Fonte_Utent_IND
     ON FonteEntrata (email_utente);

CREATE UNIQUE INDEX ID_PERIODO_IND
     ON PERIODO (mese, anno);

CREATE UNIQUE INDEX ID_HA_IND
     ON HA (nome, email, importo, data);

CREATE INDEX REF_HA_SPESA_IND
     ON HA (email, importo, data);

CREATE UNIQUE INDEX ID_SPESA_IND
     ON SPESA (email, importo, data);

CREATE INDEX REF_SPESA_Utent_IND
     ON SPESA (email_utente);

CREATE INDEX REF_SPESA_PERIO_IND
     ON SPESA (mese, anno);

CREATE INDEX REF_SPESA_CATEG_IND
     ON SPESA (nome);

CREATE UNIQUE INDEX ID_TAG_IND
     ON TAG (nome);

CREATE INDEX REF_TAG_Utent_IND
     ON TAG (email_utente);

CREATE UNIQUE INDEX ID_Utente_IND
     ON Utente (email_utente);