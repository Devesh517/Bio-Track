package config;

import model.User;

/**
 * Holds the currently logged-in user for the lifetime of the app.
 * Simple in-memory singleton - fine for a single-user desktop JavaFX app.
 */
public class Session {

    private static User currentUser;

    private Session() { }

    public static void login(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}