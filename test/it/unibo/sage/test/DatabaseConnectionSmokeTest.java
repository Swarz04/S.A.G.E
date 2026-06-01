package it.unibo.sage.test;

import it.unibo.sage.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnectionSmokeTest {

    private DatabaseConnectionSmokeTest() {
    }

    public static void runAll() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            TestAssertions.assertFalse(connection.isClosed(),
                    "La connessione al database deve risultare aperta");
        }
    }
}
