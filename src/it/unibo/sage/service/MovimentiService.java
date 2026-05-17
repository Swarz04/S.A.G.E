package it.unibo.sage.service;

import it.unibo.sage.dao.BudgetDAO;
import it.unibo.sage.dao.CategoriaDAO;
import it.unibo.sage.dao.FonteDAO;
import it.unibo.sage.dao.JdbcBudgetDAO;
import it.unibo.sage.dao.JdbcCategoriaDAO;
import it.unibo.sage.dao.JdbcFonteDAO;
import it.unibo.sage.dao.JdbcPeriodoDAO;
import it.unibo.sage.dao.JdbcTagDAO;
import it.unibo.sage.dao.JdbcTransazioneDAO;
import it.unibo.sage.dao.PeriodoDAO;
import it.unibo.sage.dao.TagDAO;
import it.unibo.sage.dao.TransazioneDAO;
import it.unibo.sage.model.Categoria;
import it.unibo.sage.model.Documento;
import it.unibo.sage.model.Fonte;
import it.unibo.sage.model.Tag;
import it.unibo.sage.model.TipoTransazione;
import it.unibo.sage.model.Transazione;
import it.unibo.sage.utils.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MovimentiService {

    private static final String INSERT_CATEGORIA_SQL =
            "INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario) VALUES (?, FALSE, ?)";

    private static final String INSERT_TAG_SQL =
            "INSERT INTO TAG (Nome, is_system, Email_Proprietario) VALUES (?, FALSE, ?)";

    private static final String UPDATE_CATEGORIA_PERSONALE_SQL =
            "UPDATE CATEGORIA SET Nome = ? "
            + "WHERE ID_Categoria = ? AND is_system = FALSE AND Email_Proprietario = ?";

    private static final String DELETE_CATEGORIA_PERSONALE_SQL =
            "DELETE FROM CATEGORIA "
            + "WHERE ID_Categoria = ? AND is_system = FALSE AND Email_Proprietario = ?";

    private static final String UPDATE_TAG_PERSONALE_SQL =
            "UPDATE TAG SET Nome = ? "
            + "WHERE ID_Tag = ? AND is_system = FALSE AND Email_Proprietario = ?";

    private static final String DELETE_TAG_PERSONALE_SQL =
            "DELETE FROM TAG "
            + "WHERE ID_Tag = ? AND is_system = FALSE AND Email_Proprietario = ?";

    public List<Categoria> caricaCategorieDisponibili(final String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final CategoriaDAO categoriaDAO = new JdbcCategoriaDAO(connection);
            return categoriaDAO.findDisponibiliPerUtente(email);
        }
    }

    public List<Tag> caricaTagDisponibili(final String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final TagDAO tagDAO = new JdbcTagDAO(connection);
            return tagDAO.findDisponibiliPerUtente(email);
        }
    }

    public List<Fonte> caricaFontiDisponibili(final String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final FonteDAO fonteDAO = new JdbcFonteDAO(connection);
            return fonteDAO.findDisponibiliPerUtente(email);
        }
    }

    public void aggiungiCategoriaPersonale(final String email, final String nome) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_CATEGORIA_SQL)) {
            statement.setString(1, nome);
            statement.setString(2, email);
            statement.executeUpdate();
        }
    }

    public void aggiungiTagPersonale(final String email, final String nome) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_TAG_SQL)) {
            statement.setString(1, nome);
            statement.setString(2, email);
            statement.executeUpdate();
        }
    }

    public void rinominaCategoriaPersonale(final String email, final long idCategoria,
            final String nome) throws SQLException {
        executePersonalUpdate(UPDATE_CATEGORIA_PERSONALE_SQL, nome, idCategoria, email);
    }

    public void eliminaCategoriaPersonale(final String email, final long idCategoria)
            throws SQLException {
        executePersonalDelete(DELETE_CATEGORIA_PERSONALE_SQL, idCategoria, email);
    }

    public void rinominaTagPersonale(final String email, final long idTag,
            final String nome) throws SQLException {
        executePersonalUpdate(UPDATE_TAG_PERSONALE_SQL, nome, idTag, email);
    }

    public void eliminaTagPersonale(final String email, final long idTag) throws SQLException {
        executePersonalDelete(DELETE_TAG_PERSONALE_SQL, idTag, email);
    }

    private void executePersonalUpdate(final String sql, final String nome, final long id,
            final String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setLong(2, id);
            statement.setString(3, email);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Elemento non personale o inesistente");
            }
        }
    }

    private void executePersonalDelete(final String sql, final long id, final String email)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setString(2, email);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Elemento non personale o inesistente");
            }
        }
    }

    public List<Transazione> caricaTransazioni(final String email, final LocalDate dal,
            final LocalDate al) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            return transazioneDAO.findByPeriodo(email, dal, al);
        }
    }

    public long registraSpesa(final String email, final BigDecimal importo,
            final LocalDate data, final String descrizione, final long idCategoria,
            final List<Long> idTag, final String documentoPath) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            final PeriodoDAO periodoDAO = new JdbcPeriodoDAO(connection);
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);

            final long idPeriodo = periodoDAO.trovaOCreaPeriodo(data.getMonthValue(), data.getYear());
            final Transazione spesa = new Transazione(
                    0,
                    TipoTransazione.SPESA,
                    importo,
                    data,
                    descrizione,
                    email,
                    idCategoria,
                    idPeriodo,
                    null);

            final long idTransazione = transazioneDAO.inserisci(spesa);
            for (final Long tag : idTag) {
                transazioneDAO.associaTag(idTransazione, tag.longValue());
            }
            if (documentoPath != null && !documentoPath.isEmpty()) {
                transazioneDAO.inserisciDocumento(new Documento(
                        0,
                        idTransazione,
                        documentoPath,
                        riconosciTipoFile(documentoPath),
                        LocalDate.now()));
            }

            budgetDAO.aggiungiSpesaAiBudget(email, idPeriodo, idCategoria, importo);
            connection.commit();
            return idTransazione;
        } catch (final SQLException ex) {
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    public long registraEntrata(final String email, final BigDecimal importo,
            final LocalDate data, final String descrizione, final long idFonte) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            final PeriodoDAO periodoDAO = new JdbcPeriodoDAO(connection);
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            final long idPeriodo = periodoDAO.trovaOCreaPeriodo(data.getMonthValue(), data.getYear());

            final Transazione entrata = new Transazione(
                    0,
                    TipoTransazione.ENTRATA,
                    importo,
                    data,
                    descrizione,
                    email,
                    null,
                    idPeriodo,
                    idFonte);

            final long idTransazione = transazioneDAO.inserisci(entrata);
            connection.commit();
            return idTransazione;
        } catch (final SQLException ex) {
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    private String riconosciTipoFile(final String path) {
        final int dotIndex = path.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == path.length() - 1) {
            return "FILE";
        }
        return path.substring(dotIndex + 1).toUpperCase();
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
