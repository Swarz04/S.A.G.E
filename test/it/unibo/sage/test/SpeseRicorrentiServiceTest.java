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
        testNomeRicorrenza();
        testValidazioneNomeRicorrenza();
        testProssimaScadenzaDopoPrimaRegistrazione();
        testBackfillDallaDataInizio();
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
        TestAssertions.assertEquals(LocalDate.of(2026, 2, 1), scadenze.get(1),
                "La frequenza 30 deve mantenere la cadenza mensile");
        TestAssertions.assertEquals(LocalDate.of(2026, 3, 1), scadenze.get(2),
                "Terza scadenza mensile non coerente");
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
    private static void testNomeRicorrenza() {
        SpesaRicorrente modello = new SpesaRicorrente(4L, "Netflix",
                new BigDecimal("12.99"), 30, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1), null, 2L, "utente@test.it");
        TestAssertions.assertEquals("Netflix", modello.getNome(),
                "La ricorrenza deve esporre un nome leggibile");
    }

    private static void testValidazioneNomeRicorrenza() {
        SpeseRicorrentiService service = new SpeseRicorrentiService();
        TestAssertions.assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.aggiungiRicorrenza("utente@test.it", "   ",
                        new BigDecimal("10.00"), 30,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1),
                        null, 2L);
            } catch (final SQLException ex) {
                throw new RuntimeException(ex);
            }
        }, "Il nome vuoto deve essere rifiutato prima dell'accesso al DB");
    }

    private static void testProssimaScadenzaDopoPrimaRegistrazione() {
        SpeseRicorrentiService service = new SpeseRicorrentiService();
        LocalDate prossima = service.calcolaProssimaScadenzaDopoRegistrazione(
                LocalDate.of(2026, 6, 15), 30, LocalDate.of(2026, 6, 15));
        TestAssertions.assertEquals(LocalDate.of(2026, 7, 15), prossima,
                "Dopo la prima spesa la prossima scadenza deve avanzare");

        LocalDate futura = service.calcolaProssimaScadenzaDopoRegistrazione(
                LocalDate.of(2026, 7, 15), 30, LocalDate.of(2026, 6, 15));
        TestAssertions.assertEquals(LocalDate.of(2026, 7, 15), futura,
                "Una prossima scadenza futura non deve essere modificata");
    }

    private static void testBackfillDallaDataInizio() {
        SpeseRicorrentiService service = new SpeseRicorrentiService();
        SpesaRicorrente modello = new SpesaRicorrente(9L, "Affitto",
                new BigDecimal("150.00"), 30, LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 7, 15), null, 2L, "utente@test.it");

        List<LocalDate> scadenze = service.calcolaScadenzeDaDataInizio(
                modello, LocalDate.of(2026, 6, 15));

        TestAssertions.assertEquals(6, scadenze.size(),
                "Una ricorrenza iniziata a gennaio deve produrre una rata per ogni mese maturato");
        TestAssertions.assertEquals(LocalDate.of(2026, 1, 15), scadenze.get(0),
                "La prima rata deve coincidere con la data iniziale");
        TestAssertions.assertEquals(LocalDate.of(2026, 6, 15), scadenze.get(5),
                "Deve essere inclusa anche la rata del mese corrente");
    }

}
