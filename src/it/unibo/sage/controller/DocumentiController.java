package it.unibo.sage.controller;

import it.unibo.sage.dao.DocumentoDAO;
import it.unibo.sage.dao.JdbcDocumentoDAO;
import it.unibo.sage.model.Documento;
import it.unibo.sage.model.DocumentoDettaglio;
import it.unibo.sage.model.SpesaDocumentabile;
import it.unibo.sage.utils.DatabaseConnection;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DocumentiController {

    public List<DocumentoDettaglio> caricaDocumentiUtente(final String email)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final DocumentoDAO documentoDAO = new JdbcDocumentoDAO(connection);
            return documentoDAO.findDocumentiByUtente(email);
        }
    }

    public List<SpesaDocumentabile> caricaSpeseDocumentabili(final String email)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final DocumentoDAO documentoDAO = new JdbcDocumentoDAO(connection);
            return documentoDAO.findSpeseSenzaDocumentoByUtente(email);
        }
    }

    public void aggiungiDocumento(final String email, final SpesaDocumentabile spesa,
            final File file) throws SQLException {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("Il file selezionato non esiste.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            final DocumentoDAO documentoDAO = new JdbcDocumentoDAO(connection);
            documentoDAO.inserisciDocumentoPerSpesa(
                    email,
                    spesa.getIdTransazione(),
                    file.getAbsolutePath(),
                    riconosciTipoFile(file.getName()),
                    LocalDate.now());
        }
    }

    public void modificaDocumento(final String email, final DocumentoDettaglio documento,
            final File nuovoFile) throws SQLException {
        if (documento == null) {
            throw new IllegalArgumentException("Documento non valido.");
        }
        if (nuovoFile == null || !nuovoFile.isFile()) {
            throw new IllegalArgumentException("Il file selezionato non esiste.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            final DocumentoDAO documentoDAO = new JdbcDocumentoDAO(connection);
            documentoDAO.update(email, new Documento(
                    documento.getIdDocumento(),
                    documento.getIdTransazione(),
                    nuovoFile.getAbsolutePath(),
                    riconosciTipoFile(nuovoFile.getName()),
                    LocalDate.now()));
        }
    }

    public void eliminaDocumento(final String email, final long idDocumento)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final DocumentoDAO documentoDAO = new JdbcDocumentoDAO(connection);
            documentoDAO.delete(email, idDocumento);
        }
    }

    public void apriDocumento(final String pathFile) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Apertura file non supportata su questo sistema.");
        }

        final File file = new File(pathFile);
        if (!file.isFile()) {
            throw new IOException("File non trovato: " + pathFile);
        }

        Desktop.getDesktop().open(file);
    }

    private String riconosciTipoFile(final String fileName) {
        final int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "FILE";
        }
        return fileName.substring(dotIndex + 1).toUpperCase();
    }
}
