package it.unibo.sage.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class IconResourceTest {

    private IconResourceTest() {
    }

    public static void runAll() {
        testIconeCategorieTagFontiPresentiInSrc();
        testIconeCaricabiliDaClasspathOCartellaSorgente();
    }

    private static void testIconeCategorieTagFontiPresentiInSrc() {
        final List<String> requiredIcons = List.of(
                "house.png", "food.png", "transport.png", "health.png", "leisure.png",
                "study.png", "shopping.png", "generic_category.png", "generic_tag.png",
                "salary.png", "gift.png", "refund.png", "scholarship.png", "tutoring.png",
                "generic_source.png");
        final Path iconsDir = Path.of("src", "it", "unibo", "sage", "view", "icons");
        TestAssertions.assertTrue(Files.isDirectory(iconsDir),
                "La cartella src/it/unibo/sage/view/icons deve esistere");
        for (final String icon : requiredIcons) {
            final Path iconPath = iconsDir.resolve(icon);
            TestAssertions.assertTrue(Files.isRegularFile(iconPath),
                    "Icona mancante in src: " + iconPath);
        }
    }

    private static void testIconeCaricabiliDaClasspathOCartellaSorgente() {
        final String resource = "/it/unibo/sage/view/icons/house.png";
        final boolean fromClasspath = IconResourceTest.class.getResource(resource) != null;
        final boolean fromSrc = Files.isRegularFile(Path.of("src", "it", "unibo", "sage", "view", "icons", "house.png"));
        final boolean fromBin = Files.isRegularFile(Path.of("bin", "it", "unibo", "sage", "view", "icons", "house.png"));
        final boolean fromBuild = Files.isRegularFile(Path.of("build", "classes", "it", "unibo", "sage", "view", "icons", "house.png"));
        TestAssertions.assertTrue(fromClasspath || fromSrc || fromBin || fromBuild,
                "Le icone devono essere raggiungibili almeno da classpath, src, bin o build/classes");
    }
}
