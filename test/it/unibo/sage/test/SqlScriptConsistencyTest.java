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
    }

    private static void testSchemaContieneTabelleFondamentali() throws IOException {
        final String schema = readSql("schema_completo.sql").toUpperCase();
        final List<String> tables = List.of("UTENTE", "PERIODO", "CATEGORIA", "TAG", "FONTE",
                "TRANSIZIONE", "SPESA_TAG", "BUDGET", "DOCUMENTO", "SPESA_RICORRENTE");
        for (final String table : tables) {
            TestAssertions.assertTrue(schema.contains("CREATE TABLE " + table)
                    || schema.contains("CREATE TABLE IF NOT EXISTS " + table),
                    "Lo schema deve creare la tabella " + table);
        }
    }


    private static void testSchemaContieneNuoveRelazioniEIcone() throws IOException {
        final String schema = readSql("schema_completo.sql").toUpperCase();
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

    private static void testTriggerVisteContieneVistePrincipali() throws IOException {
        final String triggerViews = readSql("trigger_viste.sql").toUpperCase();
        TestAssertions.assertTrue(triggerViews.contains("V_BUDGET_STATO"),
                "trigger_viste.sql deve contenere la vista v_budget_stato");
        TestAssertions.assertTrue(triggerViews.contains("V_STATISTICHE_AGGREGATE_ADMIN"),
                "trigger_viste.sql deve contenere la vista v_statistiche_aggregate_admin");
        TestAssertions.assertTrue(triggerViews.contains("TRG_TRANSIZIONE_INSERT_CHECK")
                        || triggerViews.contains("BEFORE INSERT"),
                "trigger_viste.sql deve contenere almeno un trigger di inserimento transazione");
    }


    private static void testTriggerBloccanoDuplicatiERicorrenzeIncoerenti() throws IOException {
        final String triggerViews = readSql("trigger_viste.sql").toUpperCase();
        TestAssertions.assertTrue(triggerViews.contains("TRG_CATEGORIA_INSERT_NO_DUPLICATI")
                        && triggerViews.contains("TRG_TAG_INSERT_NO_DUPLICATI"),
                "Il database deve bloccare categorie e tag duplicati");
        TestAssertions.assertTrue(triggerViews.contains("NEW.ID_RICORRENZA IS NOT NULL")
                        && triggerViews.contains("RICORRENZA NON COERENTE"),
                "Il database deve validare le transazioni generate da ricorrenze");
        TestAssertions.assertTrue(triggerViews.contains("NOME_RICORRENZA")
                        && triggerViews.contains("SR.NOME"),
                "Le viste devono esporre il nome della ricorrenza");
    }

    private static void testPopolamentoContieneDatiDemo() throws IOException {
        final String popolamento = readSql("popolamento.sql").toLowerCase();
        TestAssertions.assertTrue(popolamento.contains("studente1@mail.com"),
                "popolamento.sql deve contenere l'utente demo studente1@mail.com");
        TestAssertions.assertTrue(popolamento.contains("admin@sage.com"),
                "popolamento.sql deve contenere l'utente demo admin@sage.com");
        TestAssertions.assertTrue(popolamento.contains("alimentari") && popolamento.contains("stipendio"),
                "popolamento.sql deve contenere categorie e fonti di base");
    }


    private static void testPopolamentoNonContieneEssenziale() throws IOException {
        final String popolamento = readSql("popolamento.sql").toLowerCase();
        TestAssertions.assertFalse(popolamento.contains("essenziale"),
                "Il tag Essenziale deve essere rimosso dal popolamento");
        TestAssertions.assertTrue(popolamento.contains("abbonamento autobus")
                        && popolamento.contains("abbonamento palestra"),
                "Le spese ricorrenti demo devono avere un nome leggibile");
        TestAssertions.assertTrue(popolamento.contains("set id_ricorrenza"),
                "Le transazioni demo devono essere collegate alle ricorrenze");
    }

    private static void testAggiornamentoRicorrenzePresente() throws IOException {
        final Path path = Path.of("doc", "sql", "aggiornamento_spese_ricorrenti.sql");
        if (!Files.exists(path)) {
            return;
        }
        final String update = Files.readString(path, StandardCharsets.UTF_8).toUpperCase();
        TestAssertions.assertTrue(update.contains("SPESA_RICORRENTE"),
                "aggiornamento_spese_ricorrenti.sql deve riferirsi a SPESA_RICORRENTE");
        TestAssertions.assertTrue(update.contains("V_SPESE_RICORRENTI_SCADUTE")
                        || update.contains("IDX_RICORRENTE"),
                "aggiornamento_spese_ricorrenti.sql deve creare vista o indice per ricorrenze");
    }


    private static void testMigrazioneFunzioniRichiestePresente() throws IOException {
        final String migration = readSql("aggiornamento_funzioni_richieste.sql").toUpperCase();
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
    }

    private static String readSql(final String fileName) throws IOException {
        final Path path = Path.of("doc", "sql", fileName);
        TestAssertions.assertTrue(Files.isRegularFile(path), "File SQL mancante: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
