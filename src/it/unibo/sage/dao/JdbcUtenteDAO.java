package it.unibo.sage.dao;

import it.unibo.sage.model.Ruolo;
import it.unibo.sage.model.Utente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcUtenteDAO implements UtenteDAO {

    private static final String LOGIN_SQL =
            "SELECT Email, Password, Nome, Cognome, Ruolo "
            + "FROM UTENTE "
            + "WHERE Email = ? AND Password = SHA2(?, 512)";

    private static final String INSERT_UTENTE_SQL =
            "INSERT INTO UTENTE (Email, Password, Nome, Cognome, Ruolo) "
            + "VALUES (?, SHA2(?, 512), ?, ?, 'UTENTE')";

    private final Connection connection;

    public JdbcUtenteDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Utente> login(final String email, final String passwordChiara)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOGIN_SQL)) {
            statement.setString(1, email);
            statement.setString(2, passwordChiara);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapUtente(resultSet));
            }
        }
    }

    @Override
    public void registraUtente(final String email, final String passwordChiara,
            final String nome, final String cognome) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_UTENTE_SQL)) {
            statement.setString(1, email);
            statement.setString(2, passwordChiara);
            statement.setString(3, nome);
            statement.setString(4, cognome);
            statement.executeUpdate();
        }
    }

    private Utente mapUtente(final ResultSet resultSet) throws SQLException {
        return new Utente(
                resultSet.getString("Email"),
                resultSet.getString("Password"),
                resultSet.getString("Nome"),
                resultSet.getString("Cognome"),
                Ruolo.fromDb(resultSet.getString("Ruolo")));
    }
}
