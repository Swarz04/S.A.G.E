package it.unibo.sage.test;

import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.service.SpeseRicorrentiService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class SpeseRicorrentiServiceTest {

    private SpeseRicorrentiServiceTest() {
    }

    public static void runAll() {
        testCalcolaScadenzeSenzaDataFine();
        testCalcolaScadenzeConDataFine();
        testCalcolaScadenzeVuoteSeNonScaduta();
        testValidazioneFrequenza();
        testValidazioneScadenzaFinale();
    }

    private static void testCalcolaScadenzeSenzaDataFine() {
        SpeseRicorrentiService service = new SpeseRicorrentiService();
        SpesaRicorrente modello = new SpesaRicorrente(1L, new BigDecimal("10.00"),
                30, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1),
                null, 2L, "utente@test.it");

        List<LocalDate> scadenze = service.calcolaScadenzeDaGenerare(
                modello, LocalDate.of(2026, 3, 5));

        TestAssertions.assertEquals(3, scadenze.size(),
                "Devono essere generate tre scadenze entro il 5 marzo");
        TestAssertions.assertEquals(LocalDate.of(2026, 1, 1), scadenze.get(0),
                "Prima scadenza non coerente");
        TestAssertions.assertEquals(LocalDate.of(2026, 1, 31), scadenze.get(1),
                "Seconda scadenza non coerente");
        TestAssertions.assertEquals(LocalDate.of(2026, 3, 2), scadenze.get(2),
                "Terza scadenza non coerente");
    }

    private static void testCalcolaScadenzeConDataFine() {
        SpeseRicorrentiService service = new SpeseRicorrentiService();
        SpesaRicorrente modello = new SpesaRicorrente(1L, new BigDecimal("10.00"),
                15, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 20), 2L, "utente@test.it");

        List<LocalDate> scadenze = service.calcolaScadenzeDaGenerare(
                modello, LocalDate.of(2026, 2, 1));

        TestAssertions.assertEquals(2, scadenze.size(),
                "La data finale deve bloccare le generazioni successive");
        TestAssertions.assertEquals(LocalDate.of(2026, 1, 1), scadenze.get(0),
                "Prima scadenza non coerente con data finale");
        TestAssertions.assertEquals(LocalDate.of(2026, 1, 16), scadenze.get(1),
                "Seconda scadenza non coerente con data finale");
    }

    private static void testCalcolaScadenzeVuoteSeNonScaduta() {
        SpeseRicorrentiService service = new SpeseRicorrentiService();
        SpesaRicorrente modello = new SpesaRicorrente(1L, new BigDecimal("10.00"),
                30, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1),
                null, 2L, "utente@test.it");

        List<LocalDate> scadenze = service.calcolaScadenzeDaGenerare(
                modello, LocalDate.of(2026, 5, 31));

        TestAssertions.assertTrue(scadenze.isEmpty(),
                "Non deve essere generata alcuna scadenza non ancora maturata");
    }

    private static void testValidazioneFrequenza() {
        SpeseRicorrentiService service = new SpeseRicorrentiService();
        TestAssertions.assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.aggiungiRicorrenza("utente@test.it", new BigDecimal("10.00"), 0,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1),
                        null, 2L);
            } catch (final SQLException ex) {
                throw new RuntimeException(ex);
            }
        }, "La frequenza pari a zero deve essere rifiutata prima dell'accesso al DB");
    }

    private static void testValidazioneScadenzaFinale() {
        SpeseRicorrentiService service = new SpeseRicorrentiService();
        TestAssertions.assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.aggiungiRicorrenza("utente@test.it", new BigDecimal("10.00"), 30,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 31), 2L);
            } catch (final SQLException ex) {
                throw new RuntimeException(ex);
            }
        }, "La scadenza finale precedente alla prossima scadenza deve essere rifiutata");
    }
}
