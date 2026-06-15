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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return aggiungiRicorrenza(email, "Spesa ricorrente", importo, frequenzaGiorni,
                dataInizio, dataProssimaScadenza, scadenza, idCategoria);
    }

    public long aggiungiRicorrenza(final String email, final String nome,
            final BigDecimal importo, final int frequenzaGiorni,
            final LocalDate dataInizio, final LocalDate dataProssimaScadenza,
            final LocalDate scadenza, final long idCategoria) throws SQLException {
        final String nomePulito = validaRicorrenza(nome, importo, frequenzaGiorni,
                dataInizio, dataProssimaScadenza, scadenza);

        try (Connection connection = DatabaseConnection.getConnection()) {
            final SpesaRicorrenteDAO dao = new JdbcSpesaRicorrenteDAO(connection);
            return dao.inserisci(new SpesaRicorrente(
                    0,
                    nomePulito,
                    importo,
                    frequenzaGiorni,
                    dataInizio,
                    dataProssimaScadenza,
                    scadenza,
                    idCategoria,
                    email));
        }
    }

    /**
     * Salva il modello ricorrente e registra tutte le rate maturate dalla data
     * iniziale fino alla data corrente. Le transazioni sono collegate tramite
     * ID_Ricorrenza e non vengono duplicate se la procedura viene rieseguita.
     */
    public long aggiungiRicorrenzaERegistraPrimaSpesa(final String email, final String nome,
            final BigDecimal importo, final int frequenzaGiorni,
            final LocalDate dataInizio, final LocalDate dataProssimaScadenza,
            final LocalDate scadenza, final long idCategoria,
            final LocalDate dataRegistrazione) throws SQLException {
        final String nomePulito = validaRicorrenza(nome, importo, frequenzaGiorni,
                dataInizio, dataProssimaScadenza, scadenza);
        if (dataRegistrazione == null) {
            throw new IllegalArgumentException("La data di registrazione e' obbligatoria.");
        }

        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            final SpesaRicorrenteDAO ricorrenzaDAO = new JdbcSpesaRicorrenteDAO(connection);
            final PeriodoDAO periodoDAO = new JdbcPeriodoDAO(connection);
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);

            // La data iniziale rappresenta la prima rata della ricorrenza.
            final SpesaRicorrente modello = new SpesaRicorrente(
                    0,
                    nomePulito,
                    importo,
                    frequenzaGiorni,
                    dataInizio,
                    dataInizio,
                    scadenza,
                    idCategoria,
                    email);
            final long idRicorrenza = ricorrenzaDAO.inserisci(modello);

            final SpesaRicorrente modelloPersistito = new SpesaRicorrente(
                    idRicorrenza,
                    nomePulito,
                    importo,
                    frequenzaGiorni,
                    dataInizio,
                    dataInizio,
                    scadenza,
                    idCategoria,
                    email);

            generaOccorrenzeMancanti(
                    modelloPersistito,
                    dataRegistrazione,
                    new HashSet<>(),
                    periodoDAO,
                    transazioneDAO,
                    budgetDAO);

            final LocalDate nuovaProssimaScadenza = calcolaPrimaScadenzaSuccessiva(
                    dataInizio, frequenzaGiorni, dataRegistrazione);
            ricorrenzaDAO.aggiornaProssimaScadenza(
                    idRicorrenza, email, nuovaProssimaScadenza);

            connection.commit();
            return idRicorrenza;
        } catch (final SQLException | RuntimeException ex) {
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    public void eliminaRicorrenza(final String email, final long idRicorrenza) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final SpesaRicorrenteDAO dao = new JdbcSpesaRicorrenteDAO(connection);
            dao.elimina(idRicorrenza, email);
        }
    }

    /**
     * Genera tutte le occorrenze mancanti dalla data iniziale fino a finoA.
     * Vengono analizzate anche le ricorrenze con prossima scadenza futura, così
     * una vecchia ricorrenza salvata in modo incompleto viene riparata automaticamente.
     */
    public int generaSpeseScadute(final String email, final LocalDate finoA) throws SQLException {
        if (finoA == null) {
            throw new IllegalArgumentException("La data finale di generazione e' obbligatoria.");
        }

        Connection connection = null;
        int generate = 0;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            final SpesaRicorrenteDAO ricorrenzaDAO = new JdbcSpesaRicorrenteDAO(connection);
            final PeriodoDAO periodoDAO = new JdbcPeriodoDAO(connection);
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);

            for (final SpesaRicorrente ricorrenza : ricorrenzaDAO.findByEmail(email)) {
                final Set<LocalDate> dateGiaRegistrate = new HashSet<>(
                        transazioneDAO.findDateByRicorrenza(email, ricorrenza.getId()));

                generate += generaOccorrenzeMancanti(
                        ricorrenza,
                        finoA,
                        dateGiaRegistrate,
                        periodoDAO,
                        transazioneDAO,
                        budgetDAO);

                final LocalDate nuovaScadenza = calcolaPrimaScadenzaSuccessiva(
                        ricorrenza.getDataInizio(),
                        ricorrenza.getFrequenzaGiorni(),
                        finoA);
                if (!nuovaScadenza.equals(ricorrenza.getDataProssimaScadenza())) {
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

    private int generaOccorrenzeMancanti(final SpesaRicorrente ricorrenza,
            final LocalDate finoA, final Set<LocalDate> dateGiaRegistrate,
            final PeriodoDAO periodoDAO, final TransazioneDAO transazioneDAO,
            final BudgetDAO budgetDAO) throws SQLException {
        int generate = 0;
        for (final LocalDate data : calcolaScadenzeDaDataInizio(ricorrenza, finoA)) {
            if (dateGiaRegistrate.contains(data)) {
                continue;
            }

            final long idPeriodo = periodoDAO.trovaOCreaPeriodo(
                    data.getMonthValue(), data.getYear());
            final Transazione spesa = new Transazione(
                    0,
                    TipoTransazione.SPESA,
                    ricorrenza.getImportoPrevisto(),
                    data,
                    ricorrenza.getNome(),
                    ricorrenza.getEmail(),
                    ricorrenza.getIdCategoria(),
                    idPeriodo,
                    null,
                    ricorrenza.getId());
            transazioneDAO.inserisci(spesa);
            budgetDAO.aggiungiSpesaAiBudget(
                    ricorrenza.getEmail(),
                    idPeriodo,
                    ricorrenza.getIdCategoria(),
                    ricorrenza.getImportoPrevisto());
            dateGiaRegistrate.add(data);
            generate++;
        }
        return generate;
    }

    public LocalDate calcolaProssimaScadenzaDopoRegistrazione(
            final LocalDate dataProssimaScadenza, final int frequenzaGiorni,
            final LocalDate dataRegistrazione) {
        return calcolaPrimaScadenzaSuccessiva(
                dataProssimaScadenza, frequenzaGiorni, dataRegistrazione);
    }

    public LocalDate calcolaPrimaScadenzaSuccessiva(final LocalDate dataInizio,
            final int frequenzaGiorni, final LocalDate dopoData) {
        int indice = 0;
        LocalDate prossima = calcolaDataOccorrenza(dataInizio, frequenzaGiorni, indice);
        while (!prossima.isAfter(dopoData)) {
            indice++;
            prossima = calcolaDataOccorrenza(dataInizio, frequenzaGiorni, indice);
        }
        return prossima;
    }

    private String validaRicorrenza(final String nome, final BigDecimal importo,
            final int frequenzaGiorni, final LocalDate dataInizio,
            final LocalDate dataProssimaScadenza, final LocalDate scadenza) {
        final String nomePulito = nome == null ? "" : nome.trim();
        if (nomePulito.isEmpty()) {
            throw new IllegalArgumentException("Il nome della spesa ricorrente e' obbligatorio.");
        }
        if (importo == null || importo.signum() <= 0) {
            throw new IllegalArgumentException("L'importo deve essere positivo.");
        }
        if (frequenzaGiorni <= 0) {
            throw new IllegalArgumentException("La frequenza deve essere positiva.");
        }
        if (dataInizio == null || dataProssimaScadenza == null) {
            throw new IllegalArgumentException("Data iniziale e prossima scadenza sono obbligatorie.");
        }
        if (dataProssimaScadenza.isBefore(dataInizio)) {
            throw new IllegalArgumentException("La prossima scadenza non puo' precedere la data iniziale.");
        }
        if (scadenza != null && scadenza.isBefore(dataProssimaScadenza)) {
            throw new IllegalArgumentException("La scadenza finale non puo' precedere la prossima scadenza.");
        }
        return nomePulito;
    }

    /**
     * Mantiene il vecchio comportamento basato sulla prossima scadenza memorizzata.
     */
    public List<LocalDate> calcolaScadenzeDaGenerare(final SpesaRicorrente modello,
            final LocalDate finoA) {
        return calcolaScadenze(modello.getDataProssimaScadenza(),
                modello.getFrequenzaGiorni(), modello.getScadenza(), finoA);
    }

    /**
     * Calcola tutte le rate teoriche della ricorrenza a partire da Data_Inizio.
     */
    public List<LocalDate> calcolaScadenzeDaDataInizio(final SpesaRicorrente modello,
            final LocalDate finoA) {
        return calcolaScadenze(modello.getDataInizio(),
                modello.getFrequenzaGiorni(), modello.getScadenza(), finoA);
    }

    private List<LocalDate> calcolaScadenze(final LocalDate primaData,
            final int frequenzaGiorni, final LocalDate scadenza,
            final LocalDate finoA) {
        final List<LocalDate> scadenze = new ArrayList<>();
        int indice = 0;
        LocalDate corrente = calcolaDataOccorrenza(primaData, frequenzaGiorni, indice);

        while (!corrente.isAfter(finoA)
                && (scadenza == null || !corrente.isAfter(scadenza))) {
            scadenze.add(corrente);
            indice++;
            corrente = calcolaDataOccorrenza(primaData, frequenzaGiorni, indice);
        }

        return scadenze;
    }

    /**
     * Nell'interfaccia 30 giorni rappresenta una ricorrenza mensile e 365 una
     * ricorrenza annuale. Il calcolo parte sempre dalla data iniziale, così il
     * giorno originario non slitta dopo febbraio o dopo mesi più corti.
     */
    private LocalDate calcolaDataOccorrenza(final LocalDate dataInizio,
            final int frequenzaGiorni, final int indice) {
        if (frequenzaGiorni == 30) {
            return dataInizio.plusMonths(indice);
        }
        if (frequenzaGiorni == 365) {
            return dataInizio.plusYears(indice);
        }
        return dataInizio.plusDays((long) frequenzaGiorni * indice);
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
