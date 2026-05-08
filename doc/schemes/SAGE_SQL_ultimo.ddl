CREATE DATABASE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;
USE Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata;

-- ======================
-- TABLES
-- ======================

CREATE TABLE UTENTE (
    Email VARCHAR(100) NOT NULL,
    Password CHAR(128) NOT NULL,
    Nome VARCHAR(50) NOT NULL,
    Cognome VARCHAR(50) NOT NULL,
    Ruolo VARCHAR(50) NOT NULL,
    PRIMARY KEY (Email)
);

CREATE TABLE PERIODO (
    ID_Periodo INT AUTO_INCREMENT,
    Mese INT NOT NULL,
    Anno INT NOT NULL,
    PRIMARY KEY (ID_Periodo)
);

CREATE TABLE CATEGORIA (
    ID_Categoria INT AUTO_INCREMENT,
    Nome VARCHAR(50) NOT NULL,
    is_system CHAR(1) NOT NULL,
    Email VARCHAR(100),
    PRIMARY KEY (ID_Categoria),
    FOREIGN KEY (Email) REFERENCES UTENTE(Email)
);

CREATE TABLE FONTE (
    ID_Fonte INT AUTO_INCREMENT,
    Nome VARCHAR(50) NOT NULL,
    is_system CHAR(1) NOT NULL,
    Email VARCHAR(100),
    PRIMARY KEY (ID_Fonte),
    FOREIGN KEY (Email) REFERENCES UTENTE(Email)
);

CREATE TABLE TAG (
    ID_Tag INT AUTO_INCREMENT,
    Nome VARCHAR(50) NOT NULL,
    is_system CHAR(1) NOT NULL,
    Email VARCHAR(100),
    PRIMARY KEY (ID_Tag),
    FOREIGN KEY (Email) REFERENCES UTENTE(Email)
);

CREATE TABLE TRANSIZIONE (
    ID_Transizione INT AUTO_INCREMENT,
    TipoTransazione CHAR(1) NOT NULL,
    Importo DECIMAL(10,2) NOT NULL,
    Data DATE NOT NULL,
    Descrizione VARCHAR(255) NOT NULL,
    Email VARCHAR(100) NOT NULL,
    ID_Categoria INT,
    ID_Periodo INT NOT NULL,
    ID_Fonte INT,
    PRIMARY KEY (ID_Transizione),
    FOREIGN KEY (Email) REFERENCES UTENTE(Email),
    FOREIGN KEY (ID_Categoria) REFERENCES CATEGORIA(ID_Categoria),
    FOREIGN KEY (ID_Periodo) REFERENCES PERIODO(ID_Periodo),
    FOREIGN KEY (ID_Fonte) REFERENCES FONTE(ID_Fonte)
);

CREATE TABLE DOCUMENTO (
    ID_Documento INT AUTO_INCREMENT,
    ID_Transizione INT NOT NULL,
    Path_File VARCHAR(255) NOT NULL,
    Tipo_File VARCHAR(10) NOT NULL,
    Data_Acquisizione_Documento DATE NOT NULL,
    PRIMARY KEY (ID_Documento),
    UNIQUE (ID_Transizione),
    FOREIGN KEY (ID_Transizione) REFERENCES TRANSIZIONE(ID_Transizione)
);

CREATE TABLE BUDGET (
    ID_Budget INT AUTO_INCREMENT,
    Importo_Limite DECIMAL(10,2) NOT NULL,
    Alert_Soglia CHAR(1) NOT NULL,
    ID_Periodo INT NOT NULL,
    ID_Categoria INT,
    Email VARCHAR(100) NOT NULL,
    PRIMARY KEY (ID_Budget),
    FOREIGN KEY (ID_Periodo) REFERENCES PERIODO(ID_Periodo),
    FOREIGN KEY (ID_Categoria) REFERENCES CATEGORIA(ID_Categoria),
    FOREIGN KEY (Email) REFERENCES UTENTE(Email)
);

CREATE TABLE SPESA_RICORRENTE (
    ID_Ricorrenza INT AUTO_INCREMENT,
    Importo_Previsto DECIMAL(10,2) NOT NULL,
    Frequenza_Giorni INT NOT NULL,
    Data_Inizio DATE NOT NULL,
    Scadenza DATE NOT NULL,
    ID_Categoria INT NOT NULL,
    Email VARCHAR(100) NOT NULL,
    PRIMARY KEY (ID_Ricorrenza),
    FOREIGN KEY (ID_Categoria) REFERENCES CATEGORIA(ID_Categoria),
    FOREIGN KEY (Email) REFERENCES UTENTE(Email)
);

CREATE TABLE spesa_tag (
    ID_Tag INT NOT NULL,
    ID_Transizione INT NOT NULL,
    PRIMARY KEY (ID_Transizione, ID_Tag),
    FOREIGN KEY (ID_Transizione) REFERENCES TRANSIZIONE(ID_Transizione),
    FOREIGN KEY (ID_Tag) REFERENCES TAG(ID_Tag)
);

-- ======================
-- INDEXES
-- ======================

CREATE INDEX idx_budget_periodo ON BUDGET(ID_Periodo);
CREATE INDEX idx_budget_categoria ON BUDGET(ID_Categoria);
CREATE INDEX idx_budget_email ON BUDGET(Email);

CREATE INDEX idx_categoria_email ON CATEGORIA(Email);
CREATE INDEX idx_fonte_email ON FONTE(Email);
CREATE INDEX idx_tag_email ON TAG(Email);

CREATE INDEX idx_trans_email ON TRANSIZIONE(Email);
CREATE INDEX idx_trans_categoria ON TRANSIZIONE(ID_Categoria);
CREATE INDEX idx_trans_periodo ON TRANSIZIONE(ID_Periodo);
CREATE INDEX idx_trans_fonte ON TRANSIZIONE(ID_Fonte);

CREATE INDEX idx_spesa_categoria ON SPESA_RICORRENTE(ID_Categoria);
CREATE INDEX idx_spesa_email ON SPESA_RICORRENTE(Email);

CREATE INDEX idx_spesatag_tag ON spesa_tag(ID_Tag);

