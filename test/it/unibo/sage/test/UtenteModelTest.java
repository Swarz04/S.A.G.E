package it.unibo.sage.test;

import it.unibo.sage.model.Ruolo;
import it.unibo.sage.model.Utente;

public final class UtenteModelTest {

    private UtenteModelTest() {
    }

    public static void runAll() {
        testUtenteStandard();
        testRuoloDefaultENormalizzazione();
        testRuoloNonValido();
    }

    private static void testUtenteStandard() {
        Utente utente = new Utente("studente@test.it", "password", "Mario", "Rossi", Ruolo.UTENTE);
        TestAssertions.assertEquals("studente@test.it", utente.getEmail(), "Email utente non coerente");
        TestAssertions.assertEquals("password", utente.getPassword(), "Password utente non coerente");
        TestAssertions.assertEquals("Mario", utente.getNome(), "Nome utente non coerente");
        TestAssertions.assertEquals("Rossi", utente.getCognome(), "Cognome utente non coerente");
        TestAssertions.assertEquals(Ruolo.UTENTE, utente.getRuolo(), "Ruolo utente non coerente");
    }

    private static void testRuoloDefaultENormalizzazione() {
        TestAssertions.assertEquals(Ruolo.UTENTE, Ruolo.fromDb(null),
                "Ruolo null deve essere convertito in UTENTE");
        TestAssertions.assertEquals(Ruolo.ADMIN, Ruolo.fromDb("admin"),
                "Ruolo admin minuscolo deve essere convertito in ADMIN");
        TestAssertions.assertEquals(Ruolo.UTENTE, Ruolo.fromDb("UTENTE"),
                "Ruolo UTENTE maiuscolo deve essere riconosciuto");
    }

    private static void testRuoloNonValido() {
        TestAssertions.assertThrows(IllegalArgumentException.class,
                () -> Ruolo.fromDb("superuser"),
                "Un ruolo non previsto deve essere rifiutato");
    }
}
