package it.unibo.sage.controller;

import it.unibo.sage.dao.BudgetDAO;
import it.unibo.sage.dao.JdbcBudgetDAO;
import it.unibo.sage.dao.JdbcPeriodoDAO;
import it.unibo.sage.dao.PeriodoDAO;
import it.unibo.sage.model.Budget;
import it.unibo.sage.utils.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BudgetController {

    public void salvaBudget(final String email, final int mese, final int anno,
            final Long idCategoria, final BigDecimal importoLimite,
            final boolean alertSoglia) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            final PeriodoDAO periodoDAO = new JdbcPeriodoDAO(connection);
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);
            final long idPeriodo = periodoDAO.trovaOCreaPeriodo(mese, anno);
            final BigDecimal totaleSpesoAttuale =
                    budgetDAO.calcolaTotaleSpeso(email, idPeriodo, idCategoria);

            budgetDAO.salvaOAggiorna(new Budget(
                    0,
                    email,
                    idPeriodo,
                    idCategoria,
                    importoLimite,
                    totaleSpesoAttuale,
                    alertSoglia));
            connection.commit();
        } catch (final SQLException ex) {
            if (connection != null) {
                connection.rollback();
            }
            throw ex;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public List<Budget> caricaBudget(final String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);
            return budgetDAO.findBudgetUtente(email);
        }
    }
}
