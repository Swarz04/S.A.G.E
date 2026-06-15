package it.unibo.sage.service;

import it.unibo.sage.dao.BudgetDAO;
import it.unibo.sage.dao.JdbcBudgetDAO;
import it.unibo.sage.dao.JdbcPeriodoDAO;
import it.unibo.sage.dao.PeriodoDAO;
import it.unibo.sage.model.Budget;
import it.unibo.sage.utils.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ConfigurazioneInizialeService {

    private static final String NONE = "nessuna";
    private static final String NO_BUDGET = "nessuno";

    private static final String EXISTS_CATEGORIA_SQL =
            "SELECT 1 FROM CATEGORIA "
            + "WHERE LOWER(TRIM(Nome)) = LOWER(TRIM(?)) "
            + "AND (is_system = TRUE OR Email_Proprietario = ?) "
            + "LIMIT 1";

    private static final String INSERT_CATEGORIA_SQL =
            "INSERT INTO CATEGORIA (Nome, is_system, Email_Proprietario) "
            + "VALUES (?, FALSE, ?)";

    private static final String EXISTS_FONTE_SQL =
            "SELECT 1 FROM FONTE "
            + "WHERE Nome = ? AND is_system = FALSE AND Email_Proprietario = ? "
            + "LIMIT 1";

    private static final String INSERT_FONTE_SQL =
            "INSERT INTO FONTE (Nome, is_system, Email_Proprietario) "
            + "VALUES (?, FALSE, ?)";

    private static final String EXISTS_TAG_SQL =
            "SELECT 1 FROM TAG "
            + "WHERE LOWER(TRIM(Nome)) = LOWER(TRIM(?)) "
            + "AND (is_system = TRUE OR Email_Proprietario = ?) "
            + "LIMIT 1";

    private static final String INSERT_TAG_SQL =
            "INSERT INTO TAG (Nome, is_system, Email_Proprietario) "
            + "VALUES (?, FALSE, ?)";

    public void completaConfigurazione(final String email, final String focusSpese,
            final String fonteEntrata, final BigDecimal budgetMensile,
            final String gruppoTag) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            for (final String categoria : categoriePerFocus(focusSpese)) {
                inserisciSeManca(connection, EXISTS_CATEGORIA_SQL, INSERT_CATEGORIA_SQL,
                        categoria, email);
            }
            if (fonteEntrata != null && !NONE.equalsIgnoreCase(fonteEntrata)) {
                inserisciSeManca(connection, EXISTS_FONTE_SQL, INSERT_FONTE_SQL,
                        nomeFonte(fonteEntrata), email);
            }
            for (final String tag : tagPerGruppo(gruppoTag)) {
                inserisciSeManca(connection, EXISTS_TAG_SQL, INSERT_TAG_SQL, tag, email);
            }
            if (budgetMensile != null && budgetMensile.signum() > 0) {
                salvaBudgetMensile(connection, email, budgetMensile);
            }

            connection.commit();
        } catch (final SQLException ex) {
            rollbackSilenzioso(connection);
            throw ex;
        } finally {
            closeSilenzioso(connection);
        }
    }

    private List<String> categoriePerFocus(final String focusSpese) {
        if ("universita".equalsIgnoreCase(focusSpese)) {
            return List.of("Mensa", "Libri", "Trasporti");
        }
        if ("casa".equalsIgnoreCase(focusSpese)) {
            return List.of("Casa", "Alimentari", "Bollette");
        }
        if ("trasporti".equalsIgnoreCase(focusSpese)) {
            return List.of("Trasporti", "Carburante", "Abbonamenti");
        }
        if ("lavoro".equalsIgnoreCase(focusSpese)) {
            return List.of("Materiale lavoro", "Trasporti", "Pasti fuori");
        }
        return List.of("Alimentari", "Trasporti", "Svago");
    }

    private String nomeFonte(final String fonteEntrata) {
        if ("borsa di studio".equalsIgnoreCase(fonteEntrata)) {
            return "Borsa di studio";
        }
        if ("stipendio".equalsIgnoreCase(fonteEntrata)) {
            return "Stipendio";
        }
        if ("lavoro occasionale".equalsIgnoreCase(fonteEntrata)) {
            return "Lavoro occasionale";
        }
        if ("aiuto famiglia".equalsIgnoreCase(fonteEntrata)) {
            return "Aiuto famiglia";
        }
        return fonteEntrata;
    }

    private List<String> tagPerGruppo(final String gruppoTag) {
        if ("studio".equalsIgnoreCase(gruppoTag)) {
            return List.of("Studio", "Esame", "Extra");
        }
        if ("lavoro".equalsIgnoreCase(gruppoTag)) {
            return List.of("Lavoro", "Extra");
        }
        if ("casa e trasporti".equalsIgnoreCase(gruppoTag)
                || "essenziali".equalsIgnoreCase(gruppoTag)) {
            return List.of("Casa", "Trasporti");
        }
        return List.of("Extra", "Da rivedere");
    }

    private void salvaBudgetMensile(final Connection connection, final String email,
            final BigDecimal importoLimite) throws SQLException {
        final LocalDate today = LocalDate.now();
        final PeriodoDAO periodoDAO = new JdbcPeriodoDAO(connection);
        final BudgetDAO budgetDAO = new JdbcBudgetDAO(connection);
        final long idPeriodo = periodoDAO.trovaOCreaPeriodo(today.getMonthValue(), today.getYear());
        final BigDecimal totaleSpeso = budgetDAO.calcolaTotaleSpeso(email, idPeriodo, null);

        budgetDAO.salvaOAggiorna(new Budget(
                0,
                email,
                idPeriodo,
                null,
                importoLimite,
                totaleSpeso,
                true));
    }

    private void inserisciSeManca(final Connection connection, final String existsSql,
            final String insertSql, final String nome, final String email) throws SQLException {
        if (nome == null || nome.isBlank()) {
            return;
        }
        if (esisteElementoPersonale(connection, existsSql, nome, email)) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setString(1, nome);
            statement.setString(2, email);
            statement.executeUpdate();
        }
    }

    private boolean esisteElementoPersonale(final Connection connection,
            final String existsSql, final String nome, final String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
            statement.setString(1, nome);
            statement.setString(2, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
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
