package it.unibo.sage.model;

public enum TipoTransazione {
    SPESA("S"),
    ENTRATA("E");

    private final String dbValue;

    TipoTransazione(final String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static TipoTransazione fromDb(final String value) {
        if ("S".equalsIgnoreCase(value)) {
            return SPESA;
        }
        if ("E".equalsIgnoreCase(value)) {
            return ENTRATA;
        }
        throw new IllegalArgumentException("Tipo transazione non valido: " + value);
    }
}
