package it.unibo.sage.model;

public enum Ruolo {
    UTENTE,
    ADMIN;

    public static Ruolo fromDb(final String value) {
        return value == null ? UTENTE : Ruolo.valueOf(value.toUpperCase());
    }
}
