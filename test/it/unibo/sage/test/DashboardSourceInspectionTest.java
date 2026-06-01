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
    }

    private static void testDashboardGestisceClickSuCategoriaTagFonte() throws IOException {
        final String source = readDashboard();
        TestAssertions.assertTrue(source.contains("showClassificationTransactions"),
                "DashboardPanel deve contenere il metodo per mostrare transazioni correlate");
        TestAssertions.assertTrue(source.contains("caricaTransazioniPerCategoria"),
                "DashboardPanel deve caricare transazioni per categoria al click");
        TestAssertions.assertTrue(source.contains("caricaTransazioniPerTag"),
                "DashboardPanel deve caricare transazioni per tag al click");
        TestAssertions.assertTrue(source.contains("caricaTransazioniPerFonte"),
                "DashboardPanel deve caricare transazioni per fonte al click");
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

    private static String readDashboard() throws IOException {
        final Path path = Path.of("src", "it", "unibo", "sage", "view", "DashboardPanel.java");
        TestAssertions.assertTrue(Files.isRegularFile(path), "DashboardPanel.java deve esistere");
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
