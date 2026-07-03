package dao;

import database.DBConnection;
import enums.BloodGroup;
import enums.Gender;
import enums.UserRole;
import model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class UserDAO {

    public boolean create(User user) {
        String sql = "INSERT INTO users (user_id, full_name, email, password_hash, gender, blood_group, dob, phone, height_cm, role) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUserId());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getGender() != null ? user.getGender().name() : null);
            ps.setString(6, user.getBloodGroup() != null ? user.getBloodGroup().name() : null);
            ps.setObject(7, user.getDob());
            ps.setString(8, user.getPhone());
            ps.setDouble(9, user.getHeightCm());
            ps.setString(10, user.getRole() != null ? user.getRole().name() : UserRole.PATIENT.name());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public Optional<User> findByUserId(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, gender=?, blood_group=?, dob=?, phone=?, height_cm=? WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getGender() != null ? user.getGender().name() : null);
            ps.setString(4, user.getBloodGroup() != null ? user.getBloodGroup().name() : null);
            ps.setObject(5, user.getDob());
            ps.setString(6, user.getPhone());
            ps.setDouble(7, user.getHeightCm());
            ps.setString(8, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    /** Generates the next sequential display ID, e.g. BT1025 -> BT1026. */
    public String generateNextUserId() {
        String sql = "SELECT user_id FROM users ORDER BY id DESC LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("user_id");
                String digits = last.replaceAll("\\D+", "");
                int next = digits.isEmpty() ? 1000 : Integer.parseInt(digits) + 1;
                return "BT" + next;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate user id: " + e.getMessage(), e);
        }
        return "BT1000";
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUserId(rs.getString("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        String gender = rs.getString("gender");
        if (gender != null) u.setGender(Gender.valueOf(gender));
        String bg = rs.getString("blood_group");
        if (bg != null) u.setBloodGroup(BloodGroup.valueOf(bg));
        Date dob = rs.getDate("dob");
        if (dob != null) u.setDob(dob.toLocalDate());
        u.setPhone(rs.getString("phone"));
        u.setHeightCm(rs.getDouble("height_cm"));
        String role = rs.getString("role");
        if (role != null) u.setRole(UserRole.valueOf(role));
        return u;
    }
}