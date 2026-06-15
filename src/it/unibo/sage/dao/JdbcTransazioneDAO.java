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
            + "(TipoTransazione, Importo, Data, Descrizione, Email, ID_Categoria, ID_Periodo, ID_Fonte, ID_Ricorrenza) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_TAG_SQL =
            "INSERT INTO SPESA_TAG (ID_Transizione, ID_Tag) VALUES (?, ?)";

    private static final String INSERT_DOCUMENTO_SQL =
            "INSERT INTO DOCUMENTO "
            + "(ID_Transizione, Path_File, Tipo_File, Data_Acquisizione_Documento) "
            + "VALUES (?, ?, ?, ?)";

    private static final String FIND_BY_PERIODO_SQL =
            "SELECT ID_Transizione, TipoTransazione, Importo, Data, Descrizione, Email, "
            + "ID_Categoria, ID_Periodo, ID_Fonte, ID_Ricorrenza "
            + "FROM TRANSIZIONE "
            + "WHERE Email = ? AND Data BETWEEN ? AND ? "
            + "ORDER BY Data DESC, ID_Transizione DESC";

    private static final String FIND_BY_CATEGORIA_SQL =
            "SELECT ID_Transizione, TipoTransazione, Importo, Data, Descrizione, Email, "
            + "ID_Categoria, ID_Periodo, ID_Fonte, ID_Ricorrenza "
            + "FROM TRANSIZIONE "
            + "WHERE Email = ? AND ID_Categoria = ? "
            + "ORDER BY Data DESC, ID_Transizione DESC";

    private static final String FIND_BY_TAG_SQL =
            "SELECT T.ID_Transizione, T.TipoTransazione, T.Importo, T.Data, T.Descrizione, "
            + "T.Email, T.ID_Categoria, T.ID_Periodo, T.ID_Fonte, T.ID_Ricorrenza "
            + "FROM TRANSIZIONE T "
            + "JOIN SPESA_TAG ST ON T.ID_Transizione = ST.ID_Transizione "
            + "WHERE T.Email = ? AND ST.ID_Tag = ? "
            + "ORDER BY T.Data DESC, T.ID_Transizione DESC";

    private static final String FIND_BY_FONTE_SQL =
            "SELECT ID_Transizione, TipoTransazione, Importo, Data, Descrizione, Email, "
            + "ID_Categoria, ID_Periodo, ID_Fonte, ID_Ricorrenza "
            + "FROM TRANSIZIONE "
            + "WHERE Email = ? AND ID_Fonte = ? "
            + "ORDER BY Data DESC, ID_Transizione DESC";

    private static final String FIND_BY_ID_SQL =
            "SELECT ID_Transizione, TipoTransazione, Importo, Data, Descrizione, Email, "
            + "ID_Categoria, ID_Periodo, ID_Fonte, ID_Ricorrenza "
            + "FROM TRANSIZIONE "
            + "WHERE ID_Transizione = ? AND Email = ?";

    private static final String FIND_TAG_IDS_SQL =
            "SELECT ST.ID_Tag "
            + "FROM SPESA_TAG ST "
            + "JOIN TRANSIZIONE T ON ST.ID_Transizione = T.ID_Transizione "
            + "WHERE ST.ID_Transizione = ? AND T.Email = ?";

    private static final String FIND_DATES_BY_RICORRENZA_SQL =
            "SELECT Data FROM TRANSIZIONE "
            + "WHERE Email = ? AND ID_Ricorrenza = ? "
            + "ORDER BY Data";

    private static final String UPDATE_SQL =
            "UPDATE TRANSIZIONE "
            + "SET Importo = ?, Data = ?, Descrizione = ?, ID_Categoria = ?, "
            + "ID_Periodo = ?, ID_Fonte = ? "
            + "WHERE ID_Transizione = ? AND Email = ?";

    private static final String DELETE_TAG_SQL =
            "DELETE FROM SPESA_TAG WHERE ID_Transizione = ?";

    private static final String DELETE_SQL =
            "DELETE FROM TRANSIZIONE WHERE ID_Transizione = ? AND Email = ?";

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
            setNullableLong(statement, 9, transazione.getIdRicorrenza());
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

    @Override
    public List<Transazione> findByCategoria(final String email, final long idCategoria)
            throws SQLException {
        final List<Transazione> transazioni = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CATEGORIA_SQL)) {
            statement.setString(1, email);
            statement.setLong(2, idCategoria);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transazioni.add(mapTransazione(resultSet));
                }
            }
        }
        return transazioni;
    }

    @Override
    public List<Transazione> findByTag(final String email, final long idTag)
            throws SQLException {
        final List<Transazione> transazioni = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_TAG_SQL)) {
            statement.setString(1, email);
            statement.setLong(2, idTag);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transazioni.add(mapTransazione(resultSet));
                }
            }
        }
        return transazioni;
    }

    @Override
    public List<Transazione> findByFonte(final String email, final long idFonte)
            throws SQLException {
        final List<Transazione> transazioni = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_FONTE_SQL)) {
            statement.setString(1, email);
            statement.setLong(2, idFonte);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transazioni.add(mapTransazione(resultSet));
                }
            }
        }
        return transazioni;
    }

    @Override
    public Transazione findById(final long idTransazione, final String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, idTransazione);
            statement.setString(2, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTransazione(resultSet);
                }
                throw new SQLException("Transazione inesistente o non appartenente all'utente");
            }
        }
    }

    @Override
    public List<Long> findTagIds(final long idTransazione, final String email) throws SQLException {
        final List<Long> tagIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_TAG_IDS_SQL)) {
            statement.setLong(1, idTransazione);
            statement.setString(2, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tagIds.add(resultSet.getLong("ID_Tag"));
                }
            }
        }
        return tagIds;
    }

    @Override
    public List<LocalDate> findDateByRicorrenza(final String email,
            final long idRicorrenza) throws SQLException {
        final List<LocalDate> dates = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                FIND_DATES_BY_RICORRENZA_SQL)) {
            statement.setString(1, email);
            statement.setLong(2, idRicorrenza);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    dates.add(resultSet.getDate("Data").toLocalDate());
                }
            }
        }
        return dates;
    }

    @Override
    public void aggiorna(final Transazione transazione) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setBigDecimal(1, transazione.getImporto());
            statement.setDate(2, Date.valueOf(transazione.getData()));
            statement.setString(3, transazione.getDescrizione());
            setNullableLong(statement, 4, transazione.getIdCategoria());
            statement.setLong(5, transazione.getIdPeriodo());
            setNullableLong(statement, 6, transazione.getIdFonte());
            statement.setLong(7, transazione.getId());
            statement.setString(8, transazione.getEmail());
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Transazione inesistente o non appartenente all'utente");
            }
        }
    }

    @Override
    public void eliminaTag(final long idTransazione) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_TAG_SQL)) {
            statement.setLong(1, idTransazione);
            statement.executeUpdate();
        }
    }

    @Override
    public void elimina(final long idTransazione, final String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, idTransazione);
            statement.setString(2, email);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Transazione inesistente o non appartenente all'utente");
            }
        }
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
                readNullableLong(resultSet, "ID_Fonte"),
                readNullableLong(resultSet, "ID_Ricorrenza"));
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
