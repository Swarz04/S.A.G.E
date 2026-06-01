package it.unibo.sage.test;

import java.math.BigDecimal;
import java.util.Objects;

public final class TestAssertions {

    private TestAssertions() {
    }

    public static void assertTrue(final boolean condition, final String message) {
        if (!condition) {
            fail(message);
        }
    }

    public static void assertFalse(final boolean condition, final String message) {
        assertTrue(!condition, message);
    }

    public static void assertEquals(final Object expected, final Object actual, final String message) {
        if (!Objects.equals(expected, actual)) {
            fail(message + " - atteso: " + expected + ", ottenuto: " + actual);
        }
    }

    public static void assertBigDecimalEquals(final BigDecimal expected, final BigDecimal actual,
            final String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || actual == null || expected.compareTo(actual) != 0) {
            fail(message + " - atteso: " + expected + ", ottenuto: " + actual);
        }
    }

    public static void assertThrows(final Class<? extends Throwable> expectedType,
            final Runnable executable, final String message) {
        try {
            executable.run();
        } catch (final Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return;
            }
            fail(message + " - eccezione attesa: " + expectedType.getSimpleName()
                    + ", ottenuta: " + actual.getClass().getSimpleName());
        }
        fail(message + " - nessuna eccezione lanciata");
    }

    public static void fail(final String message) {
        throw new AssertionError(message);
    }
}
