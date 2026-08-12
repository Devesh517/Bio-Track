package biotrack.ui;

import biotrack.dao.UserSettingsDAO;
import javafx.scene.Scene;

/**
 * Applies/removes the dark-theme.css override on the current scene and
 * persists the user's choice to the user_settings table so it's remembered
 * on next launch (see migration.sql).
 */
public class ThemeManager {

    private static final String DARK_CSS = "/css/dark-theme.css";

    public static void apply(Scene scene, boolean dark) {
        String url = ThemeManager.class.getResource(DARK_CSS).toExternalForm();
        scene.getStylesheets().remove(url);
        if (dark) scene.getStylesheets().add(url);
    }

    /** Applies the theme and best-effort persists it (failures are swallowed - theme is a convenience, not critical data). */
    public static void applyAndPersist(Scene scene, int userId, boolean dark) {
        apply(scene, dark);
        try {
            new UserSettingsDAO().updateTheme(userId, dark ? "dark" : "light");
        } catch (Exception ignored) {
            // Non-fatal: worst case the preference doesn't stick across restarts.
        }
    }
}