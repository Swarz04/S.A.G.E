package it.unibo.sage.dao;

import it.unibo.sage.model.Documento;
import it.unibo.sage.model.DocumentoDettaglio;
import it.unibo.sage.model.SpesaDocumentabile;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcDocumentoDAO implements DocumentoDAO {

    private static final String FIND_DOCUMENTI_SQL =
            "SELECT D.ID_Documento, D.ID_Transizione, D.Path_File, D.Tipo_File, "
            + "D.Data_Acquisizione_Documento, T.Data, T.Importo, T.Descrizione "
            + "FROM DOCUMENTO D "
            + "JOIN TRANSIZIONE T ON D.ID_Transizione = T.ID_Transizione "
            + "WHERE T.Email = ? AND T.TipoTransazione = 'S' "
            + "ORDER BY T.Data DESC, D.ID_Documento DESC";

    private static final String FIND_SPESE_SENZA_DOCUMENTO_SQL =
            "SELECT T.ID_Transizione, T.Data, T.Importo, T.Descrizione "
            + "FROM TRANSIZIONE T "
            + "LEFT JOIN DOCUMENTO D ON D.ID_Transizione = T.ID_Transizione "
            + "WHERE T.Email = ? AND T.TipoTransazione = 'S' "
            + "AND D.ID_Documento IS NULL "
            + "ORDER BY T.Data DESC, T.ID_Transizione DESC";

    private static final String INSERT_DOCUMENTO_PER_SPESA_SQL =
            "INSERT INTO DOCUMENTO "
            + "(ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento) "
            + "SELECT T.ID_Transizione, ?, ?, ? "
            + "FROM TRANSIZIONE T "
            + "WHERE T.ID_Transizione = ? AND T.Email = ? AND T.TipoTransazione = 'S' "
            + "AND NOT EXISTS ("
            + "    SELECT 1 FROM DOCUMENTO D WHERE D.ID_Transizione = T.ID_Transizione"
            + ")";

    private static final String UPDATE_DOCUMENTO_SQL =
            "UPDATE DOCUMENTO D "
            + "SET D.Path_File = ?, D.Tipo_File = ?, D.Data_Acquisizione_Documento = ? "
            + "WHERE D.ID_Documento = ? "
            + "AND EXISTS ("
            + "    SELECT 1 FROM TRANSIZIONE T "
            + "    WHERE T.ID_Transizione = D.ID_Transizione "
            + "    AND T.Email = ? "
            + "    AND T.TipoTransazione = 'S'"
            + ")";

    private static final String DELETE_DOCUMENTO_SQL =
            "DELETE D FROM DOCUMENTO D "
            + "WHERE D.ID_Documento = ? "
            + "AND EXISTS ("
            + "    SELECT 1 FROM TRANSIZIONE T "
            + "    WHERE T.ID_Transizione = D.ID_Transizione "
            + "    AND T.Email = ? "
            + "    AND T.TipoTransazione = 'S'"
            + ")";

    private final Connection connection;

    public JdbcDocumentoDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<DocumentoDettaglio> findDocumentiByUtente(final String email)
            throws SQLException {
        final List<DocumentoDettaglio> documenti = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_DOCUMENTI_SQL)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    documenti.add(mapDocumentoDettaglio(resultSet));
                }
            }
        }
        return documenti;
    }

    @Override
    public List<SpesaDocumentabile> findSpeseSenzaDocumentoByUtente(final String email)
            throws SQLException {
        final List<SpesaDocumentabile> spese = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                FIND_SPESE_SENZA_DOCUMENTO_SQL)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    spese.add(mapSpesaDocumentabile(resultSet));
                }
            }
        }
        return spese;
    }

    @Override
    public void inserisciDocumentoPerSpesa(final String email, final long idTransazione,
            final String pathFile, final String tipoFile, final LocalDate dataAcquisizione)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_DOCUMENTO_PER_SPESA_SQL)) {
            statement.setString(1, pathFile);
            statement.setString(2, tipoFile);
            statement.setDate(3, Date.valueOf(dataAcquisizione));
            statement.setLong(4, idTransazione);
            statement.setString(5, email);

            final int insertedRows = statement.executeUpdate();
            if (insertedRows == 0) {
                throw new SQLException("Spesa non valida o gia' associata a un documento.");
            }
        }
    }

    @Override
    public void update(final String email, final Documento documento) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_DOCUMENTO_SQL)) {
            statement.setString(1, documento.getPathFile());
            statement.setString(2, documento.getTipoFile());
            statement.setDate(3, Date.valueOf(documento.getDataAcquisizione()));
            statement.setLong(4, documento.getId());
            statement.setString(5, email);

            final int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Documento non trovato o non modificabile per questo utente.");
            }
        }
    }

    @Override
    public void delete(final String email, final long idDocumento) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_DOCUMENTO_SQL)) {
            statement.setLong(1, idDocumento);
            statement.setString(2, email);

            final int deletedRows = statement.executeUpdate();
            if (deletedRows == 0) {
                throw new SQLException("Documento non trovato o non eliminabile per questo utente.");
            }
        }
    }

    private DocumentoDettaglio mapDocumentoDettaglio(final ResultSet resultSet)
            throws SQLException {
        return new DocumentoDettaglio(
                resultSet.getLong("ID_Documento"),
                resultSet.getLong("ID_Transizione"),
                resultSet.getString("Path_File"),
                resultSet.getString("Tipo_File"),
                resultSet.getDate("Data_Acquisizione_Documento").toLocalDate(),
                resultSet.getDate("Data").toLocalDate(),
                resultSet.getBigDecimal("Importo"),
                resultSet.getString("Descrizione"));
    }

    private SpesaDocumentabile mapSpesaDocumentabile(final ResultSet resultSet)
            throws SQLException {
        return new SpesaDocumentabile(
                resultSet.getLong("ID_Transizione"),
                resultSet.getDate("Data").toLocalDate(),
                resultSet.getBigDecimal("Importo"),
                resultSet.getString("Descrizione"));
    }
}
