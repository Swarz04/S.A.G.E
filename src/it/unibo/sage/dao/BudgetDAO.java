package it.unibo.sage.dao;

import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Periodo;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface BudgetDAO {

    void salvaOAggiorna(Budget budget) throws SQLException;

    void aggiornaBudget(Budget budget) throws SQLException;

    void eliminaBudget(String email, long idBudget) throws SQLException;

    boolean esisteBudgetPerAmbitoDiversoDaId(String email, Long idCategoria, long idBudgetEscluso)
            throws SQLException;

    Periodo findPeriodoBudget(String email, long idBudget) throws SQLException;

    BigDecimal calcolaTotaleSpeso(String email, long idPeriodo, Long idCategoria)
            throws SQLException;

    void aggiungiSpesaAiBudget(String email, long idPeriodo, long idCategoria,
            BigDecimal importo) throws SQLException;

    List<Budget> findBudgetUtente(String email) throws SQLException;
}
