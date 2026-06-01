package it.unibo.sage.dao;

import it.unibo.sage.model.SpesaRicorrente;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcSpesaRicorrenteDAO implements SpesaRicorrenteDAO {

    private static final String FIND_BY_EMAIL_SQL =
            "SELECT ID_Ricorrenza, Importo_Previsto, Frequenza_Giorni, Data_Inizio, "
            + "Data_Prossima_Scadenza, Scadenza, ID_Categoria, Email "
            + "FROM SPESA_RICORRENTE "
            + "WHERE Email = ? "
            + "ORDER BY Data_Prossima_Scadenza, ID_Ricorrenza";

    private static final String FIND_SCADUTE_SQL =
            "SELECT ID_Ricorrenza, Importo_Previsto, Frequenza_Giorni, Data_Inizio, "
            + "Data_Prossima_Scadenza, Scadenza, ID_Categoria, Email "
            + "FROM SPESA_RICORRENTE "
            + "WHERE Email = ? "
            + "AND Data_Prossima_Scadenza <= ? "
            + "AND (Scadenza IS NULL OR Data_Prossima_Scadenza <= Scadenza) "
            + "ORDER BY Data_Prossima_Scadenza, ID_Ricorrenza";

    private static final String INSERT_SQL =
            "INSERT INTO SPESA_RICORRENTE "
            + "(Importo_Previsto, Frequenza_Giorni, Data_Inizio, Data_Prossima_Scadenza, "
            + "Scadenza, ID_Categoria, Email) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_NEXT_DATE_SQL =
            "UPDATE SPESA_RICORRENTE "
            + "SET Data_Prossima_Scadenza = ? "
            + "WHERE ID_Ricorrenza = ? AND Email = ?";

    private static final String DELETE_SQL =
            "DELETE FROM SPESA_RICORRENTE WHERE ID_Ricorrenza = ? AND Email = ?";

    private final Connection connection;

    public JdbcSpesaRicorrenteDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<SpesaRicorrente> findByEmail(final String email) throws SQLException {
        final List<SpesaRicorrente> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL_SQL)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapSpesaRicorrente(resultSet));
                }
            }
        }
        return result;
    }

    @Override
    public List<SpesaRicorrente> findScadute(final String email, final LocalDate finoA)
            throws SQLException {
        final List<SpesaRicorrente> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_SCADUTE_SQL)) {
            statement.setString(1, email);
            statement.setDate(2, Date.valueOf(finoA));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapSpesaRicorrente(resultSet));
                }
            }
        }
        return result;
    }

    @Override
    public long inserisci(final SpesaRicorrente spesaRicorrente) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setBigDecimal(1, spesaRicorrente.getImportoPrevisto());
            statement.setInt(2, spesaRicorrente.getFrequenzaGiorni());
            statement.setDate(3, Date.valueOf(spesaRicorrente.getDataInizio()));
            statement.setDate(4, Date.valueOf(spesaRicorrente.getDataProssimaScadenza()));
            setNullableDate(statement, 5, spesaRicorrente.getScadenza());
            statement.setLong(6, spesaRicorrente.getIdCategoria());
            statement.setString(7, spesaRicorrente.getEmail());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("Inserimento spesa ricorrente senza chiave generata");
            }
        }
    }

    @Override
    public void aggiornaProssimaScadenza(final long idRicorrenza, final String email,
            final LocalDate nuovaScadenza) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_NEXT_DATE_SQL)) {
            statement.setDate(1, Date.valueOf(nuovaScadenza));
            statement.setLong(2, idRicorrenza);
            statement.setString(3, email);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Spesa ricorrente inesistente o non appartenente all'utente");
            }
        }
    }

    @Override
    public void elimina(final long idRicorrenza, final String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, idRicorrenza);
            statement.setString(2, email);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Spesa ricorrente inesistente o non appartenente all'utente");
            }
        }
    }

    private SpesaRicorrente mapSpesaRicorrente(final ResultSet resultSet) throws SQLException {
        return new SpesaRicorrente(
                resultSet.getLong("ID_Ricorrenza"),
                resultSet.getBigDecimal("Importo_Previsto"),
                resultSet.getInt("Frequenza_Giorni"),
                resultSet.getDate("Data_Inizio").toLocalDate(),
                resultSet.getDate("Data_Prossima_Scadenza").toLocalDate(),
                readNullableDate(resultSet, "Scadenza"),
                resultSet.getLong("ID_Categoria"),
                resultSet.getString("Email"));
    }

    private void setNullableDate(final PreparedStatement statement, final int index,
            final LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.DATE);
        } else {
            statement.setDate(index, Date.valueOf(value));
        }
    }

    private LocalDate readNullableDate(final ResultSet resultSet, final String column)
            throws SQLException {
        final Date value = resultSet.getDate(column);
        return value == null ? null : value.toLocalDate();
    }
}
