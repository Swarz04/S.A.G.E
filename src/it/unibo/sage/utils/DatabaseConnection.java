package it.unibo.sage.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/Schema_finale_del_relazionale_SAGE_Vista_ibrida_Raffinata"
            + "?serverTimezone=Europe/Rome";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        loadDriverIfPresent();

        final String url = readConfig("SAGE_DB_URL", "sage.db.url", DEFAULT_URL);
        final String user = readConfig("SAGE_DB_USER", "sage.db.user", DEFAULT_USER);
        final String password = readConfig("SAGE_DB_PASSWORD", "sage.db.password", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }

    private static String readConfig(final String envName, final String propertyName,
            final String defaultValue) {
        final String fromProperty = System.getProperty(propertyName);
        if (fromProperty != null && !fromProperty.isEmpty()) {
            return fromProperty;
        }
        final String fromEnv = System.getenv(envName);
        return fromEnv == null || fromEnv.isEmpty() ? defaultValue : fromEnv;
    }

    private static void loadDriverIfPresent() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (final ClassNotFoundException ignored) {
            throw new SQLException(
                    "Driver MySQL JDBC non trovato. Verifica che lib/mysql-connector-j-9.7.0.jar "
                    + "sia nel classpath dell'applicazione.");
        }
    }
}
