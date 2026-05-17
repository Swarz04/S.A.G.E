package it.unibo.sage.controller;

import it.unibo.sage.model.Utente;
import it.unibo.sage.service.LoginService;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    private final LoginService loginService = new LoginService();

    public Optional<Utente> login(final String email, final String passwordChiara)
            throws SQLException {
        return loginService.login(email, passwordChiara);
    }
}
