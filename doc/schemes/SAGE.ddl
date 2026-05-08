-- *********************************************
-- * Standard SQL generation                   
-- *--------------------------------------------
-- * DB-MAIN version: 11.0.2              
-- * Generator date: Sep 14 2021              
-- * Generation date: Sun May  3 10:36:58 2026 
-- * LUN file: C:\Users\schar\Music\S.A.G.E-local\SAGE_Relazionale_vista_ibrida.lun 
-- * Schema: Finale Schema relazionale S.A.G.E Vista ibrida Raffinata/SQL 
-- ********************************************* 


-- Database Section
-- ________________ 

create database Finale Schema relazionale S.A.G.E Vista ibrida Raffinata;


-- DBSpace Section
-- _______________


-- Tables Section
-- _____________ 

create table associazione (
     ID_TAG_1 numeric(10) not null,
     Usc_ID_Transazione numeric(11) not null,
     ID_Transazione char(1) not null,
     constraint ID_associazione_ID primary key (ID_TAG_1, Usc_ID_Transazione, ID_Transazione));

create table BUDGET (
     ID_Budget -- Sequence attribute not implemented -- not null,
     Importo_Limite float(10) not null,
     Totale_Speso_Attuale float(10) not null,
     Alert_Soglia char not null,
     ID_Periodo numeric(11) not null,
     Email varchar(100) not null,
     constraint ID_BUDGET_ID primary key (ID_Budget));

create table CATEGORIA (
     ID_Categoria -- Sequence attribute not implemented -- not null,
     Nome varchar(50) not null,
     is_system char not null,
     ID_Ricorrenza numeric(11) not null,
     Email varchar(100) not null,
     ID_Budget numeric(11),
     constraint ID_CATEGORIA_ID primary key (ID_Categoria));

create table DOCUMENTO (
     ID_Documento -- Sequence attribute not implemented -- not null,
     Usc_ID_Transazione numeric(11) not null,
     ID_Transazione char(1) not null,
     Path_File varchar(255) not null,
     Tipo_File varchar(10) not null,
     Data_Acquisizione_Documento date not null,
     constraint ID_DOCUMENTO_ID primary key (ID_Documento),
     constraint FKdocumenta_ID unique (Usc_ID_Transazione, ID_Transazione));

create table ENTRATA (
     Gua_ID_Transazione numeric(11) not null,
     ID_Transazione char(1) not null,
     constraint ID_ENTRATA_ID primary key (Gua_ID_Transazione, ID_Transazione),
     constraint FKGuadagno_ID unique (Gua_ID_Transazione));

create table FONTE (
     Gua_ID_Transazione numeric(11) not null,
     ID_Transazione char(1) not null,
     ID_Fonte char(1) not null,
     Nome char(1) not null,
     is_system char(1) not null,
     Email varchar(100) not null,
     constraint SID_FONTE_ID unique (ID_Fonte),
     constraint FKproviene_ID primary key (Gua_ID_Transazione, ID_Transazione));

create table PERIODO (
     ID_Periodo -- Sequence attribute not implemented -- not null,
     Mese numeric(2) not null,
     Anno numeric(4) not null,
     constraint ID_PERIODO_ID primary key (ID_Periodo));

create table SPESA (
     Usc_ID_Transazione numeric(11) not null,
     ID_Transazione char(1) not null,
     ID_Categoria numeric(11) not null,
     constraint ID_SPESA_ID primary key (Usc_ID_Transazione, ID_Transazione),
     constraint FKUscita_ID unique (Usc_ID_Transazione));

create table SPESA_RICORRENTE (
     ID_Ricorrenza -- Sequence attribute not implemented -- not null,
     Importo_Previsto float(10) not null,
     Frequenza_Giorni numeric(3) not null,
     Data_Inizio date not null,
     Scadenza date not null,
     Email varchar(100) not null,
     constraint ID_SPESA_RICORRENTE_ID primary key (ID_Ricorrenza));

create table TAG (
     ID_TAG_1 -- Sequence attribute not implemented -- not null,
     ID_Tag char(1) not null,
     Nome char(1) not null,
     is_system char(1) not null,
     Email varchar(100) not null,
     constraint ID_ID primary key (ID_TAG_1),
     constraint SID_TAG_ID unique (ID_Tag));

create table TRANSIZIONE (
     ID_Transazione -- Sequence attribute not implemented -- not null,
     Importo char not null,
     Data date not null,
     Descrizione varchar(255) not null,
     Email varchar(100) not null,
     ID_Periodo numeric(11) not null,
     constraint ID_TRANSIZIONE_ID primary key (ID_Transazione));

