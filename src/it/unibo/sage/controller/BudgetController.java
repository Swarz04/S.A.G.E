package it.unibo.sage.controller;

import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Periodo;
import it.unibo.sage.service.BudgetService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class BudgetController {

    private final BudgetService budgetService = new BudgetService();

    public void salvaBudget(final String email, final int mese, final int anno,
            final Long idCategoria, final BigDecimal importoLimite,
            final boolean alertSoglia) throws SQLException {
        budgetService.salvaBudget(email, mese, anno, idCategoria, importoLimite, alertSoglia);
    }


    public void aggiornaBudget(final String email, final long idBudget, final int mese, final int anno,
            final Long idCategoria, final BigDecimal importoLimite,
            final boolean alertSoglia) throws SQLException {
        budgetService.aggiornaBudget(email, idBudget, mese, anno, idCategoria, importoLimite, alertSoglia);
    }

    public void eliminaBudget(final String email, final long idBudget) throws SQLException {
        budgetService.eliminaBudget(email, idBudget);
    }

    public Periodo caricaPeriodoBudget(final String email, final long idBudget) throws SQLException {
        return budgetService.caricaPeriodoBudget(email, idBudget);
    }

    public List<Budget> caricaBudget(final String email) throws SQLException {
        return budgetService.caricaBudget(email);
    }
}
