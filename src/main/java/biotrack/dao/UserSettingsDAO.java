package biotrack.dao;

import biotrack.db.DBConnection;
import biotrack.model.UserSettings;

import java.sql.*;

public class UserSettingsDAO {

    public UserSettings getSettings(int userId) throws SQLException {
        ensureSettingsExist(userId);
        String sql = "SELECT * FROM user_settings WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserSettings(rs.getInt("user_id"), rs.getString("theme"),
                            rs.getInt("recent_records_count"));
                }
            }
        }
        return new UserSettings(userId, "light", 5);
    }

    public void updateTheme(int userId, String theme) throws SQLException {
        ensureSettingsExist(userId);
        String sql = "UPDATE user_settings SET theme = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, theme);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void updateRecentRecordsCount(int userId, int count) throws SQLException {
        ensureSettingsExist(userId);
        String sql = "UPDATE user_settings SET recent_records_count = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, count);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Creates a default settings row for a user if one doesn't already exist (idempotent). */
    public void ensureSettingsExist(int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO user_settings (user_id, theme, recent_records_count) VALUES (?, 'light', 5)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
