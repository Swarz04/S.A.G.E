package it.unibo.sage.dao;

import it.unibo.sage.model.Documento;
import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransazioneDAO implements TransazioneDAO {

    private static final String INSERT_SQL =
            "INSERT INTO TRANSIZIONE "
            + "(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_TAG_SQL =
            "INSERT INTO SPESA_TAG (ID_Transizione, ID_Tag) VALUES (?, ?)";

    private static final String INSERT_DOCUMENTO_SQL =
            "INSERT INTO DOCUMENTO "
            + "(ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento) "
            + "VALUES (?, ?, ?, ?)";

    private static final String FIND_BY_PERIODO_SQL =
            "SELECT ID_Transizione, TipoTransazione, Importo, Data, Descrizione, Email, "
            + "ID_Categoria, ID_Periodo, ID_Fonte "
            + "FROM TRANSIZIONE "
            + "WHERE Email = ? AND Data BETWEEN ? AND ? "
            + "ORDER BY Data DESC, ID_Transizione DESC";

    private final Connection connection;

    public JdbcTransazioneDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public long inserisci(final Transazione transazione) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, transazione.getTipo().getDbValue());
            statement.setBigDecimal(2, transazione.getImporto());
            statement.setDate(3, Date.valueOf(transazione.getData()));
            statement.setString(4, transazione.getDescrizione());
            statement.setString(5, transazione.getEmail());
            setNullableLong(statement, 6, transazione.getIdCategoria());
            statement.setLong(7, transazione.getIdPeriodo());
            setNullableLong(statement, 8, transazione.getIdFonte());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("Inserimento transazione senza chiave generata");
            }
        }
    }

    @Override
    public void associaTag(final long idTransazione, final long idTag) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_TAG_SQL)) {
            statement.setLong(1, idTransazione);
            statement.setLong(2, idTag);
            statement.executeUpdate();
        }
    }

    @Override
    public void inserisciDocumento(final Documento documento) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_DOCUMENTO_SQL)) {
            statement.setLong(1, documento.getIdTransazione());
            statement.setString(2, documento.getPathFile());
            statement.setString(3, documento.getTipoFile());
            statement.setDate(4, Date.valueOf(documento.getDataAcquisizione()));
            statement.executeUpdate();
        }
    }

    @Override
    public List<Transazione> findByPeriodo(final String email, final LocalDate dal,
            final LocalDate al) throws SQLException {
        final List<Transazione> transazioni = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_PERIODO_SQL)) {
            statement.setString(1, email);
            statement.setDate(2, Date.valueOf(dal));
            statement.setDate(3, Date.valueOf(al));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transazioni.add(mapTransazione(resultSet));
                }
            }
        }
        return transazioni;
    }

    private Transazione mapTransazione(final ResultSet resultSet) throws SQLException {
        return new Transazione(
                resultSet.getLong("ID_Transizione"),
                TipoTransazione.fromDb(resultSet.getString("TipoTransazione")),
                resultSet.getBigDecimal("Importo"),
                resultSet.getDate("Data").toLocalDate(),
                resultSet.getString("Descrizione"),
                resultSet.getString("Email"),
                readNullableLong(resultSet, "ID_Categoria"),
                resultSet.getLong("ID_Periodo"),
                readNullableLong(resultSet, "ID_Fonte"));
    }

    private void setNullableLong(final PreparedStatement statement, final int index,
            final Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setLong(index, value);
        }
    }

    private Long readNullableLong(final ResultSet resultSet, final String column)
            throws SQLException {
        final long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }
}
