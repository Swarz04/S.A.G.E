package it.unibo.sage.service;

import it.unibo.sage.dao.BudgetDAO;
import it.unibo.sage.dao.JdbcBudgetDAO;
import it.unibo.sage.dao.JdbcPeriodoDAO;
import it.unibo.sage.dao.JdbcSpesaRicorrenteDAO;
import it.unibo.sage.dao.JdbcTransazioneDAO;
import it.unibo.sage.dao.PeriodoDAO;
import it.unibo.sage.dao.SpesaRicorrenteDAO;
import it.unibo.sage.dao.TransazioneDAO;
import it.unibo.sage.model.SpesaRicorrente;
import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import it.unibo.sage.utils.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SpeseRicorrentiService {

    public List<SpesaRicorrente> caricaRicorrenze(final String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final SpesaRicorrenteDAO dao = new JdbcSpesaRicorrenteDAO(connection);
            return dao.findByEmail(email);
        }
    }

    public long aggiungiRicorrenza(final String email, final BigDecimal importo,
            final int frequenzaGiorni, final LocalDate dataInizio,
            final LocalDate dataProssimaScadenza, final LocalDate scadenza,
            final long idCategoria) throws SQLException {
        if (frequenzaGiorni <= 0) {
            throw new IllegalArgumentException("La frequenza deve essere positiva.");
        }
        if (scadenza != null && scadenza.isBefore(dataProssimaScadenza)) {
            throw new IllegalArgumentException("La scadenza finale non puo' precedere la prossima scadenza.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            final SpesaRicorrenteDAO dao = new JdbcSpesaRicorrenteDAO(connection);
            return dao.inserisci(new SpesaRicorrente(
                    0,
                    importo,
                    frequenzaGiorni,
                    dataInizio,
                    dataProssimaScadenza,
                    scadenza,
                    idCategoria,
                    email));
        }
    }

    public void eliminaRicorrenza(final String email, final long idRicorrenza) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final SpesaRicorrenteDAO dao = new JdbcSpesaRicorrenteDAO(connection);
            dao.elimina(idRicorrenza, email);
        }
    }

    public int generaSpeseScadute(final String email, final LocalDate finoA) throws SQLException {
        Connection connection = null;
        int generate = 0;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            final SpesaRicorrenteDAO ricorrenzaDAO = new JdbcSpesaRicorrenteDAO(connection);
            final PeriodoDAO periodoDAO = new JdbcPeriodoDAO(connection);
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);

            for (final SpesaRicorrente ricorrenza : ricorrenzaDAO.findScadute(email, finoA)) {
                final List<LocalDate> scadenze = calcolaScadenzeDaGenerare(ricorrenza, finoA);
                for (final LocalDate data : scadenze) {
                    final long idPeriodo = periodoDAO.trovaOCreaPeriodo(
                            data.getMonthValue(), data.getYear());
                    final Transazione spesa = new Transazione(
                            0,
                            TipoTransazione.SPESA,
                            ricorrenza.getImportoPrevisto(),
                            data,
                            "Spesa ricorrente #" + ricorrenza.getId(),
                            email,
                            ricorrenza.getIdCategoria(),
                            idPeriodo,
                            null);
                    transazioneDAO.inserisci(spesa);
                    budgetDAO.aggiungiSpesaAiBudget(
                            email,
                            idPeriodo,
                            ricorrenza.getIdCategoria(),
                            ricorrenza.getImportoPrevisto());
                    generate++;
                }

                if (!scadenze.isEmpty()) {
                    final LocalDate nuovaScadenza = scadenze.get(scadenze.size() - 1)
                            .plusDays(ricorrenza.getFrequenzaGiorni());
                    ricorrenzaDAO.aggiornaProssimaScadenza(
                            ricorrenza.getId(), email, nuovaScadenza);
                }
            }

            connection.commit();
            return generate;
        } catch (final SQLException | RuntimeException ex) {
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    public List<LocalDate> calcolaScadenzeDaGenerare(final SpesaRicorrente modello,
            final LocalDate finoA) {
        final List<LocalDate> scadenze = new ArrayList<>();
        LocalDate corrente = modello.getDataProssimaScadenza();

        while (!corrente.isAfter(finoA)
                && (modello.getScadenza() == null || !corrente.isAfter(modello.getScadenza()))) {
            scadenze.add(corrente);
            corrente = corrente.plusDays(modello.getFrequenzaGiorni());
        }

        return scadenze;
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
