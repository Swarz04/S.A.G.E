package it.unibo.sage.dao;

import it.unibo.sage.model.Budget;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface BudgetDAO {

    void salvaOAggiorna(Budget budget) throws SQLException;

    void aggiungiSpesaAiBudget(String email, long idPeriodo, long idCategoria,
            BigDecimal importo) throws SQLException;

    List<Budget> findBudgetUtente(String email) throws SQLException;
}
