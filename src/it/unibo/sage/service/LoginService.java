package it.unibo.sage.service;

import it.unibo.sage.dao.JdbcUtenteDAO;
import it.unibo.sage.dao.UtenteDAO;
import it.unibo.sage.model.Utente;
import it.unibo.sage.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class LoginService {

    public Optional<Utente> login(final String email, final String passwordChiara)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final UtenteDAO utenteDAO = new JdbcUtenteDAO(connection);
            return utenteDAO.login(email, passwordChiara);
        }
    }

    public void registraUtente(final String email, final String passwordChiara,
            final String nome, final String cognome) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final UtenteDAO utenteDAO = new JdbcUtenteDAO(connection);
            utenteDAO.registraUtente(email, passwordChiara, nome, cognome);
        }
    }
}
