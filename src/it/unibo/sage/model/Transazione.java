package it.unibo.sage.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transazione {

    private long id;
    private TipoTransazione tipo;
    private BigDecimal importo;
    private LocalDate data;
    private String descrizione;
    private String email;
    private Long idCategoria;
    private long idPeriodo;
    private Long idFonte;
    private Long idRicorrenza;

    public Transazione(final long id, final TipoTransazione tipo, final BigDecimal importo,
            final LocalDate data, final String descrizione, final String email,
            final Long idCategoria, final long idPeriodo, final Long idFonte) {
        this(id, tipo, importo, data, descrizione, email, idCategoria, idPeriodo, idFonte, null);
    }

    public Transazione(final long id, final TipoTransazione tipo, final BigDecimal importo,
            final LocalDate data, final String descrizione, final String email,
            final Long idCategoria, final long idPeriodo, final Long idFonte,
            final Long idRicorrenza) {
        this.id = id;
        this.tipo = tipo;
        this.importo = importo;
        this.data = data;
        this.descrizione = descrizione;
        this.email = email;
        this.idCategoria = idCategoria;
        this.idPeriodo = idPeriodo;
        this.idFonte = idFonte;
        this.idRicorrenza = idRicorrenza;
    }

    public long getId() {
        return id;
    }

    public TipoTransazione getTipo() {
        return tipo;
    }

    public BigDecimal getImporto() {
        return importo;
    }

    public LocalDate getData() {
        return data;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getEmail() {
        return email;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public long getIdPeriodo() {
        return idPeriodo;
    }

    public Long getIdFonte() {
        return idFonte;
    }

    public Long getIdRicorrenza() {
        return idRicorrenza;
    }
}
