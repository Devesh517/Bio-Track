package dao;

import database.DBConnection;
import model.HealthRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HealthRecordDAO {

    public int create(HealthRecord r) {
        String sql = "INSERT INTO health_records " +
                "(user_id, record_date, record_time, heart_rate, bp_systolic, bp_diastolic, blood_sugar, spo2, weight_kg, bmi, notes) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getUserId());
            ps.setObject(2, r.getRecordDate());
            ps.setObject(3, r.getRecordTime());
            setNullableInt(ps, 4, r.getHeartRate());
            setNullableInt(ps, 5, r.getBpSystolic());
            setNullableInt(ps, 6, r.getBpDiastolic());
            setNullableDouble(ps, 7, r.getBloodSugar());
            setNullableDouble(ps, 8, r.getSpo2());
            setNullableDouble(ps, 9, r.getWeightKg());
            setNullableDouble(ps, 10, r.getBmi());
            ps.setString(11, r.getNotes());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save health record: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean update(HealthRecord r) {
        String sql = "UPDATE health_records SET record_date=?, record_time=?, heart_rate=?, bp_systolic=?, bp_diastolic=?, " +
                "blood_sugar=?, spo2=?, weight_kg=?, bmi=?, notes=? WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, r.getRecordDate());
            ps.setObject(2, r.getRecordTime());
            setNullableInt(ps, 3, r.getHeartRate());
            setNullableInt(ps, 4, r.getBpSystolic());
            setNullableInt(ps, 5, r.getBpDiastolic());
            setNullableDouble(ps, 6, r.getBloodSugar());
            setNullableDouble(ps, 7, r.getSpo2());
            setNullableDouble(ps, 8, r.getWeightKg());
            setNullableDouble(ps, 9, r.getBmi());
            ps.setString(10, r.getNotes());
            ps.setInt(11, r.getId());
            ps.setString(12, r.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update health record: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id, String userId) {
        String sql = "DELETE FROM health_records WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete health record: " + e.getMessage(), e);
        }
    }

    public Optional<HealthRecord> findLatest(String userId) {
        String sql = "SELECT * FROM health_records WHERE user_id=? ORDER BY record_date DESC, record_time DESC, id DESC LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch latest record: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<HealthRecord> findAllByUser(String userId) {
        return runQuery("SELECT * FROM health_records WHERE user_id=? ORDER BY record_date DESC, record_time DESC", userId, null, null);
    }

    public List<HealthRecord> findByDateRange(String userId, LocalDate from, LocalDate to) {
        return runQuery("SELECT * FROM health_records WHERE user_id=? AND record_date BETWEEN ? AND ? ORDER BY record_date ASC, record_time ASC",
                userId, from, to);
    }

    /** Convenience used by the Dashboard chart: last N days. */
    public List<HealthRecord> findLastNDays(String userId, int days) {
        return findByDateRange(userId, LocalDate.now().minusDays(days - 1L), LocalDate.now());
    }

    private List<HealthRecord> runQuery(String sql, String userId, LocalDate from, LocalDate to) {
        List<HealthRecord> results = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            if (from != null) ps.setObject(2, from);
            if (to != null) ps.setObject(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch health records: " + e.getMessage(), e);
        }
        return results;
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER); else ps.setInt(idx, val);
    }

    private void setNullableDouble(PreparedStatement ps, int idx, Double val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.DOUBLE); else ps.setDouble(idx, val);
    }

    private HealthRecord map(ResultSet rs) throws SQLException {
        HealthRecord r = new HealthRecord();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getString("user_id"));
        Date d = rs.getDate("record_date");
        if (d != null) r.setRecordDate(d.toLocalDate());
        Time t = rs.getTime("record_time");
        if (t != null) r.setRecordTime(t.toLocalTime());

        r.setHeartRate((Integer) rs.getObject("heart_rate"));
        r.setBpSystolic((Integer) rs.getObject("bp_systolic"));
        r.setBpDiastolic((Integer) rs.getObject("bp_diastolic"));
        r.setBloodSugar((Double) rs.getObject("blood_sugar"));
        r.setSpo2((Double) rs.getObject("spo2"));
        r.setWeightKg((Double) rs.getObject("weight_kg"));
        r.setBmi((Double) rs.getObject("bmi"));
        r.setNotes(rs.getString("notes"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }
}