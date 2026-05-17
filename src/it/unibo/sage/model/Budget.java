package it.unibo.sage.model;

import java.math.BigDecimal;

public class Budget {

    private long id;
    private String email;
    private long idPeriodo;
    private Long idCategoria;
    private BigDecimal importoLimite;
    private BigDecimal totaleSpesoAttuale;
    private boolean alertSoglia;

    public Budget(final long id, final String email, final long idPeriodo, final Long idCategoria,
            final BigDecimal importoLimite, final BigDecimal totaleSpesoAttuale,
            final boolean alertSoglia) {
        this.id = id;
        this.email = email;
        this.idPeriodo = idPeriodo;
        this.idCategoria = idCategoria;
        this.importoLimite = importoLimite;
        this.totaleSpesoAttuale = totaleSpesoAttuale;
        this.alertSoglia = alertSoglia;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public long getIdPeriodo() {
        return idPeriodo;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public BigDecimal getImportoLimite() {
        return importoLimite;
    }

    public BigDecimal getTotaleSpesoAttuale() {
        return totaleSpesoAttuale;
    }

    public boolean isAlertSoglia() {
        return alertSoglia;
    }
}
