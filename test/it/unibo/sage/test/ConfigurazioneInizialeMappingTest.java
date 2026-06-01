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
        testTagPerGruppoEssenziali();
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
    private static void testTagPerGruppoEssenziali() throws Exception {
        final List<String> tag = (List<String>) callPrivate("tagPerGruppo", "essenziali");
        TestAssertions.assertTrue(tag.contains("Essenziale"),
                "Gruppo essenziali deve contenere il tag Essenziale");
        TestAssertions.assertTrue(tag.contains("Casa"),
                "Gruppo essenziali deve contenere il tag Casa");
        TestAssertions.assertTrue(tag.contains("Trasporti"),
                "Gruppo essenziali deve contenere il tag Trasporti");
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
