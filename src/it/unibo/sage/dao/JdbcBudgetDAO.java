package it.unibo.sage.dao;

import it.unibo.sage.model.Budget;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcBudgetDAO implements BudgetDAO {

    private static final String UPSERT_BUDGET_SQL =
            "INSERT INTO BUDGET "
            + "(Importo_Limite, Alert_Soglia, Totale_Speso_Attuale, ID_Periodo, ID_Categoria, Email) "
            + "VALUES (?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "Importo_Limite = VALUES(Importo_Limite), "
            + "Alert_Soglia = VALUES(Alert_Soglia)";

    private static final String UPDATE_BUDGET_GLOBALE_SQL =
            "UPDATE BUDGET "
            + "SET Totale_Speso_Attuale = Totale_Speso_Attuale + ? "
            + "WHERE Email = ? AND ID_Periodo = ? AND ID_Categoria IS NULL";

    private static final String UPDATE_BUDGET_CATEGORIA_SQL =
            "UPDATE BUDGET "
            + "SET Totale_Speso_Attuale = Totale_Speso_Attuale + ? "
            + "WHERE Email = ? AND ID_Periodo = ? AND ID_Categoria = ?";

    private static final String FIND_BY_UTENTE_SQL =
            "SELECT ID_Budget, Email, ID_Periodo, ID_Categoria, Importo_Limite, "
            + "Totale_Speso_Attuale, Alert_Soglia "
            + "FROM BUDGET "
            + "WHERE Email = ? "
            + "ORDER BY ID_Periodo DESC, ID_Categoria";

    private final Connection connection;

    public JdbcBudgetDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public void salvaOAggiorna(final Budget budget) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_BUDGET_SQL)) {
            statement.setBigDecimal(1, budget.getImportoLimite());
            statement.setBoolean(2, budget.isAlertSoglia());
            statement.setBigDecimal(3, budget.getTotaleSpesoAttuale());
            statement.setLong(4, budget.getIdPeriodo());
            setNullableLong(statement, 5, budget.getIdCategoria());
            statement.setString(6, budget.getEmail());
            statement.executeUpdate();
        }
    }

    @Override
    public void aggiungiSpesaAiBudget(final String email, final long idPeriodo,
            final long idCategoria, final BigDecimal importo) throws SQLException {
        aggiornaBudgetGlobale(email, idPeriodo, importo);
        aggiornaBudgetCategoria(email, idPeriodo, idCategoria, importo);
    }

    @Override
    public List<Budget> findBudgetUtente(final String email) throws SQLException {
        final List<Budget> budgets = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_UTENTE_SQL)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    budgets.add(mapBudget(resultSet));
                }
            }
        }
        return budgets;
    }

    private void aggiornaBudgetGlobale(final String email, final long idPeriodo,
            final BigDecimal importo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_BUDGET_GLOBALE_SQL)) {
            statement.setBigDecimal(1, importo);
            statement.setString(2, email);
            statement.setLong(3, idPeriodo);
            statement.executeUpdate();
        }
    }

    private void aggiornaBudgetCategoria(final String email, final long idPeriodo,
            final long idCategoria, final BigDecimal importo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_BUDGET_CATEGORIA_SQL)) {
            statement.setBigDecimal(1, importo);
            statement.setString(2, email);
            statement.setLong(3, idPeriodo);
            statement.setLong(4, idCategoria);
            statement.executeUpdate();
        }
    }

    private Budget mapBudget(final ResultSet resultSet) throws SQLException {
        return new Budget(
                resultSet.getLong("ID_Budget"),
                resultSet.getString("Email"),
                resultSet.getLong("ID_Periodo"),
                readNullableLong(resultSet, "ID_Categoria"),
                resultSet.getBigDecimal("Importo_Limite"),
                resultSet.getBigDecimal("Totale_Speso_Attuale"),
                resultSet.getBoolean("Alert_Soglia"));
    }

    private void setNullableLong(final PreparedStatement statement, final int index,
            final Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setLong(index, value);
        }
    }

    private Long readNullableLong(final ResultSet resultSet, final String column)
            throws SQLException {
        final long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }
}
