package it.unibo.sage.test;

import it.unibo.sage.service.ConfigurazioneInizialeService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public final class ConfigurazioneInizialeMappingTest {

    private ConfigurazioneInizialeMappingTest() {
    }

    public static void runAll() throws Exception {
        testCategoriePerFocusUniversita();
        testCategorieDefault();
        testNomeFonteNormalizzato();
        testTagPerGruppoCasaTrasporti();
        testNessunGruppoCreaEssenziale();
    }

    @SuppressWarnings("unchecked")
    private static void testCategoriePerFocusUniversita() throws Exception {
        final List<String> categorie = (List<String>) callPrivate("categoriePerFocus", "universita");
        TestAssertions.assertTrue(categorie.contains("Mensa"),
                "Focus universita deve proporre Mensa");
        TestAssertions.assertTrue(categorie.contains("Libri"),
                "Focus universita deve proporre Libri");
        TestAssertions.assertTrue(categorie.contains("Trasporti"),
                "Focus universita deve proporre Trasporti");
    }

    @SuppressWarnings("unchecked")
    private static void testCategorieDefault() throws Exception {
        final List<String> categorie = (List<String>) callPrivate("categoriePerFocus", "altro");
        TestAssertions.assertTrue(categorie.contains("Alimentari"),
                "Focus non riconosciuto deve ricadere su Alimentari");
        TestAssertions.assertTrue(categorie.contains("Svago"),
                "Focus non riconosciuto deve ricadere su Svago");
    }

    private static void testNomeFonteNormalizzato() throws Exception {
        TestAssertions.assertEquals("Borsa di studio", callPrivate("nomeFonte", "borsa di studio"),
                "Fonte borsa di studio deve essere normalizzata");
        TestAssertions.assertEquals("Stipendio", callPrivate("nomeFonte", "stipendio"),
                "Fonte stipendio deve essere normalizzata");
        TestAssertions.assertEquals("Aiuto famiglia", callPrivate("nomeFonte", "aiuto famiglia"),
                "Fonte aiuto famiglia deve essere normalizzata");
    }

    @SuppressWarnings("unchecked")
    private static void testTagPerGruppoCasaTrasporti() throws Exception {
        final List<String> tag = (List<String>) callPrivate(
                "tagPerGruppo", "casa e trasporti");
        TestAssertions.assertTrue(tag.contains("Casa"),
                "Il gruppo casa e trasporti deve contenere Casa");
        TestAssertions.assertTrue(tag.contains("Trasporti"),
                "Il gruppo casa e trasporti deve contenere Trasporti");
        TestAssertions.assertFalse(tag.contains("Essenziale"),
                "Il tag Essenziale non deve più essere creato");
    }

    @SuppressWarnings("unchecked")
    private static void testNessunGruppoCreaEssenziale() throws Exception {
        for (final String gruppo : List.of("base", "studio", "lavoro", "essenziali")) {
            final List<String> tag = (List<String>) callPrivate("tagPerGruppo", gruppo);
            TestAssertions.assertFalse(tag.contains("Essenziale"),
                    "Nessun gruppo deve creare il tag Essenziale: " + gruppo);
        }
    }

    private static Object callPrivate(final String methodName, final String value) throws Exception {
        final Method method = ConfigurazioneInizialeService.class.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        try {
            return method.invoke(new ConfigurazioneInizialeService(), value);
        } catch (final InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw ex;
        }
    }
}
