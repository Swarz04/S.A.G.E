package it.unibo.sage.dao;

import it.unibo.sage.model.Budget;
import it.unibo.sage.model.Periodo;
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
            + "Alert_Soglia = VALUES(Alert_Soglia), "
            + "Totale_Speso_Attuale = VALUES(Totale_Speso_Attuale)";


    private static final String UPDATE_BUDGET_SQL =
            "UPDATE BUDGET "
            + "SET Importo_Limite = ?, Alert_Soglia = ?, Totale_Speso_Attuale = ?, "
            + "ID_Periodo = ?, ID_Categoria = ? "
            + "WHERE Email = ? AND ID_Budget = ?";

    private static final String DELETE_BUDGET_SQL =
            "DELETE FROM BUDGET WHERE Email = ? AND ID_Budget = ?";

    private static final String EXISTS_BUDGET_AMBITO_SQL =
            "SELECT 1 FROM BUDGET "
            + "WHERE Email = ? "
            + "AND ID_Categoria_Key = COALESCE(?, 0) "
            + "AND ID_Budget <> ? "
            + "LIMIT 1";

    private static final String FIND_PERIODO_BUDGET_SQL =
            "SELECT P.ID_Periodo, P.Mese, P.Anno "
            + "FROM BUDGET B JOIN PERIODO P ON P.ID_Periodo = B.ID_Periodo "
            + "WHERE B.Email = ? AND B.ID_Budget = ?";

    private static final String CALCOLA_TOTALE_GLOBALE_SQL =
            "SELECT COALESCE(SUM(Importo), 0) AS Totale "
            + "FROM TRANSIZIONE "
            + "WHERE Email = ? AND ID_Periodo = ? AND TipoTransazione = 'S'";

    private static final String CALCOLA_TOTALE_CATEGORIA_SQL =
            "SELECT COALESCE(SUM(Importo), 0) AS Totale "
            + "FROM TRANSIZIONE "
            + "WHERE Email = ? AND ID_Periodo = ? AND TipoTransazione = 'S' "
            + "AND ID_Categoria = ?";

    private static final String UPDATE_BUDGET_GLOBALE_SQL =
            "UPDATE BUDGET "
            + "SET Totale_Speso_Attuale = Totale_Speso_Attuale + ? "
            + "WHERE Email = ? AND ID_Periodo = ? AND ID_Categoria IS NULL";

    private static final String UPDATE_BUDGET_CATEGORIA_SQL =
            "UPDATE BUDGET "
            + "SET Totale_Speso_Attuale = Totale_Speso_Attuale + ? "
            + "WHERE Email = ? AND ID_Periodo = ? AND ID_Categoria = ?";

    private static final String FIND_BY_UTENTE_SQL =
            "SELECT B.ID_Budget, B.Email, B.ID_Periodo, B.ID_Categoria, B.Importo_Limite, "
            + "B.Totale_Speso_Attuale, B.Alert_Soglia "
            + "FROM BUDGET B "
            + "JOIN ("
            + "    SELECT MAX(ID_Budget) AS ID_Budget "
            + "    FROM BUDGET "
            + "    WHERE Email = ? "
            + "    GROUP BY ID_Categoria_Key"
            + ") Ultimo ON Ultimo.ID_Budget = B.ID_Budget "
            + "ORDER BY B.ID_Categoria_Key, B.ID_Budget";

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
    public void aggiornaBudget(final Budget budget) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_BUDGET_SQL)) {
            statement.setBigDecimal(1, budget.getImportoLimite());
            statement.setBoolean(2, budget.isAlertSoglia());
            statement.setBigDecimal(3, budget.getTotaleSpesoAttuale());
            statement.setLong(4, budget.getIdPeriodo());
            setNullableLong(statement, 5, budget.getIdCategoria());
            statement.setString(6, budget.getEmail());
            statement.setLong(7, budget.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void eliminaBudget(final String email, final long idBudget) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_BUDGET_SQL)) {
            statement.setString(1, email);
            statement.setLong(2, idBudget);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean esisteBudgetPerAmbitoDiversoDaId(final String email, final Long idCategoria,
            final long idBudgetEscluso) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(EXISTS_BUDGET_AMBITO_SQL)) {
            statement.setString(1, email);
            setNullableLong(statement, 2, idCategoria);
            statement.setLong(3, idBudgetEscluso);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public Periodo findPeriodoBudget(final String email, final long idBudget) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_PERIODO_BUDGET_SQL)) {
            statement.setString(1, email);
            statement.setLong(2, idBudget);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Budget non trovato.");
                }
                return new Periodo(
                        resultSet.getLong("ID_Periodo"),
                        resultSet.getInt("Mese"),
                        resultSet.getInt("Anno"));
            }
        }
    }

    @Override
    public BigDecimal calcolaTotaleSpeso(final String email, final long idPeriodo,
            final Long idCategoria) throws SQLException {
        final String sql = idCategoria == null
                ? CALCOLA_TOTALE_GLOBALE_SQL
                : CALCOLA_TOTALE_CATEGORIA_SQL;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setLong(2, idPeriodo);
            if (idCategoria != null) {
                statement.setLong(3, idCategoria);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBigDecimal("Totale");
            }
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
