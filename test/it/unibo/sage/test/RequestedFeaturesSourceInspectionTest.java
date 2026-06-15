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
        testFontiPersistonoIcone();
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

    private static void testFontiPersistonoIcone() throws IOException {
        final String dao = read("src/it/unibo/sage/dao/JdbcFonteDAO.java");
        final String service = read("src/it/unibo/sage/service/MovimentiService.java");
        TestAssertions.assertTrue(dao.contains("Nome, Icona, is_system")
                        && dao.contains("getString(\"Icona\")"),
                "Il DAO delle fonti deve leggere l'icona dal database");
        TestAssertions.assertTrue(service.contains("INSERT INTO FONTE (Nome, Icona")
                        && service.contains("UPDATE FONTE SET Nome = ?, Icona = ?")
                        && service.contains("SOURCE_ICONS"),
                "Il servizio deve creare e modificare fonti con icona");
    }

    private static String read(final String fileName) throws IOException {
        final Path path = Path.of(fileName);
        TestAssertions.assertTrue(Files.isRegularFile(path), "File sorgente mancante: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
