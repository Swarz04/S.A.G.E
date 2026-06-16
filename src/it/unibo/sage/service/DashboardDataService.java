package it.unibo.sage.service;

import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.model.Tag;
import it.unibo.sage.model.Transazione;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardDataService {

    private static final LocalDate START_DATE = LocalDate.of(1900, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2100, 12, 31);

    private final MovimentiService movimentiService;
    private final BudgetService budgetService;
    private final SpeseRicorrentiService speseRicorrentiService;

    public DashboardDataService() {
        this(new MovimentiService(), new BudgetService(), new SpeseRicorrentiService());
    }

    DashboardDataService(final MovimentiService movimentiService,
            final BudgetService budgetService,
            final SpeseRicorrentiService speseRicorrentiService) {
        this.movimentiService = movimentiService;
        this.budgetService = budgetService;
        this.speseRicorrentiService = speseRicorrentiService;
    }

    public DashboardData loadForUser(final String email) throws SQLException {
        final List<Transazione> transazioni =
                movimentiService.caricaTransazioni(email, START_DATE, END_DATE);
        final List<Budget> budget = budgetService.caricaBudget(email);
        final List<SpesaRicorrente> ricorrenze =
                speseRicorrentiService.caricaRicorrenze(email);
        final List<Categoria> categorie =
                movimentiService.caricaCategorieDisponibili(email);
        final List<Tag> tag = movimentiService.caricaTagDisponibili(email);
        final List<Fonte> fonti = movimentiService.caricaFontiDisponibili(email);

        return new DashboardData(transazioni, budget, ricorrenze, categorie,
                tag, fonti, mapCategoryNames(categorie));
    }

    public List<Transazione> loadTransactionsForClassification(
            final String email, final String type, final long id) throws SQLException {
        if ("Categoria".equals(type)) {
            return movimentiService.caricaTransazioniPerCategoria(email, id);
        }
        if ("Fonte".equals(type)) {
            return movimentiService.caricaTransazioniPerFonte(email, id);
        }
        return movimentiService.caricaTransazioniPerTag(email, id);
    }

<<<<<<< HEAD
=======
    public List<Transazione> loadTransactionsForRecurringExpense(
            final String email, final long idRicorrenza) throws SQLException {
        return movimentiService.caricaTransazioniPerRicorrenza(email, idRicorrenza);
    }

>>>>>>> 3351d66 (aggiornamento interfaccia)
    public List<Long> loadTransactionTagIds(final String email, final long idTransazione)
            throws SQLException {
        return movimentiService.caricaTagTransazione(email, idTransazione);
    }

    public int generateDueRecurringExpenses(final String email, final LocalDate today)
            throws SQLException {
        return speseRicorrentiService.generaSpeseScadute(email, today);
    }

    private Map<Long, String> mapCategoryNames(final List<Categoria> categorie) {
        final Map<Long, String> names = new HashMap<>();
        for (final Categoria categoria : categorie) {
            names.put(categoria.getId(), categoria.getNome());
        }
        return names;
    }
}
