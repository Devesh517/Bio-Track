package config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Central place that loads runtime configuration:
 *   - MySQL connection details
 *   - Anthropic API key (for the BioTrack Assistant chatbot)
 *
 * Resolution order for every key: environment variable  ->  config.properties
 * (on classpath, or next to the running jar)  ->  hard-coded default.
 *
 * This means you can either drop a "config.properties" file next to the app,
 * or simply set environment variables (recommended for the API key so it is
 * never committed to source control):
 *
 *   BIOTRACK_DB_URL, BIOTRACK_DB_USER, BIOTRACK_DB_PASSWORD, ANTHROPIC_API_KEY
 */
public class AppConfig {

    private static final Properties props = new Properties();
    private static boolean loaded = false;

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;

        // 1) Try classpath (src/config/config.properties packaged into the jar)
        try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (in != null) props.load(in);
        } catch (IOException ignored) { }

        // 2) Try a config.properties file sitting next to the app (project root)
        Path external = Path.of("config.properties");
        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                props.load(in);
            } catch (IOException ignored) { }
        }
    }

    private static String resolve(String envKey, String propKey, String defaultValue) {
        load();
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env;
        String prop = props.getProperty(propKey);
        if (prop != null && !prop.isBlank()) return prop;
        return defaultValue;
    }

    // ---------------- Database ----------------
    public static String dbUrl() {
        return resolve("BIOTRACK_DB_URL", "db.url", "jdbc:mysql://localhost:3306/biotrack?useSSL=false&serverTimezone=UTC");
    }

    public static String dbUser() {
        return resolve("BIOTRACK_DB_USER", "db.user", "root");
    }

    public static String dbPassword() {
        return resolve("BIOTRACK_DB_PASSWORD", "db.password", "");
    }

    // ---------------- Anthropic (Chatbot) ----------------
    public static String anthropicApiKey() {
        return resolve("ANTHROPIC_API_KEY", "anthropic.api.key", "");
    }

    public static String anthropicModel() {
        return resolve("ANTHROPIC_MODEL", "anthropic.model", "claude-sonnet-4-6");
    }

    public static String anthropicApiUrl() {
        return resolve("ANTHROPIC_API_URL", "anthropic.api.url", "https://api.anthropic.com/v1/messages");
    }

    public static boolean isChatbotConfigured() {
        String key = anthropicApiKey();
        return key != null && !key.isBlank();
    }
}