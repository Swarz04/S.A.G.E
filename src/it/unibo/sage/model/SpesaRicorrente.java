package it.unibo.sage.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SpesaRicorrente {

    private long id;
    private String nome;
    private BigDecimal importoPrevisto;
    private int frequenzaGiorni;
    private LocalDate dataInizio;
    private LocalDate dataProssimaScadenza;
    private LocalDate scadenza;
    private long idCategoria;
    private String email;

    public SpesaRicorrente(final long id, final BigDecimal importoPrevisto,
            final int frequenzaGiorni, final LocalDate dataInizio,
            final LocalDate dataProssimaScadenza, final LocalDate scadenza,
            final long idCategoria, final String email) {
        this(id, "Spesa ricorrente", importoPrevisto, frequenzaGiorni, dataInizio,
                dataProssimaScadenza, scadenza, idCategoria, email);
    }

    public SpesaRicorrente(final long id, final String nome, final BigDecimal importoPrevisto,
            final int frequenzaGiorni, final LocalDate dataInizio,
            final LocalDate dataProssimaScadenza, final LocalDate scadenza,
            final long idCategoria, final String email) {
        this.id = id;
        this.nome = nome;
        this.importoPrevisto = importoPrevisto;
        this.frequenzaGiorni = frequenzaGiorni;
        this.dataInizio = dataInizio;
        this.dataProssimaScadenza = dataProssimaScadenza;
        this.scadenza = scadenza;
        this.idCategoria = idCategoria;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getImportoPrevisto() {
        return importoPrevisto;
    }

    public int getFrequenzaGiorni() {
        return frequenzaGiorni;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public LocalDate getDataProssimaScadenza() {
        return dataProssimaScadenza;
    }

    public LocalDate getScadenza() {
        return scadenza;
    }

    public long getIdCategoria() {
        return idCategoria;
    }

    public String getEmail() {
        return email;
    }
}
