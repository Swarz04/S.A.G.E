package it.unibo.sage.test;

import it.unibo.sage.model.Documento;
import it.unibo.sage.model.DocumentoDettaglio;
import it.unibo.sage.model.SpesaDocumentabile;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class DocumentoModelTest {

    private DocumentoModelTest() {
    }

    public static void runAll() {
        testDocumentoBase();
        testDocumentoDettaglio();
        testSpesaDocumentabileToString();
    }

    private static void testDocumentoBase() {
        LocalDate acquisizione = LocalDate.of(2026, 5, 20);
        Documento documento = new Documento(1L, 15L, "ricevute/mensa.pdf", "PDF", acquisizione);
        TestAssertions.assertEquals(1L, documento.getId(), "ID documento non coerente");
        TestAssertions.assertEquals(15L, documento.getIdTransazione(), "Transazione documento non coerente");
        TestAssertions.assertEquals("ricevute/mensa.pdf", documento.getPathFile(), "Path documento non coerente");
        TestAssertions.assertEquals("PDF", documento.getTipoFile(), "Tipo documento non coerente");
        TestAssertions.assertEquals(acquisizione, documento.getDataAcquisizione(),
                "Data acquisizione documento non coerente");
    }

    private static void testDocumentoDettaglio() {
        DocumentoDettaglio dettaglio = new DocumentoDettaglio(2L, 16L, "ricevute/libri.jpg", "JPG",
                LocalDate.of(2026, 5, 21), LocalDate.of(2026, 5, 20),
                new BigDecimal("39.90"), "Manuale basi di dati");
        TestAssertions.assertEquals(2L, dettaglio.getIdDocumento(), "ID dettaglio documento non coerente");
        TestAssertions.assertEquals(16L, dettaglio.getIdTransazione(),
                "ID transazione dettaglio non coerente");
        TestAssertions.assertBigDecimalEquals(new BigDecimal("39.90"), dettaglio.getImportoSpesa(),
                "Importo spesa dettaglio non coerente");
        TestAssertions.assertEquals("Manuale basi di dati", dettaglio.getDescrizioneSpesa(),
                "Descrizione spesa dettaglio non coerente");
    }

    private static void testSpesaDocumentabileToString() {
        SpesaDocumentabile spesa = new SpesaDocumentabile(3L, LocalDate.of(2026, 5, 22),
                new BigDecimal("12.00"), "Mensa");
        TestAssertions.assertEquals(3L, spesa.getIdTransazione(), "ID spesa documentabile non coerente");
        TestAssertions.assertTrue(spesa.toString().contains("Mensa"),
                "La rappresentazione testuale deve contenere la descrizione");
        TestAssertions.assertTrue(spesa.toString().contains("12.00"),
                "La rappresentazione testuale deve contenere l'importo");
    }
}
