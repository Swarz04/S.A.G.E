package it.unibo.sage.dao;

import it.unibo.sage.model.Utente;
import java.sql.SQLException;
import java.util.Optional;

public interface UtenteDAO {

    Optional<Utente> login(String email, String passwordChiara) throws SQLException;

    void registraUtente(String email, String passwordChiara, String nome, String cognome)
            throws SQLException;
}
