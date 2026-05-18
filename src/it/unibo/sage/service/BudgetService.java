package it.unibo.sage.service;

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

public class BudgetService {

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
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    public List<Budget> caricaBudget(final String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);
            final List<Budget> budgets = budgetDAO.findBudgetUtente(email);
            for (int i = 0; i < budgets.size(); i++) {
                final Budget budget = budgets.get(i);
                final BigDecimal totaleSpeso =
                        budgetDAO.calcolaTotaleSpeso(email, budget.getIdPeriodo(), budget.getIdCategoria());
                budgets.set(i, new Budget(
                        budget.getId(),
                        budget.getEmail(),
                        budget.getIdPeriodo(),
                        budget.getIdCategoria(),
                        budget.getImportoLimite(),
                        totaleSpeso,
                        budget.isAlertSoglia()));
            }
            return budgets;
        }
    }

    private void rollbackSilenzioso(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (final SQLException ignored) {
            }
        }
    }

    private void closeSilenzioso(final Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (final SQLException ignored) {
            }
        }
    }
}
