package it.unibo.sage.service;

import it.unibo.sage.utils.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AdminService {

    public AdminSummary caricaRiepilogo() throws SQLException {
        final String sql = "SELECT "
                + "(SELECT COUNT(*) FROM UTENTE WHERE Ruolo = 'UTENTE') AS Utenti, "
                + "COUNT(T.ID_Transizione) AS Numero_Transazioni, "
                + "COALESCE(SUM(CASE WHEN T.TipoTransazione = 'S' THEN T.Importo ELSE 0 END), 0) AS Totale_Spese, "
                + "COALESCE(SUM(CASE WHEN T.TipoTransazione = 'E' THEN T.Importo ELSE 0 END), 0) AS Totale_Entrate, "
                + "(SELECT COUNT(*) FROM v_budget_stato WHERE Superato = TRUE) AS Budget_Superati "
                + "FROM TRANSIZIONE T";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return new AdminSummary(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0);
            }
            return new AdminSummary(
                    resultSet.getInt("Utenti"),
                    resultSet.getInt("Numero_Transazioni"),
                    resultSet.getBigDecimal("Totale_Spese"),
                    resultSet.getBigDecimal("Totale_Entrate"),
                    resultSet.getInt("Budget_Superati"));
        }
    }

    public List<MonthlyAdminStat> caricaAndamentoMensile() throws SQLException {
        final String sql = "SELECT Anno, Mese, Numero_Transazioni, Totale_Spese, Totale_Entrate "
                + "FROM v_statistiche_aggregate_admin ORDER BY Anno DESC, Mese DESC LIMIT 12";
        final List<MonthlyAdminStat> stats = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                final int year = resultSet.getInt("Anno");
                final int month = resultSet.getInt("Mese");
                stats.add(new MonthlyAdminStat(
                        year,
                        month,
                        shortMonthLabel(year, month),
                        resultSet.getInt("Numero_Transazioni"),
                        resultSet.getBigDecimal("Totale_Spese"),
                        resultSet.getBigDecimal("Totale_Entrate")));
            }
        }
        Collections.reverse(stats);
        return stats;
    }

    public List<CategoryUsage> caricaCategoriePiuUsate() throws SQLException {
        final String sql = "SELECT C.Nome, COUNT(*) AS Numero_Utilizzi, "
                + "COALESCE(SUM(T.Importo), 0) AS Totale_Spese "
                + "FROM TRANSIZIONE T "
                + "JOIN CATEGORIA C ON C.ID_Categoria = T.ID_Categoria "
                + "WHERE T.TipoTransazione = 'S' "
                + "GROUP BY C.ID_Categoria, C.Nome "
                + "ORDER BY Numero_Utilizzi DESC, Totale_Spese DESC, C.Nome ASC LIMIT 6";
        final List<CategoryUsage> result = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(new CategoryUsage(
                        resultSet.getString("Nome"),
                        resultSet.getInt("Numero_Utilizzi"),
                        resultSet.getBigDecimal("Totale_Spese")));
            }
        }
        return result;
    }

    public TableData caricaStatisticheAggregate() throws SQLException {
        return loadTable("SELECT Anno, Mese, Numero_Transazioni, Totale_Spese, Totale_Entrate "
                + "FROM v_statistiche_aggregate_admin ORDER BY Anno DESC, Mese DESC");
    }

    public TableData caricaStatoBudget() throws SQLException {
        return loadTable("SELECT Email, Mese, Anno, Ambito, Importo_Limite, Totale_Speso, "
                + "Residuo, Superato FROM v_budget_stato ORDER BY Anno DESC, Mese DESC, Email");
    }

    private TableData loadTable(final String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            final ResultSetMetaData metadata = resultSet.getMetaData();
            final List<String> columns = new ArrayList<>();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                columns.add(metadata.getColumnLabel(i));
            }

            final List<Object[]> rows = new ArrayList<>();
            while (resultSet.next()) {
                final Object[] row = new Object[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    row[i] = resultSet.getObject(i + 1);
                }
                rows.add(row);
            }
            return new TableData(columns, rows);
        }
    }

    private String shortMonthLabel(final int year, final int month) {
        String label = YearMonth.of(year, month).getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.ITALIAN)
                .replace(".", "");
        if (!label.isEmpty()) {
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
        }
        return label + " " + String.valueOf(year).substring(2);
    }

    public static final class AdminSummary {
        private final int users;
        private final int transactions;
        private final BigDecimal expenses;
        private final BigDecimal incomes;
        private final int exceededBudgets;

        public AdminSummary(final int users, final int transactions,
                final BigDecimal expenses, final BigDecimal incomes,
                final int exceededBudgets) {
            this.users = users;
            this.transactions = transactions;
            this.expenses = expenses == null ? BigDecimal.ZERO : expenses;
            this.incomes = incomes == null ? BigDecimal.ZERO : incomes;
            this.exceededBudgets = exceededBudgets;
        }

        public int getUsers() {
            return users;
        }

        public int getTransactions() {
            return transactions;
        }

        public BigDecimal getExpenses() {
            return expenses;
        }

        public BigDecimal getIncomes() {
            return incomes;
        }

        public int getExceededBudgets() {
            return exceededBudgets;
        }
    }

    public static final class MonthlyAdminStat {
        private final int year;
        private final int month;
        private final String label;
        private final int transactions;
        private final BigDecimal expenses;
        private final BigDecimal incomes;

        public MonthlyAdminStat(final int year, final int month, final String label,
                final int transactions, final BigDecimal expenses,
                final BigDecimal incomes) {
            this.year = year;
            this.month = month;
            this.label = label;
            this.transactions = transactions;
            this.expenses = expenses == null ? BigDecimal.ZERO : expenses;
            this.incomes = incomes == null ? BigDecimal.ZERO : incomes;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public String getLabel() {
            return label;
        }

        public int getTransactions() {
            return transactions;
        }

        public BigDecimal getExpenses() {
            return expenses;
        }

        public BigDecimal getIncomes() {
            return incomes;
        }
    }

    public static final class CategoryUsage {
        private final String name;
        private final int uses;
        private final BigDecimal totalExpenses;

        public CategoryUsage(final String name, final int uses,
                final BigDecimal totalExpenses) {
            this.name = name;
            this.uses = uses;
            this.totalExpenses = totalExpenses == null ? BigDecimal.ZERO : totalExpenses;
        }

        public String getName() {
            return name;
        }

        public int getUses() {
            return uses;
        }

        public BigDecimal getTotalExpenses() {
            return totalExpenses;
        }
    }

    public static class TableData {
        private final List<String> columns;
        private final List<Object[]> rows;

        public TableData(final List<String> columns, final List<Object[]> rows) {
            this.columns = columns;
            this.rows = rows;
        }

        public List<String> getColumns() {
            return columns;
        }

        public List<Object[]> getRows() {
            return rows;
        }
    }
}
