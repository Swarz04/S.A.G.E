package it.unibo.sage.dao;

import it.unibo.sage.model.DocumentoDettaglio;
import it.unibo.sage.model.Documento;
import it.unibo.sage.model.SpesaDocumentabile;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface DocumentoDAO {

    List<DocumentoDettaglio> findDocumentiByUtente(String email) throws SQLException;

    List<SpesaDocumentabile> findSpeseSenzaDocumentoByUtente(String email) throws SQLException;

    void inserisciDocumentoPerSpesa(String email, long idTransazione, String pathFile,
            String tipoFile, LocalDate dataAcquisizione) throws SQLException;

    void update(String email, Documento documento) throws SQLException;

    void delete(String email, long idDocumento) throws SQLException;
}