create table UTENTE (
     Email varchar(100) not null,
     Password char(128) not null,
     Nome varchar(50) not null,
     Cognome varchar(50) not null,
     Ruolo varchar(50) not null,
     constraint ID_UTENTE_ID primary key (Email));


-- Constraints Section
-- ___________________ 

alter table associazione add constraint FKass_SPE_FK
     foreign key (Usc_ID_Transazione, ID_Transazione)
     references SPESA;

alter table associazione add constraint FKass_TAG
     foreign key (ID_TAG_1)
     references TAG;

alter table BUDGET add constraint FKrelativo_FK
     foreign key (ID_Periodo)
     references PERIODO;

alter table BUDGET add constraint FKdefinizione_FK
     foreign key (Email)
     references UTENTE;

alter table CATEGORIA add constraint FKtipo_ricorrenza_FK
     foreign key (ID_Ricorrenza)
     references SPESA_RICORRENTE;

alter table CATEGORIA add constraint FKpersonalizza_cat_FK
     foreign key (Email)
     references UTENTE;

alter table CATEGORIA add constraint FKlimitazione_FK
     foreign key (ID_Budget)
     references BUDGET;

alter table DOCUMENTO add constraint FKdocumenta_FK
     foreign key (Usc_ID_Transazione, ID_Transazione)
     references SPESA;

alter table ENTRATA add constraint FKGuadagno_FK
     foreign key (Gua_ID_Transazione)
     references TRANSIZIONE;

alter table FONTE add constraint FKpersonalizza_fonte_FK
     foreign key (Email)
     references UTENTE;

alter table FONTE add constraint FKproviene_FK
     foreign key (Gua_ID_Transazione, ID_Transazione)
     references ENTRATA;

alter table SPESA add constraint FKUscita_FK
     foreign key (Usc_ID_Transazione)
     references TRANSIZIONE;

alter table SPESA add constraint FKclassifica_FK
     foreign key (ID_Categoria)
     references CATEGORIA;

alter table SPESA_RICORRENTE add constraint FKpianifica_FK
     foreign key (Email)
     references UTENTE;

alter table TAG add constraint FKpersonalizza_tag_FK
     foreign key (Email)
     references UTENTE;

alter table TRANSIZIONE add constraint FKeffettua_FK
     foreign key (Email)
     references UTENTE;

alter table TRANSIZIONE add constraint FKavviene_FK
     foreign key (ID_Periodo)
     references PERIODO;


-- Index Section
-- _____________ 

create unique index ID_associazione_IND
     on associazione (ID_TAG_1, Usc_ID_Transazione, ID_Transazione);

create index FKass_SPE_IND
     on associazione (Usc_ID_Transazione, ID_Transazione);

create unique index ID_BUDGET_IND
     on BUDGET (ID_Budget);

create index FKrelativo_IND
     on BUDGET (ID_Periodo);

create index FKdefinizione_IND
     on BUDGET (Email);

create unique index ID_CATEGORIA_IND
     on CATEGORIA (ID_Categoria);

create index FKtipo_ricorrenza_IND
     on CATEGORIA (ID_Ricorrenza);

create index FKpersonalizza_cat_IND
     on CATEGORIA (Email);

create index FKlimitazione_IND
     on CATEGORIA (ID_Budget);

create unique index ID_DOCUMENTO_IND
     on DOCUMENTO (ID_Documento);

create unique index FKdocumenta_IND
     on DOCUMENTO (Usc_ID_Transazione, ID_Transazione);

create unique index ID_ENTRATA_IND
     on ENTRATA (Gua_ID_Transazione, ID_Transazione);

create unique index SID_FONTE_IND
     on FONTE (ID_Fonte);

create index FKpersonalizza_fonte_IND
     on FONTE (Email);

create unique index FKproviene_IND
     on FONTE (Gua_ID_Transazione, ID_Transazione);

create unique index ID_PERIODO_IND
     on PERIODO (ID_Periodo);

create unique index ID_SPESA_IND
     on SPESA (Usc_ID_Transazione, ID_Transazione);

create index FKclassifica_IND
     on SPESA (ID_Categoria);

create unique index ID_SPESA_RICORRENTE_IND
     on SPESA_RICORRENTE (ID_Ricorrenza);

create index FKpianifica_IND
     on SPESA_RICORRENTE (Email);

create unique index ID_IND
     on TAG (ID_TAG_1);

create unique index SID_TAG_IND
     on TAG (ID_Tag);

create index FKpersonalizza_tag_IND
     on TAG (Email);

create unique index ID_TRANSIZIONE_IND
     on TRANSIZIONE (ID_Transazione);

create index FKeffettua_IND
     on TRANSIZIONE (Email);

create index FKavviene_IND
     on TRANSIZIONE (ID_Periodo);

create unique index ID_UTENTE_IND
     on UTENTE (Email);

