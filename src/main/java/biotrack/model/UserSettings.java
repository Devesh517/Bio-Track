package biotrack.model;

/** Per-user preferences: dashboard theme and how many recent records to show. */
public class UserSettings {
    private final int userId;
    private final String theme; // "light" or "dark"
    private final int recentRecordsCount;

    public UserSettings(int userId, String theme, int recentRecordsCount) {
        this.userId = userId;
        this.theme = (theme == null || theme.isBlank()) ? "light" : theme;
        this.recentRecordsCount = recentRecordsCount <= 0 ? 5 : recentRecordsCount;
    }

    public int getUserId() { return userId; }
    public String getTheme() { return theme; }
    public boolean isDark() { return "dark".equalsIgnoreCase(theme); }
    public int getRecentRecordsCount() { return recentRecordsCount; }
}
