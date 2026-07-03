package database;

import config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Small JDBC helper. Each call to getConnection() returns a fresh
 * connection (simplest, safest option for a desktop app); callers are
 * expected to use try-with-resources.
 */
public class DBConnection {

    private static boolean driverLoaded = false;

    private DBConnection() { }

    private static synchronized void loadDriver() {
        if (driverLoaded) return;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found on classpath.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(
                AppConfig.dbUrl(),
                AppConfig.dbUser(),
                AppConfig.dbPassword()
        );
    }

    /** Quick health-check, e.g. to show a friendly error on app startup. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Could not connect: " + e.getMessage());
            return false;
        }
    }
}