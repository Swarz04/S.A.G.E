package it.unibo.sage.test;

import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.Tag;

public final class ClassificationModelTest {

    private ClassificationModelTest() {
    }

    public static void runAll() {
        testCategoriaSistema();
        testTagPersonale();
        testFontePersonale();
    }

    private static void testCategoriaSistema() {
        Categoria categoria = new Categoria(1L, "Casa", true, null, "house.png");
        TestAssertions.assertEquals(1L, categoria.getId(), "ID categoria non coerente");
        TestAssertions.assertEquals("Casa", categoria.getNome(), "Nome categoria non coerente");
        TestAssertions.assertTrue(categoria.isSystem(), "Categoria Casa deve essere di sistema nel test");
        TestAssertions.assertEquals(null, categoria.getEmailProprietario(),
                "Categoria di sistema non deve avere proprietario");
        TestAssertions.assertEquals("house.png", categoria.getIcona(),
                "Icona categoria non coerente");
    }

    private static void testTagPersonale() {
        Tag tag = new Tag(10L, "Esame", false, "studente@test.it", "study.png");
        TestAssertions.assertEquals(10L, tag.getId(), "ID tag non coerente");
        TestAssertions.assertEquals("Esame", tag.getNome(), "Nome tag non coerente");
        TestAssertions.assertFalse(tag.isSystem(), "Tag personale non deve essere di sistema");
        TestAssertions.assertEquals("studente@test.it", tag.getEmailProprietario(),
                "Proprietario tag non coerente");
        TestAssertions.assertEquals("study.png", tag.getIcona(),
                "Icona tag non coerente");
    }

    private static void testFontePersonale() {
        Fonte fonte = new Fonte(20L, "Ripetizioni private", false,
                "studente@test.it", "tutoring.png");
        TestAssertions.assertEquals(20L, fonte.getId(), "ID fonte non coerente");
        TestAssertions.assertEquals("Ripetizioni private", fonte.getNome(), "Nome fonte non coerente");
        TestAssertions.assertFalse(fonte.isSystem(), "Fonte personale non deve essere di sistema");
        TestAssertions.assertEquals("studente@test.it", fonte.getEmailProprietario(),
                "Proprietario fonte non coerente");
        TestAssertions.assertEquals("tutoring.png", fonte.getIcona(),
                "Icona fonte non coerente");
    }
}
