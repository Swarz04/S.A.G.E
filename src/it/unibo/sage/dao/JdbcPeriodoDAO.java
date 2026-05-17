package it.unibo.sage.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcPeriodoDAO implements PeriodoDAO {

    private static final String UPSERT_PERIODO_SQL =
            "INSERT INTO PERIODO (Mese, Anno) VALUES (?, ?) "
            + "ON DUPLICATE KEY UPDATE ID_Periodo = LAST_INSERT_ID(ID_Periodo)";

    private static final String LAST_ID_SQL = "SELECT LAST_INSERT_ID()";

    private final Connection connection;

    public JdbcPeriodoDAO(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public long trovaOCreaPeriodo(final int mese, final int anno) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_PERIODO_SQL)) {
            statement.setInt(1, mese);
            statement.setInt(2, anno);
            statement.executeUpdate();
        }

        try (PreparedStatement statement = connection.prepareStatement(LAST_ID_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
