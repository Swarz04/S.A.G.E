package it.unibo.sage.test;

import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.Ruolo;
import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.model.Tag;
import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import it.unibo.sage.model.Utente;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class ModelMappingTest {

    private ModelMappingTest() {
    }

    public static void runAll() {
        testTipoTransazioneMapping();
        testRuoloMapping();
        testCategoriaTagFonte();
        testBudget();
        testTransazione();
        testSpesaRicorrente();
        testUtente();
    }

    private static void testTipoTransazioneMapping() {
        TestAssertions.assertEquals(TipoTransazione.SPESA, TipoTransazione.fromDb("S"),
                "S deve essere mappato come SPESA");
        TestAssertions.assertEquals(TipoTransazione.ENTRATA, TipoTransazione.fromDb("E"),
                "E deve essere mappato come ENTRATA");
        TestAssertions.assertEquals("S", TipoTransazione.SPESA.getDbValue(),
                "SPESA deve avere valore DB S");
        TestAssertions.assertEquals("E", TipoTransazione.ENTRATA.getDbValue(),
                "ENTRATA deve avere valore DB E");
        TestAssertions.assertThrows(IllegalArgumentException.class,
                () -> TipoTransazione.fromDb("X"),
                "Un tipo transazione non previsto deve essere rifiutato");
    }

    private static void testRuoloMapping() {
        TestAssertions.assertEquals(Ruolo.UTENTE, Ruolo.fromDb(null),
                "Ruolo null deve ricadere su UTENTE");
        TestAssertions.assertEquals(Ruolo.ADMIN, Ruolo.fromDb("admin"),
                "admin minuscolo deve essere convertito in ADMIN");
    }

    private static void testCategoriaTagFonte() {
        Categoria categoria = new Categoria(1L, "Casa", true, null, "house.png");
        TestAssertions.assertEquals(1L, categoria.getId(), "ID categoria non coerente");
        TestAssertions.assertEquals("Casa", categoria.getNome(), "Nome categoria non coerente");
        TestAssertions.assertTrue(categoria.isSystem(), "Categoria di sistema non riconosciuta");
        TestAssertions.assertEquals("house.png", categoria.getIcona(),
                "Icona categoria non coerente");

        Tag tag = new Tag(2L, "Studio", false, "utente@test.it", "study.png");
        TestAssertions.assertEquals(2L, tag.getId(), "ID tag non coerente");
        TestAssertions.assertFalse(tag.isSystem(), "Tag personale marcato come sistema");
        TestAssertions.assertEquals("utente@test.it", tag.getEmailProprietario(),
                "Proprietario tag non coerente");
        TestAssertions.assertEquals("study.png", tag.getIcona(),
                "Icona tag non coerente");

        Fonte fonte = new Fonte(3L, "Stipendio", true, null);
        TestAssertions.assertEquals(3L, fonte.getId(), "ID fonte non coerente");
        TestAssertions.assertEquals("Stipendio", fonte.getNome(), "Nome fonte non coerente");
    }

    private static void testBudget() {
        Budget budget = new Budget(10L, "utente@test.it", 4L, 1L,
                new BigDecimal("500.00"), new BigDecimal("125.50"), true);
        TestAssertions.assertEquals(10L, budget.getId(), "ID budget non coerente");
        TestAssertions.assertEquals("utente@test.it", budget.getEmail(), "Email budget non coerente");
        TestAssertions.assertEquals(4L, budget.getIdPeriodo(), "Periodo budget non coerente");
        TestAssertions.assertEquals(1L, budget.getIdCategoria(), "Categoria budget non coerente");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("500.00"), budget.getImportoLimite(),
                "Limite budget non coerente");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("125.50"), budget.getTotaleSpesoAttuale(),
                "Totale speso budget non coerente");
        TestAssertions.assertTrue(budget.isAlertSoglia(), "Alert budget non coerente");
    }

    private static void testTransazione() {
        LocalDate data = LocalDate.of(2026, 5, 17);
        Transazione transazione = new Transazione(8L, TipoTransazione.SPESA,
                new BigDecimal("19.99"), data, "Netflix", "utente@test.it",
                2L, 6L, null, 12L);
        TestAssertions.assertEquals(8L, transazione.getId(), "ID transazione non coerente");
        TestAssertions.assertEquals(TipoTransazione.SPESA, transazione.getTipo(),
                "Tipo transazione non coerente");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("19.99"), transazione.getImporto(),
                "Importo transazione non coerente");
        TestAssertions.assertEquals(data, transazione.getData(), "Data transazione non coerente");
        TestAssertions.assertEquals("Netflix", transazione.getDescrizione(),
                "Descrizione transazione non coerente");
        TestAssertions.assertEquals(2L, transazione.getIdCategoria(),
                "Categoria transazione non coerente");
        TestAssertions.assertEquals(null, transazione.getIdFonte(),
                "Fonte transazione dovrebbe essere null per una spesa");
        TestAssertions.assertEquals(12L, transazione.getIdRicorrenza(),
                "Riferimento alla ricorrenza non coerente");
    }

    private static void testSpesaRicorrente() {
        LocalDate inizio = LocalDate.of(2026, 1, 1);
        LocalDate prossima = LocalDate.of(2026, 2, 1);
        SpesaRicorrente ricorrente = new SpesaRicorrente(5L, "Netflix",
                new BigDecimal("30.00"), 30, inizio, prossima, null,
                3L, "utente@test.it");
        TestAssertions.assertEquals(5L, ricorrente.getId(), "ID ricorrenza non coerente");
        TestAssertions.assertEquals("Netflix", ricorrente.getNome(),
                "Nome ricorrenza non coerente");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("30.00"),
                ricorrente.getImportoPrevisto(), "Importo ricorrenza non coerente");
        TestAssertions.assertEquals(30, ricorrente.getFrequenzaGiorni(),
                "Frequenza ricorrenza non coerente");
        TestAssertions.assertEquals(inizio, ricorrente.getDataInizio(),
                "Data inizio ricorrenza non coerente");
        TestAssertions.assertEquals(prossima, ricorrente.getDataProssimaScadenza(),
                "Data prossima scadenza non coerente");
        TestAssertions.assertEquals(null, ricorrente.getScadenza(),
                "Scadenza finale dovrebbe essere null");
    }

    private static void testUtente() {
        Utente utente = new Utente("utente@test.it", "hash", "Mario", "Rossi", Ruolo.UTENTE);
        TestAssertions.assertEquals("utente@test.it", utente.getEmail(), "Email utente non coerente");
        TestAssertions.assertEquals("Mario", utente.getNome(), "Nome utente non coerente");
        TestAssertions.assertEquals("Rossi", utente.getCognome(), "Cognome utente non coerente");
        TestAssertions.assertEquals(Ruolo.UTENTE, utente.getRuolo(), "Ruolo utente non coerente");
    }
}
