package it.unibo.sage.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DashboardSourceInspectionTest {

    private DashboardSourceInspectionTest() {
    }

    public static void runAll() throws IOException {
        testDashboardGestisceClickSuCategoriaTagFonte();
        testDashboardMappaIconePrincipali();
        testDashboardHaFallbackIcone();
        testDashboardPermetteSceltaIcone();
        testDashboardPermetteIconeFonteETrascinamento();
        testDashboardMostraNomeRicorrenza();
    }

    private static void testDashboardGestisceClickSuCategoriaTagFonte() throws IOException {
        final String source = readDashboard()
                + readSource("src/it/unibo/sage/service/DashboardDataService.java");
        TestAssertions.assertTrue(source.contains("showClassificationTransactions"),
                "DashboardPanel deve contenere il metodo per mostrare transazioni correlate");
        TestAssertions.assertTrue(source.contains("caricaTransazioniPerCategoria"),
                "La dashboard deve caricare transazioni per categoria al click");
        TestAssertions.assertTrue(source.contains("caricaTransazioniPerTag"),
                "La dashboard deve caricare transazioni per tag al click");
        TestAssertions.assertTrue(source.contains("caricaTransazioniPerFonte"),
                "La dashboard deve caricare transazioni per fonte al click");
    }

    private static void testDashboardMappaIconePrincipali() throws IOException {
        final String source = readDashboard();
        final String[] expectedIcons = {
            "house.png", "food.png", "transport.png", "health.png", "leisure.png",
            "gift.png", "refund.png", "salary.png", "scholarship.png", "tutoring.png",
            "generic_category.png", "generic_tag.png", "generic_source.png"
        };
        for (final String icon : expectedIcons) {
            TestAssertions.assertTrue(source.contains(icon),
                    "DashboardPanel deve mappare o usare l'icona " + icon);
        }
    }

    private static void testDashboardHaFallbackIcone() throws IOException {
        final String source = readDashboard();
        TestAssertions.assertTrue(source.contains("paintFallbackClassificationIcon"),
                "DashboardPanel deve avere un fallback se manca una icona");
        TestAssertions.assertTrue(source.contains("normalizeClassificationName"),
                "DashboardPanel deve normalizzare i nomi prima della mappatura icone");
        TestAssertions.assertTrue(source.contains("possiblePaths"),
                "DashboardPanel deve provare più percorsi per caricare risorse icona");
    }

    private static void testDashboardPermetteSceltaIcone() throws IOException {
        final String source = readDashboard();
        TestAssertions.assertTrue(source.contains("CATEGORY_ICON_CHOICES"),
                "DashboardPanel deve offrire icone selezionabili per le categorie");
        TestAssertions.assertTrue(source.contains("TAG_ICON_CHOICES"),
                "DashboardPanel deve offrire icone selezionabili per i tag");
        TestAssertions.assertTrue(source.contains("createIconChoiceCombo"),
                "DashboardPanel deve mostrare il selettore delle icone");
        TestAssertions.assertTrue(source.contains("modificaCategoriaPersonale")
                        && source.contains("modificaTagPersonale"),
                "Il nome e l'icona devono poter essere aggiornati insieme");
    }

    private static void testDashboardPermetteIconeFonteETrascinamento() throws IOException {
        final String source = readDashboard();
        TestAssertions.assertTrue(source.contains("SOURCE_ICON_CHOICES")
                        && source.contains("modificaFontePersonale"),
                "Le fonti devono permettere scelta e modifica dell'icona");
        TestAssertions.assertTrue(source.contains("DataFlavor.javaFileListFlavor")
                        && source.contains("Trascina qui una tua immagine")
                        && source.contains("IconStorage.saveCustomIcon"),
                "Categorie, tag e fonti devono accettare immagini trascinate");
    }

    private static void testDashboardMostraNomeRicorrenza() throws IOException {
        final String source = readDashboard();
        TestAssertions.assertTrue(source.contains("ricorrenza.getNome()"),
                "La tabella delle ricorrenze deve mostrare il nome, ad esempio Netflix");
        TestAssertions.assertTrue(source.contains("addFormRow(form, \"Nome\""),
                "Il form delle ricorrenze deve chiedere un nome leggibile");
        TestAssertions.assertTrue(source.contains("generateDueRecurringExpensesOnStartup"),
                "Le ricorrenze scadute devono generare transazioni automaticamente");
    }

    private static String readDashboard() throws IOException {
        return readSource("src/it/unibo/sage/view/dashboard/DashboardPanel.java")
                + readSource("src/it/unibo/sage/view/dashboard/ClassificationIconSupport.java");
    }

    private static String readSource(final String fileName) throws IOException {
        final Path path = Path.of(fileName);
        TestAssertions.assertTrue(Files.isRegularFile(path), "File sorgente mancante: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
