package it.unibo.sage.service;

import it.unibo.sage.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

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
