package it.unibo.sage.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RequestedFeaturesSourceInspectionTest {

    private RequestedFeaturesSourceInspectionTest() {
    }

    public static void runAll() throws IOException {
        testRicorrenzeGeneranoTransazioniCollegate();
        testDaoPersistonoNuoviCampi();
        testServizioBloccaDuplicati();
        testConfigurazioneInizialeBloccaFontiDuplicate();
        testMovimentiRollbackAncheSuRuntimeException();
        testFontiPersistonoIcone();
        testFonteDaoNascondeDuplicatiDiSistema();
        testDashboardCaricamentoDatiSeparato();
        testDashboardCalcoliOverviewEstratti();
        testDashboardSupportoUiEstratto();
    }

    private static void testRicorrenzeGeneranoTransazioniCollegate() throws IOException {
        final String source = read("src/it/unibo/sage/service/SpeseRicorrentiService.java");
        TestAssertions.assertTrue(source.contains("ricorrenza.getNome()"),
                "La transazione generata deve usare il nome della ricorrenza");
        TestAssertions.assertTrue(source.contains("ricorrenza.getId()"),
                "La transazione generata deve conservare ID_Ricorrenza");
        TestAssertions.assertTrue(source.contains("transazioneDAO.inserisci(spesa)"),
                "Una scadenza ricorrente deve creare una vera TRANSIZIONE");
        TestAssertions.assertTrue(source.contains("budgetDAO.aggiungiSpesaAiBudget"),
                "La spesa generata deve aggiornare anche i budget");
        TestAssertions.assertTrue(source.contains("aggiungiRicorrenzaERegistraPrimaSpesa")
                        && source.contains("generaOccorrenzeMancanti")
                        && source.contains("idRicorrenza"),
                "La creazione di una ricorrenza deve registrare tutte le TRANSIZIONI maturate");
        TestAssertions.assertTrue(source.contains("findDateByRicorrenza")
                        && source.contains("calcolaScadenzeDaDataInizio"),
                "La generazione deve riparare lo storico senza creare duplicati");
    }

    private static void testDaoPersistonoNuoviCampi() throws IOException {
        final String recurringDao = read(
                "src/it/unibo/sage/dao/JdbcSpesaRicorrenteDAO.java");
        final String transactionDao = read(
                "src/it/unibo/sage/dao/JdbcTransazioneDAO.java");
        TestAssertions.assertTrue(recurringDao.contains("Nome, Importo_Previsto"),
                "Il DAO delle ricorrenze deve leggere e scrivere il nome");
        TestAssertions.assertTrue(transactionDao.contains("ID_Ricorrenza")
                        && transactionDao.contains("getIdRicorrenza"),
                "Il DAO delle transazioni deve leggere e scrivere ID_Ricorrenza");
    }

    private static void testServizioBloccaDuplicati() throws IOException {
        final String source = read("src/it/unibo/sage/service/MovimentiService.java");
        TestAssertions.assertTrue(source.contains("LOWER(TRIM(Nome))")
                        && source.contains("is_system = TRUE OR Email_Proprietario = ?"),
                "Il servizio deve confrontare nomi normalizzati e classificazioni di sistema");
        TestAssertions.assertTrue(source.contains("verificaNomeDisponibile"),
                "Creazione e modifica devono verificare i duplicati");
    }

    private static void testConfigurazioneInizialeBloccaFontiDuplicate() throws IOException {
        final String source = read("src/it/unibo/sage/service/ConfigurazioneInizialeService.java");
        final String existsFonteSql = section(source,
                "private static final String EXISTS_FONTE_SQL",
                "private static final String INSERT_FONTE_SQL");
        TestAssertions.assertTrue(existsFonteSql.contains("LOWER(TRIM(Nome)) = LOWER(TRIM(?))"),
                "La configurazione iniziale deve normalizzare i nomi delle fonti");
        TestAssertions.assertTrue(existsFonteSql.contains("is_system = TRUE OR Email_Proprietario = ?"),
                "La configurazione iniziale deve bloccare duplicati rispetto a fonti di sistema");
    }

    private static void testMovimentiRollbackAncheSuRuntimeException() throws IOException {
        final String source = read("src/it/unibo/sage/service/MovimentiService.java");
        final int rollbackCatches = source.split(
                "catch \\(final SQLException \\| RuntimeException ex\\)", -1).length - 1;
        TestAssertions.assertTrue(rollbackCatches >= 4,
                "I metodi transazionali devono fare rollback anche su RuntimeException");
    }

    private static void testFontiPersistonoIcone() throws IOException {
        final String dao = read("src/it/unibo/sage/dao/JdbcFonteDAO.java");
        final String service = read("src/it/unibo/sage/service/MovimentiService.java");
        TestAssertions.assertTrue((dao.contains("Nome, Icona, is_system")
                        || dao.contains("F.Nome, F.Icona, F.is_system"))
                        && dao.contains("getString(\"Icona\")"),
                "Il DAO delle fonti deve leggere l'icona dal database");
        TestAssertions.assertTrue(service.contains("INSERT INTO FONTE (Nome, Icona")
                        && service.contains("UPDATE FONTE SET Nome = ?, Icona = ?")
                        && service.contains("SOURCE_ICONS"),
                "Il servizio deve creare e modificare fonti con icona");
    }

    private static void testFonteDaoNascondeDuplicatiDiSistema() throws IOException {
        final String dao = read("src/it/unibo/sage/dao/JdbcFonteDAO.java");
        TestAssertions.assertTrue(dao.contains("LOWER(TRIM(FS.Nome)) = LOWER(TRIM(F.Nome))")
                        && dao.contains("F.is_system = FALSE AND EXISTS"),
                "Il DAO delle fonti deve nascondere duplicati personali di fonti di sistema");
    }

    private static void testDashboardCaricamentoDatiSeparato() throws IOException {
        final String dashboard = read("src/it/unibo/sage/view/dashboard/DashboardPanel.java");
        final String dataService = read("src/it/unibo/sage/service/DashboardDataService.java");
        final String data = read("src/it/unibo/sage/service/DashboardData.java");
        TestAssertions.assertTrue(dashboard.contains("DashboardDataService")
                        && dashboard.contains("reloadDashboardData"),
                "DashboardPanel deve delegare il caricamento dati a un servizio dedicato");
        TestAssertions.assertTrue(dataService.contains("loadForUser")
                        && dataService.contains("caricaTransazioni")
                        && dataService.contains("caricaBudget")
                        && dataService.contains("caricaRicorrenze"),
                "DashboardDataService deve centralizzare il caricamento delle card");
        TestAssertions.assertTrue(data.contains("Collections.unmodifiableList")
                        && data.contains("Collections.unmodifiableMap"),
                "DashboardData deve esporre uno snapshot non modificabile");
    }

    private static void testDashboardCalcoliOverviewEstratti() throws IOException {
        final String dashboard = read("src/it/unibo/sage/view/dashboard/DashboardPanel.java");
        final String calculator = read("src/it/unibo/sage/service/DashboardOverviewCalculator.java");
        TestAssertions.assertTrue(dashboard.contains("DashboardOverviewCalculator"),
                "DashboardPanel deve delegare i calcoli overview a un helper dedicato");
        TestAssertions.assertTrue(calculator.contains("filterTransactions")
                        && calculator.contains("buildExpenseDistribution")
                        && calculator.contains("buildMonthlyTotals"),
                "DashboardOverviewCalculator deve contenere i calcoli non UI dell'overview");
    }

    private static void testDashboardSupportoUiEstratto() throws IOException {
        final String dashboard = read("src/it/unibo/sage/view/dashboard/DashboardPanel.java");
        final String icons = read("src/it/unibo/sage/view/dashboard/ClassificationIconSupport.java");
        final String charts = read("src/it/unibo/sage/view/dashboard/DashboardCharts.java");
        TestAssertions.assertTrue(dashboard.contains("ClassificationIconSupport")
                        && icons.contains("IconSelectionPanel")
                        && icons.contains("paintFallbackClassificationIcon"),
                "La dashboard deve delegare icone e selettore a un supporto dedicato");
        TestAssertions.assertTrue(dashboard.contains("DashboardCharts")
                        && charts.contains("DailyExpenseChartPanel")
                        && charts.contains("ExpenseDistributionChartPanel"),
                "La dashboard deve delegare il disegno dei grafici a una classe dedicata");
    }

    private static String read(final String fileName) throws IOException {
        final Path path = Path.of(fileName);
        TestAssertions.assertTrue(Files.isRegularFile(path), "File sorgente mancante: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static String section(final String source, final String start, final String end) {
        final int startIndex = source.indexOf(start);
        final int endIndex = source.indexOf(end, startIndex);
        TestAssertions.assertTrue(startIndex >= 0 && endIndex > startIndex,
                "Sezione sorgente non trovata: " + start);
        return source.substring(startIndex, endIndex);
    }
}
