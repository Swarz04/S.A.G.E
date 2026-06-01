package it.unibo.sage.test;

import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class TransazioneModelTest {

    private TransazioneModelTest() {
    }

    public static void runAll() {
        testSpesaConCategoriaSenzaFonte();
        testEntrataConFonteSenzaCategoria();
        testTipoTransazioneMappingCaseInsensitive();
    }

    private static void testSpesaConCategoriaSenzaFonte() {
        Transazione spesa = new Transazione(11L, TipoTransazione.SPESA, new BigDecimal("42.50"),
                LocalDate.of(2026, 6, 1), "Libri universitari", "studente@test.it", 3L, 8L, null);
        TestAssertions.assertEquals(11L, spesa.getId(), "ID spesa non coerente");
        TestAssertions.assertEquals(TipoTransazione.SPESA, spesa.getTipo(), "Tipo spesa non coerente");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("42.50"), spesa.getImporto(),
                "Importo spesa non coerente");
        TestAssertions.assertEquals(3L, spesa.getIdCategoria(), "Categoria spesa non coerente");
        TestAssertions.assertEquals(null, spesa.getIdFonte(), "Una spesa non deve avere fonte");
    }

    private static void testEntrataConFonteSenzaCategoria() {
        Transazione entrata = new Transazione(12L, TipoTransazione.ENTRATA, new BigDecimal("250.00"),
                LocalDate.of(2026, 6, 2), "Ripetizioni private", "studente@test.it", null, 8L, 4L);
        TestAssertions.assertEquals(12L, entrata.getId(), "ID entrata non coerente");
        TestAssertions.assertEquals(TipoTransazione.ENTRATA, entrata.getTipo(), "Tipo entrata non coerente");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("250.00"), entrata.getImporto(),
                "Importo entrata non coerente");
        TestAssertions.assertEquals(null, entrata.getIdCategoria(), "Un'entrata non deve avere categoria");
        TestAssertions.assertEquals(4L, entrata.getIdFonte(), "Fonte entrata non coerente");
    }

    private static void testTipoTransazioneMappingCaseInsensitive() {
        TestAssertions.assertEquals(TipoTransazione.SPESA, TipoTransazione.fromDb("s"),
                "Il tipo s minuscolo deve essere SPESA");
        TestAssertions.assertEquals(TipoTransazione.ENTRATA, TipoTransazione.fromDb("e"),
                "Il tipo e minuscolo deve essere ENTRATA");
        TestAssertions.assertThrows(IllegalArgumentException.class,
                () -> TipoTransazione.fromDb("uscita"),
                "Un tipo DB non codificato deve essere rifiutato");
    }
}
