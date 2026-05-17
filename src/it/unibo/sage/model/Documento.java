package it.unibo.sage.model;

import java.time.LocalDate;

public class Documento {

    private long id;
    private long idTransazione;
    private String pathFile;
    private String tipoFile;
    private LocalDate dataAcquisizione;

    public Documento(final long id, final long idTransazione, final String pathFile,
            final String tipoFile, final LocalDate dataAcquisizione) {
        this.id = id;
        this.idTransazione = idTransazione;
        this.pathFile = pathFile;
        this.tipoFile = tipoFile;
        this.dataAcquisizione = dataAcquisizione;
    }

    public long getId() {
        return id;
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
}
