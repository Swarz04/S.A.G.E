package it.unibo.sage.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DocumentoDettaglio {

    private final long idDocumento;
    private final long idTransazione;
    private final String pathFile;
    private final String tipoFile;
    private final LocalDate dataAcquisizione;
    private final LocalDate dataSpesa;
    private final BigDecimal importoSpesa;
    private final String descrizioneSpesa;

    public DocumentoDettaglio(final long idDocumento, final long idTransazione,
            final String pathFile, final String tipoFile, final LocalDate dataAcquisizione,
            final LocalDate dataSpesa, final BigDecimal importoSpesa,
            final String descrizioneSpesa) {
        this.idDocumento = idDocumento;
        this.idTransazione = idTransazione;
        this.pathFile = pathFile;
        this.tipoFile = tipoFile;
        this.dataAcquisizione = dataAcquisizione;
        this.dataSpesa = dataSpesa;
        this.importoSpesa = importoSpesa;
        this.descrizioneSpesa = descrizioneSpesa;
    }

    public long getIdDocumento() {
        return idDocumento;
    }

    public long getIdTransazione() {
        return idTransazione;
    }

    public String getPathFile() {
        return pathFile;
    }

    public String getTipoFile() {
        return tipoFile;
    }

    public LocalDate getDataAcquisizione() {
        return dataAcquisizione;
    }

    public LocalDate getDataSpesa() {
        return dataSpesa;
    }

    public BigDecimal getImportoSpesa() {
        return importoSpesa;
    }

    public String getDescrizioneSpesa() {
        return descrizioneSpesa;
    }
}
