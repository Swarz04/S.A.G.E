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
import it.unibo.sage.utils.IconStorage;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class MovimentiService {

    private static final String INSERT_CATEGORIA_SQL =
            "INSERT INTO CATEGORIA (Nome, Icona, is_system, Email_Proprietario) "
            + "VALUES (?, ?, FALSE, ?)";

    private static final String INSERT_TAG_SQL =
            "INSERT INTO TAG (Nome, Icona, is_system, Email_Proprietario) "
            + "VALUES (?, ?, FALSE, ?)";

    private static final String INSERT_FONTE_SQL =
            "INSERT INTO FONTE (Nome, Icona, is_system, Email_Proprietario) "
            + "VALUES (?, ?, FALSE, ?)";

    private static final String UPDATE_CATEGORIA_PERSONALE_SQL =
            "UPDATE CATEGORIA SET Nome = ? "
            + "WHERE ID_Categoria = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String UPDATE_CATEGORIA_COMPLETA_SQL =
            "UPDATE CATEGORIA SET Nome = ?, Icona = ? "
            + "WHERE ID_Categoria = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String DELETE_CATEGORIA_PERSONALE_SQL =
            "DELETE FROM CATEGORIA "
            + "WHERE ID_Categoria = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String UPDATE_TAG_PERSONALE_SQL =
            "UPDATE TAG SET Nome = ? "
            + "WHERE ID_Tag = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String UPDATE_TAG_COMPLETO_SQL =
            "UPDATE TAG SET Nome = ?, Icona = ? "
            + "WHERE ID_Tag = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String DELETE_TAG_PERSONALE_SQL =
            "DELETE FROM TAG "
            + "WHERE ID_Tag = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String UPDATE_FONTE_PERSONALE_SQL =
            "UPDATE FONTE SET Nome = ? "
            + "WHERE ID_Fonte = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String UPDATE_FONTE_COMPLETA_SQL =
            "UPDATE FONTE SET Nome = ?, Icona = ? "
            + "WHERE ID_Fonte = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String DELETE_FONTE_PERSONALE_SQL =
            "DELETE FROM FONTE "
            + "WHERE ID_Fonte = ? AND (is_system = TRUE OR Email_Proprietario = ?)";

    private static final String EXISTS_CATEGORIA_DISPONIBILE_SQL =
            "SELECT 1 FROM CATEGORIA "
            + "WHERE LOWER(TRIM(Nome)) = LOWER(TRIM(?)) "
            + "AND (is_system = TRUE OR Email_Proprietario = ?) "
            + "AND ID_Categoria <> ? LIMIT 1";

    private static final String EXISTS_TAG_DISPONIBILE_SQL =
            "SELECT 1 FROM TAG "
            + "WHERE LOWER(TRIM(Nome)) = LOWER(TRIM(?)) "
            + "AND (is_system = TRUE OR Email_Proprietario = ?) "
            + "AND ID_Tag <> ? LIMIT 1";

    private static final String EXISTS_FONTE_DISPONIBILE_SQL =
            "SELECT 1 FROM FONTE "
            + "WHERE LOWER(TRIM(Nome)) = LOWER(TRIM(?)) "
            + "AND (is_system = TRUE OR Email_Proprietario = ?) "
            + "AND ID_Fonte <> ? LIMIT 1";

    private static final Set<String> CATEGORY_ICONS = Set.of(
            "generic_category.png", "house.png", "food.png", "transport.png",
            "health.png", "study.png", "work.png", "savings.png", "shopping.png",
            "leisure.png", "bill.png", "gym.png", "travel.png", "gift.png");

    private static final Set<String> TAG_ICONS = Set.of(
            "generic_tag.png", "urgent.png", "study.png", "gym.png", "work.png",
            "family.png", "travel.png", "gift.png", "savings.png", "shopping.png",
            "leisure.png");

    private static final Set<String> SOURCE_ICONS = Set.of(
            "generic_source.png", "salary.png", "scholarship.png", "gift.png",
            "refund.png", "tutoring.png", "work.png", "family.png", "savings.png");

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
        aggiungiCategoriaPersonale(email, nome, "generic_category.png");
    }

    public void aggiungiCategoriaPersonale(final String email, final String nome,
            final String icona) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        final String iconaValida = validaIcona(icona, CATEGORY_ICONS, "generic_category.png");
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_CATEGORIA_DISPONIBILE_SQL,
                    nomePulito, email, 0, "categoria");
            try (PreparedStatement statement = connection.prepareStatement(INSERT_CATEGORIA_SQL)) {
                statement.setString(1, nomePulito);
                statement.setString(2, iconaValida);
                statement.setString(3, email);
                statement.executeUpdate();
            }
        }
    }

    public void aggiungiTagPersonale(final String email, final String nome) throws SQLException {
        aggiungiTagPersonale(email, nome, "generic_tag.png");
    }

    public void aggiungiTagPersonale(final String email, final String nome,
            final String icona) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        final String iconaValida = validaIcona(icona, TAG_ICONS, "generic_tag.png");
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_TAG_DISPONIBILE_SQL,
                    nomePulito, email, 0, "tag");
            try (PreparedStatement statement = connection.prepareStatement(INSERT_TAG_SQL)) {
                statement.setString(1, nomePulito);
                statement.setString(2, iconaValida);
                statement.setString(3, email);
                statement.executeUpdate();
            }
        }
    }

    public void aggiungiFontePersonale(final String email, final String nome) throws SQLException {
        aggiungiFontePersonale(email, nome, "generic_source.png");
    }

    public void aggiungiFontePersonale(final String email, final String nome,
            final String icona) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        final String iconaValida = validaIcona(icona, SOURCE_ICONS, "generic_source.png");
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_FONTE_DISPONIBILE_SQL,
                    nomePulito, email, 0, "fonte");
            try (PreparedStatement statement = connection.prepareStatement(INSERT_FONTE_SQL)) {
                statement.setString(1, nomePulito);
                statement.setString(2, iconaValida);
                statement.setString(3, email);
                statement.executeUpdate();
            }
        }
    }

    public void rinominaCategoriaPersonale(final String email, final long idCategoria,
            final String nome) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_CATEGORIA_DISPONIBILE_SQL,
                    nomePulito, email, idCategoria, "categoria");
            executePersonalUpdate(connection, UPDATE_CATEGORIA_PERSONALE_SQL,
                    nomePulito, idCategoria, email);
        }
    }

    public void modificaCategoriaPersonale(final String email, final long idCategoria,
            final String nome, final String icona) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        final String iconaValida = validaIcona(icona, CATEGORY_ICONS, "generic_category.png");
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_CATEGORIA_DISPONIBILE_SQL,
                    nomePulito, email, idCategoria, "categoria");
            executePersonalUpdateWithIcon(connection, UPDATE_CATEGORIA_COMPLETA_SQL,
                    nomePulito, iconaValida, idCategoria, email);
        }
    }

    public void eliminaCategoriaPersonale(final String email, final long idCategoria)
            throws SQLException {
        executePersonalDelete(DELETE_CATEGORIA_PERSONALE_SQL, idCategoria, email);
    }

    public void rinominaTagPersonale(final String email, final long idTag,
            final String nome) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_TAG_DISPONIBILE_SQL,
                    nomePulito, email, idTag, "tag");
            executePersonalUpdate(connection, UPDATE_TAG_PERSONALE_SQL,
                    nomePulito, idTag, email);
        }
    }

    public void modificaTagPersonale(final String email, final long idTag,
            final String nome, final String icona) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        final String iconaValida = validaIcona(icona, TAG_ICONS, "generic_tag.png");
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_TAG_DISPONIBILE_SQL,
                    nomePulito, email, idTag, "tag");
            executePersonalUpdateWithIcon(connection, UPDATE_TAG_COMPLETO_SQL,
                    nomePulito, iconaValida, idTag, email);
        }
    }

    public void eliminaTagPersonale(final String email, final long idTag) throws SQLException {
        executePersonalDelete(DELETE_TAG_PERSONALE_SQL, idTag, email);
    }

    public void rinominaFontePersonale(final String email, final long idFonte,
            final String nome) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_FONTE_DISPONIBILE_SQL,
                    nomePulito, email, idFonte, "fonte");
            executePersonalUpdate(connection, UPDATE_FONTE_PERSONALE_SQL,
                    nomePulito, idFonte, email);
        }
    }

    public void modificaFontePersonale(final String email, final long idFonte,
            final String nome, final String icona) throws SQLException {
        final String nomePulito = validaNomeClassificazione(nome);
        final String iconaValida = validaIcona(icona, SOURCE_ICONS, "generic_source.png");
        try (Connection connection = DatabaseConnection.getConnection()) {
            verificaNomeDisponibile(connection, EXISTS_FONTE_DISPONIBILE_SQL,
                    nomePulito, email, idFonte, "fonte");
            executePersonalUpdateWithIcon(connection, UPDATE_FONTE_COMPLETA_SQL,
                    nomePulito, iconaValida, idFonte, email);
        }
    }

    public void eliminaFontePersonale(final String email, final long idFonte) throws SQLException {
        executePersonalDelete(DELETE_FONTE_PERSONALE_SQL, idFonte, email);
    }

    private void executePersonalUpdate(final String sql, final String nome, final long id,
            final String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            executePersonalUpdate(connection, sql, nome, id, email);
        }
    }

    private void executePersonalUpdate(final Connection connection, final String sql,
            final String nome, final long id, final String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setLong(2, id);
            statement.setString(3, email);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Elemento inesistente o non disponibile");
            }
        }
    }

    private void executePersonalUpdateWithIcon(final Connection connection, final String sql,
            final String nome, final String icona, final long id, final String email)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, icona);
            statement.setLong(3, id);
            statement.setString(4, email);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Elemento inesistente o non disponibile");
            }
        }
    }

    private void verificaNomeDisponibile(final Connection connection, final String sql,
            final String nome, final String email, final long idEscluso,
            final String tipo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, email);
            statement.setLong(3, idEscluso);
            try (java.sql.ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    throw new SQLException("Esiste gia' un " + tipo + " disponibile con questo nome");
                }
            }
        }
    }

    private String validaNomeClassificazione(final String nome) {
        final String nomePulito = nome == null ? "" : nome.trim();
        if (nomePulito.isEmpty()) {
            throw new IllegalArgumentException("Il nome e' obbligatorio");
        }
        return nomePulito;
    }

    private String validaIcona(final String icona, final Set<String> iconeAmmesse,
            final String iconaDefault) {
        if (icona == null || icona.isBlank()) {
            return iconaDefault;
        }
        if (!iconeAmmesse.contains(icona) && !IconStorage.isCustomIconReference(icona)) {
            throw new IllegalArgumentException("Icona non valida");
        }
        return icona;
    }

    private void executePersonalDelete(final String sql, final long id, final String email)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setString(2, email);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Elemento inesistente o non disponibile");
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

    public List<Transazione> caricaTransazioniPerCategoria(final String email,
            final long idCategoria) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            return transazioneDAO.findByCategoria(email, idCategoria);
        }
    }

    public List<Transazione> caricaTransazioniPerTag(final String email,
            final long idTag) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            return transazioneDAO.findByTag(email, idTag);
        }
    }

    public List<Transazione> caricaTransazioniPerFonte(final String email,
            final long idFonte) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            return transazioneDAO.findByFonte(email, idFonte);
        }
    }

    public List<Transazione> caricaTransazioniPerRicorrenza(final String email,
            final long idRicorrenza) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            return transazioneDAO.findByRicorrenza(email, idRicorrenza);
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
        } catch (final SQLException | RuntimeException ex) {
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
        } catch (final SQLException | RuntimeException ex) {
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    public List<Long> caricaTagTransazione(final String email, final long idTransazione)
            throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            return transazioneDAO.findTagIds(idTransazione, email);
        }
    }

    public void aggiornaTransazione(final String email, final long idTransazione,
            final BigDecimal importo, final LocalDate data, final String descrizione,
            final Long idCategoria, final Long idFonte, final List<Long> idTag)
            throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            final PeriodoDAO periodoDAO = new JdbcPeriodoDAO(connection);
            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);

            final Transazione precedente = transazioneDAO.findById(idTransazione, email);
            final long idPeriodo = periodoDAO.trovaOCreaPeriodo(data.getMonthValue(), data.getYear());
            final Transazione aggiornata = new Transazione(
                    idTransazione,
                    precedente.getTipo(),
                    importo,
                    data,
                    descrizione,
                    email,
                    idCategoria,
                    idPeriodo,
                    idFonte);

            transazioneDAO.aggiorna(aggiornata);
            transazioneDAO.eliminaTag(idTransazione);
            if (precedente.getTipo() == TipoTransazione.SPESA) {
                for (final Long tag : idTag) {
                    transazioneDAO.associaTag(idTransazione, tag.longValue());
                }
            }
            aggiornaBudgetDopoModifica(budgetDAO, precedente, aggiornata);
            connection.commit();
        } catch (final SQLException | RuntimeException ex) {
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    public void eliminaTransazione(final String email, final long idTransazione)
            throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            final TransazioneDAO transazioneDAO = new JdbcTransazioneDAO(connection);
            final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);
            final Transazione transazione = transazioneDAO.findById(idTransazione, email);

            transazioneDAO.elimina(idTransazione, email);
            sottraiDaBudgetSeSpesa(budgetDAO, transazione);
            connection.commit();
        } catch (final SQLException | RuntimeException ex) {
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    private void aggiornaBudgetDopoModifica(final BudgetDAO budgetDAO,
            final Transazione precedente, final Transazione aggiornata) throws SQLException {
        sottraiDaBudgetSeSpesa(budgetDAO, precedente);
        aggiungiABudgetSeSpesa(budgetDAO, aggiornata);
    }

    private void sottraiDaBudgetSeSpesa(final BudgetDAO budgetDAO,
            final Transazione transazione) throws SQLException {
        if (transazione.getTipo() == TipoTransazione.SPESA && transazione.getIdCategoria() != null) {
            budgetDAO.aggiungiSpesaAiBudget(
                    transazione.getEmail(),
                    transazione.getIdPeriodo(),
                    transazione.getIdCategoria(),
                    transazione.getImporto().negate());
        }
    }

    private void aggiungiABudgetSeSpesa(final BudgetDAO budgetDAO,
            final Transazione transazione) throws SQLException {
        if (transazione.getTipo() == TipoTransazione.SPESA && transazione.getIdCategoria() != null) {
            budgetDAO.aggiungiSpesaAiBudget(
                    transazione.getEmail(),
                    transazione.getIdPeriodo(),
                    transazione.getIdCategoria(),
                    transazione.getImporto());
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
