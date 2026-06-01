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
        testTriggerVisteContieneVistePrincipali();
        testPopolamentoContieneDatiDemo();
        testAggiornamentoRicorrenzePresente();
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

    private static void testPopolamentoContieneDatiDemo() throws IOException {
        final String popolamento = readSql("popolamento.sql").toLowerCase();
        TestAssertions.assertTrue(popolamento.contains("studente1@mail.com"),
                "popolamento.sql deve contenere l'utente demo studente1@mail.com");
        TestAssertions.assertTrue(popolamento.contains("admin@sage.com"),
                "popolamento.sql deve contenere l'utente demo admin@sage.com");
        TestAssertions.assertTrue(popolamento.contains("alimentari") && popolamento.contains("stipendio"),
                "popolamento.sql deve contenere categorie e fonti di base");
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

    private static String readSql(final String fileName) throws IOException {
        final Path path = Path.of("doc", "sql", fileName);
        TestAssertions.assertTrue(Files.isRegularFile(path), "File SQL mancante: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
