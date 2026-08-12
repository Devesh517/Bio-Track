package biotrack.dao;
import biotrack.db.DBConnection;
import biotrack.model.User;
import org.mindrot.BCrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;

public class UserDAO {

    /**
     * Registers a new user. The password is hashed with BCrypt before it is
     * ever sent to the database - plaintext passwords are never stored.
     */
    public int insertUser(String name, int age, String gender, String phone, String email,
                          String bloodGroup, String emergencyContact, String plainPassword) throws SQLException {
        String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String sql = "INSERT INTO users (name, age, gender, phone, email, blood_group, " +
                "emergency_contact, password_hash) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.setString(6, bloodGroup);
            ps.setString(7, emergencyContact);
            ps.setString(8, passwordHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    // Every user gets a settings row up front so DashboardView
                    // never has to special-case "no settings yet".
                    new UserSettingsDAO().ensureSettingsExist(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Verifies a login attempt for the given user ID and plaintext password.
     * Returns the authenticated User on success, or null if the ID doesn't
     * exist OR the password is wrong - callers should show a single generic
     * error either way, so a caller can't tell which one it was.
     */
    public User verifyLogin(int userId, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = mapRow(rs);
                String storedHash = user.getPasswordHash();
                if (storedHash == null || storedHash.isEmpty() || !BCrypt.checkpw(plainPassword, storedHash)) {
                    return null;
                }
                return user;
            }
        }
    }

    /**
     * Updates editable profile fields (used by the "Edit Profile" screen).
     * Password is intentionally not touched here.
     */
    public void updateProfile(int userId, String name, int age, String gender, String phone,
                              String email, String bloodGroup, String emergencyContact,
                              String conditions, String allergies) throws SQLException {
        String sql = "UPDATE users SET name = ?, age = ?, gender = ?, phone = ?, email = ?, " +
                "blood_group = ?, emergency_contact = ?, conditions = ?, allergies = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.setString(6, bloodGroup);
            ps.setString(7, emergencyContact);
            ps.setString(8, conditions);
            ps.setString(9, allergies);
            ps.setInt(10, userId);
            ps.executeUpdate();
        }
    }

    /** Stores/replaces the user's profile picture. Pass null data to clear it. */
    public void updateProfilePicture(int userId, byte[] data, String fileType) throws SQLException {
        String sql = "UPDATE users SET profile_picture = ?, profile_picture_type = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (data == null) {
                ps.setNull(1, Types.LONGVARBINARY);
            } else {
                ps.setBinaryStream(1, new ByteArrayInputStream(data), data.length);
            }
            ps.setString(2, fileType);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        byte[] picture = null;
        try (InputStream is = rs.getBinaryStream("profile_picture")) {
            if (is != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
                picture = bos.toByteArray();
            }
        } catch (Exception e) {
            throw new SQLException(e);
        }
        return new User(
                rs.getInt("user_id"), rs.getString("name"), rs.getInt("age"), rs.getString("gender"),
                rs.getString("phone"), rs.getString("email"), rs.getString("blood_group"),
                rs.getString("emergency_contact"), rs.getString("password_hash"),
                picture, rs.getString("profile_picture_type"),
                rs.getString("conditions"), rs.getString("allergies"),
                rs.getTimestamp("created_at")
        );
    }
}
