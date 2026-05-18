package it.unibo.sage.controller;

import it.unibo.sage.dao.JdbcUtenteDAO;
import it.unibo.sage.dao.UtenteDAO;
import it.unibo.sage.model.Ruolo;
import it.unibo.sage.model.Utente;
import it.unibo.sage.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    private static final String OFFLINE_USERNAME = "Uadmin";
    private static final String OFFLINE_PASSWORD = "1234";

    public Optional<Utente> login(final String email, final String passwordChiara)
            throws SQLException {
        if (isOfflineAdmin(email, passwordChiara)) {
            return Optional.of(new Utente(
                    OFFLINE_USERNAME,
                    "",
                    "Utente",
                    "Admin",
                    Ruolo.ADMIN));
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            final UtenteDAO utenteDAO = new JdbcUtenteDAO(connection);
            return utenteDAO.login(email, passwordChiara);
        }
    }

    private boolean isOfflineAdmin(final String username, final String passwordChiara) {
        return OFFLINE_USERNAME.equals(username) && OFFLINE_PASSWORD.equals(passwordChiara);
    }
}
