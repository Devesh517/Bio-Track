package application;

import database.DBConnection;

/**
 * Plain entry point (no JavaFX Application subclassing) that can be used
 * to sanity-check the environment - e.g. verifying the MySQL connection -
 * before handing off to the real JavaFX Main. Useful for a quick
 * `java -cp ... application.AppLauncher` smoke test.
 */
public class AppLauncher {

    public static void main(String[] args) {
        System.out.println("BioTrack - checking environment...");
        boolean dbOk = DBConnection.testConnection();
        System.out.println("Database connection: " + (dbOk ? "OK" : "FAILED - check config.properties / env vars"));
        System.out.println("Starting BioTrack...");
        Main.main(args);
    }
}