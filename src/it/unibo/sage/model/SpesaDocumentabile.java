package it.unibo.sage.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SpesaDocumentabile {

    private final long idTransazione;
    private final LocalDate data;
    private final BigDecimal importo;
    private final String descrizione;

    public SpesaDocumentabile(final long idTransazione, final LocalDate data,
            final BigDecimal importo, final String descrizione) {
        this.idTransazione = idTransazione;
        this.data = data;
        this.importo = importo;
        this.descrizione = descrizione;
    }

    public long getIdTransazione() {
        return idTransazione;
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getImporto() {
        return importo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    @Override
    public String toString() {
        return data + " - " + importo + " euro - " + descrizione;
    }
}
