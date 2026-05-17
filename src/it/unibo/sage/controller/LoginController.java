package it.unibo.sage.controller;

import it.unibo.sage.dao.JdbcUtenteDAO;
import it.unibo.sage.dao.UtenteDAO;
import it.unibo.sage.model.Utente;
import it.unibo.sage.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    public Optional<Utente> login(final String email, final String passwordChiara)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final UtenteDAO utenteDAO = new JdbcUtenteDAO(connection);
            return utenteDAO.login(email, passwordChiara);
        }
    }
}
