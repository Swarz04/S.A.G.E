package it.unibo.sage.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SqlScriptConsistencyTest {

    private SqlScriptConsistencyTest() {
    }

    public static void runAll() throws IOException {
        testSchemaContieneTabelleFondamentali();
        testSchemaContieneNuoveRelazioniEIcone();
        testTriggerVisteContieneVistePrincipali();
        testTriggerBloccanoDuplicatiERicorrenzeIncoerenti();
        testPopolamentoContieneDatiDemo();
        testPopolamentoNonContieneEssenziale();
        testAggiornamentoRicorrenzePresente();
        testMigrazioneFunzioniRichiestePresente();
        testDocumentoUnicoPerTransazione();
        testDocumentazioneIndicaScriptUfficiali();
    }

    private static void testSchemaContieneTabelleFondamentali() throws IOException {
        final String schema = readSql("01_schema_completo.sql").toUpperCase();
        final List<String> tables = List.of("UTENTE", "PERIODO", "CATEGORIA", "TAG", "FONTE",
                "TRANSIZIONE", "SPESA_TAG", "BUDGET", "DOCUMENTO", "SPESA_RICORRENTE");
        for (final String table : tables) {
            TestAssertions.assertTrue(schema.contains("CREATE TABLE " + table)
                    || schema.contains("CREATE TABLE IF NOT EXISTS " + table),
                    "Lo schema deve creare la tabella " + table);
        }
    }


    private static void testSchemaContieneNuoveRelazioniEIcone() throws IOException {
        final String schema = readSql("01_schema_completo.sql").toUpperCase();
        TestAssertions.assertTrue(schema.contains("ICONA VARCHAR(255)"),
                "Categorie, tag e fonti devono salvare anche riferimenti a icone personalizzate");
        final int iconColumns = schema.split("ICONA VARCHAR\\(255\\)", -1).length - 1;
        TestAssertions.assertTrue(iconColumns >= 3,
                "CATEGORIA, TAG e FONTE devono avere la colonna Icona");
        TestAssertions.assertTrue(schema.contains("ID_RICORRENZA INT"),
                "TRANSIZIONE deve conservare il riferimento alla ricorrenza");
        TestAssertions.assertTrue(schema.contains("FK_TRANSIZIONE_RICORRENZA"),
                "Lo schema deve definire la FK tra transazione e ricorrenza");
        TestAssertions.assertTrue(schema.contains("NOME VARCHAR(100) NOT NULL"),
                "SPESA_RICORRENTE deve avere un nome leggibile");
        TestAssertions.assertTrue(schema.contains("UQ_TRANSIZIONE_RICORRENZA_DATA"),
                "La stessa ricorrenza non deve generare due volte la stessa data");
    }

    private static void testDocumentoUnicoPerTransazione() throws IOException {
<<<<<<< HEAD
        final String schema = readSql("schema_completo.sql").toUpperCase();
        final String migration = readSql("aggiornamento_funzioni_richieste.sql").toUpperCase();
=======
        final String schema = readSql("01_schema_completo.sql").toUpperCase();
        final String migration = readSql("03_migrazione_database_esistente.sql").toUpperCase();
>>>>>>> 3351d66 (aggiornamento interfaccia)
        TestAssertions.assertTrue(schema.contains("UQ_DOCUMENTO_TRANSIZIONE")
                        && schema.contains("ON DOCUMENTO (ID_TRANSIZIONE)"),
                "Lo schema deve impedire piu' documenti sulla stessa transazione");
        TestAssertions.assertTrue(migration.contains("UQ_DOCUMENTO_TRANSIZIONE")
                        && migration.contains("CREATE UNIQUE INDEX UQ_DOCUMENTO_TRANSIZIONE"),
                "La migrazione deve aggiungere il vincolo unico sui documenti");
    }

    private static void testTriggerVisteContieneVistePrincipali() throws IOException {
        final String triggerViews = readSql("01_schema_completo.sql").toUpperCase();
        TestAssertions.assertTrue(triggerViews.contains("V_BUDGET_STATO"),
                "01_schema_completo.sql deve contenere la vista v_budget_stato");
        TestAssertions.assertTrue(triggerViews.contains("V_STATISTICHE_AGGREGATE_ADMIN"),
                "01_schema_completo.sql deve contenere la vista v_statistiche_aggregate_admin");
        TestAssertions.assertTrue(triggerViews.contains("TRG_TRANSIZIONE_INSERT_CHECK")
                        || triggerViews.contains("BEFORE INSERT"),
                "01_schema_completo.sql deve contenere almeno un trigger di inserimento transazione");
    }


    private static void testTriggerBloccanoDuplicatiERicorrenzeIncoerenti() throws IOException {
        final String triggerViews = readSql("01_schema_completo.sql").toUpperCase();
        TestAssertions.assertTrue(triggerViews.contains("TRG_CATEGORIA_INSERT_NO_DUPLICATI")
                        && triggerViews.contains("TRG_TAG_INSERT_NO_DUPLICATI")
                        && triggerViews.contains("TRG_FONTE_INSERT_NO_DUPLICATI")
                        && triggerViews.contains("TRG_FONTE_UPDATE_NO_DUPLICATI"),
                "Il database deve bloccare categorie, tag e fonti duplicati");
        TestAssertions.assertTrue(triggerViews.contains("NEW.ID_RICORRENZA IS NOT NULL")
                        && triggerViews.contains("RICORRENZA NON COERENTE"),
                "Il database deve validare le transazioni generate da ricorrenze");
        TestAssertions.assertTrue(triggerViews.contains("NOME_RICORRENZA")
                        && triggerViews.contains("SR.NOME"),
                "Le viste devono esporre il nome della ricorrenza");
    }

    private static void testPopolamentoContieneDatiDemo() throws IOException {
        final String popolamento = readSql("02_popolamento_demo.sql").toLowerCase();
        TestAssertions.assertTrue(popolamento.contains("studente1@mail.com"),
                "02_popolamento_demo.sql deve contenere l'utente demo studente1@mail.com");
        TestAssertions.assertTrue(popolamento.contains("admin@sage.com"),
                "02_popolamento_demo.sql deve contenere l'utente demo admin@sage.com");
        TestAssertions.assertTrue(popolamento.contains("alimentari") && popolamento.contains("stipendio"),
                "02_popolamento_demo.sql deve contenere categorie e fonti di base");
    }


    private static void testPopolamentoNonContieneEssenziale() throws IOException {
        final String popolamento = readSql("02_popolamento_demo.sql").toLowerCase();
        TestAssertions.assertFalse(popolamento.contains("essenziale"),
                "Il tag Essenziale deve essere rimosso dal popolamento");
        TestAssertions.assertTrue(popolamento.contains("abbonamento autobus")
                        && popolamento.contains("abbonamento palestra"),
                "Le spese ricorrenti demo devono avere un nome leggibile");
        TestAssertions.assertTrue(popolamento.contains("set id_ricorrenza"),
                "Le transazioni demo devono essere collegate alle ricorrenze");
    }

    private static void testAggiornamentoRicorrenzePresente() throws IOException {
        final Path path = Path.of("doc", "sql", "03_migrazione_database_esistente.sql");
        if (!Files.exists(path)) {
            return;
        }
        final String update = Files.readString(path, StandardCharsets.UTF_8).toUpperCase();
        TestAssertions.assertTrue(update.contains("SPESA_RICORRENTE"),
                "03_migrazione_database_esistente.sql deve riferirsi a SPESA_RICORRENTE");
        TestAssertions.assertTrue(update.contains("V_SPESE_RICORRENTI_SCADUTE")
                        || update.contains("IDX_RICORRENTE"),
                "03_migrazione_database_esistente.sql deve creare vista o indice per ricorrenze");
    }


    private static void testMigrazioneFunzioniRichiestePresente() throws IOException {
        final String migration = readSql("03_migrazione_database_esistente.sql").toUpperCase();
        TestAssertions.assertTrue(migration.contains("CATEGORIA ADD COLUMN ICONA")
                        && migration.contains("TAG ADD COLUMN ICONA")
                        && migration.contains("FONTE ADD COLUMN ICONA"),
                "La migrazione deve aggiungere le icone a categorie, tag e fonti");
        TestAssertions.assertTrue(migration.contains("TRANSIZIONE ADD COLUMN ID_RICORRENZA")
                        && migration.contains("FK_TRANSIZIONE_RICORRENZA"),
                "La migrazione deve collegare transazioni e ricorrenze");
        TestAssertions.assertTrue(migration.contains("DELETE FROM TAG")
                        && migration.contains("ESSENZIALE"),
                "La migrazione deve eliminare il vecchio tag Essenziale");
        TestAssertions.assertTrue(migration.contains("TRG_FONTE_INSERT_NO_DUPLICATI")
                        && migration.contains("TRG_FONTE_UPDATE_NO_DUPLICATI"),
                "La migrazione deve aggiungere i trigger anti-duplicato per le fonti");
    }

    private static void testDocumentazioneIndicaScriptUfficiali() throws IOException {
        final String latex = Files.readString(Path.of("doc", "latex", "main.tex"),
                StandardCharsets.UTF_8);
<<<<<<< HEAD
        final String legacy = readSql("creazione_schema_indici.sql").toUpperCase();
        TestAssertions.assertTrue(latex.contains("doc/sql/schema\\_completo.sql")
                        && latex.contains("doc/sql/query\\_operazioni.sql")
                        && latex.contains("trigger\\_viste.sql"),
                "Il LaTeX deve indicare gli script SQL ufficiali corretti");
        TestAssertions.assertTrue(legacy.contains("SCRIPT STORICO")
                        && legacy.contains("NON UFFICIALE"),
                "Lo script vecchio deve essere marcato come storico e non ufficiale");
=======
        final String readme = Files.readString(Path.of("doc", "sql", "README.md"),
                StandardCharsets.UTF_8);
        TestAssertions.assertTrue(latex.contains("01\\_schema\\_completo.sql")
                        && latex.contains("04\\_query\\_operazioni.sql")
                        && latex.contains("02\\_popolamento\\_demo.sql"),
                "Il LaTeX deve indicare i quattro script SQL consolidati");
        TestAssertions.assertTrue(readme.contains("03_migrazione_database_esistente.sql")
                        && readme.contains("quattro script"),
                "Il README SQL deve spiegare installazione e migrazione consolidate");
        try (java.util.stream.Stream<Path> files = Files.list(Path.of("doc", "sql"))) {
            final long sqlCount = files
                    .filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .count();
            TestAssertions.assertTrue(sqlCount <= 5,
                    "La cartella doc/sql deve contenere al massimo cinque file SQL");
        }
>>>>>>> 3351d66 (aggiornamento interfaccia)
    }

    private static String readSql(final String fileName) throws IOException {
        final Path path = Path.of("doc", "sql", fileName);
        TestAssertions.assertTrue(Files.isRegularFile(path), "File SQL mancante: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
