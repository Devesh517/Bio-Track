package biotrack.dao;
import biotrack.db.DBConnection;
import biotrack.model.HealthRecord;
import biotrack.model.RecordFilter;
import biotrack.service.HealthAnalyzer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthRecordDAO {

    public void insertRecord(int userId, double weight, double height, double bmi, double temperature,
                             Integer bpSystolic, Integer bpDiastolic, int heartRate, int sugarLevel,
                             String sugarContext, Double spo2, Integer cholesterolTotal, String notes) throws SQLException {
        String sql = "INSERT INTO health_records (user_id, weight, height, bmi, temperature, " +
                "bp_systolic, bp_diastolic, heart_rate, sugar_level, sugar_context, spo2, cholesterol_total, notes) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDouble(2, weight);
            ps.setDouble(3, height);
            ps.setDouble(4, bmi);
            ps.setDouble(5, temperature);
            setNullableInt(ps, 6, bpSystolic);
            setNullableInt(ps, 7, bpDiastolic);
            ps.setInt(8, heartRate);
            ps.setInt(9, sugarLevel);
            ps.setString(10, sugarContext);
            if (spo2 == null) ps.setNull(11, Types.DOUBLE); else ps.setDouble(11, spo2);
            setNullableInt(ps, 12, cholesterolTotal);
            ps.setString(13, notes);
            ps.executeUpdate();
        }
    }

    public List<HealthRecord> getRecordsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM health_records WHERE user_id = ? ORDER BY recorded_at DESC";
        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Most recent N records for a user (dashboard "Recent Records" card). */
    public List<HealthRecord> getRecentRecords(int userId, int limit) throws SQLException {
        String sql = "SELECT * FROM health_records WHERE user_id = ? ORDER BY recorded_at DESC LIMIT ?";
        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<HealthRecord> getRecordsBetween(int userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT * FROM health_records WHERE user_id = ? AND DATE(recorded_at) BETWEEN ? AND ? " +
                "ORDER BY recorded_at ASC";
        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Unified search/filter used by the "Search & Filters" bar. All criteria
     * are AND-ed. The SQL is built with a fixed set of conditional clauses and
     * bound via PreparedStatement placeholders - user input is never
     * concatenated into the query string. valueField is validated against a
     * whitelist (RecordFilter.VALUE_FIELDS) before ever touching the SQL text,
     * since column names can't be bound as parameters.
     *
     * Status (Normal/Warning/Critical) can't be expressed in SQL because it's
     * computed by HealthAnalyzer from several columns at once, so it's applied
     * as a final pass over the already-filtered rows in Java.
     */
    public List<HealthRecord> search(int userId, RecordFilter filter, int userAgeYears) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM health_records WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (filter.hasKeyword()) {
            sql.append(" AND notes LIKE ?");
            params.add("%" + filter.keyword.trim() + "%");
        }
        if (filter.hasDateRange()) {
            sql.append(" AND DATE(recorded_at) BETWEEN ? AND ?");
            params.add(Date.valueOf(filter.startDate));
            params.add(Date.valueOf(filter.endDate));
        }
        if (filter.hasValueRange() && Arrays.asList(RecordFilter.VALUE_FIELDS).contains(filter.valueField)) {
            if (filter.valueMin != null) {
                sql.append(" AND ").append(filter.valueField).append(" >= ?");
                params.add(filter.valueMin);
            }
            if (filter.valueMax != null) {
                sql.append(" AND ").append(filter.valueField).append(" <= ?");
                params.add(filter.valueMax);
            }
        }
        sql.append(" ORDER BY recorded_at DESC");

        List<HealthRecord> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }

        if (filter.status != null) {
            List<HealthRecord> filtered = new ArrayList<>();
            for (HealthRecord r : list) {
                HealthAnalyzer.Severity sev = HealthAnalyzer.overallSeverity(
                        r.getBmi(), r.getBpSystolic(), r.getBpDiastolic(), r.getTemperature(),
                        r.getHeartRate(), userAgeYears, r.getSugarLevel(), r.getSugarContext(),
                        r.getSpo2(), r.getCholesterolTotal());
                if (sev == filter.status) filtered.add(r);
            }
            return filtered;
        }
        return list;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER); else ps.setInt(index, value);
    }

    private HealthRecord mapRow(ResultSet rs) throws SQLException {
        Integer legacyBp = (Integer) rs.getObject("blood_pressure");
        Integer systolic = (Integer) rs.getObject("bp_systolic");
        Integer diastolic = (Integer) rs.getObject("bp_diastolic");
        Double spo2 = (Double) rs.getObject("spo2");
        Integer cholesterol = (Integer) rs.getObject("cholesterol_total");
        return new HealthRecord(
                rs.getInt("record_id"), rs.getInt("user_id"), rs.getDouble("weight"), rs.getDouble("height"),
                rs.getDouble("bmi"), rs.getDouble("temperature"), legacyBp, systolic, diastolic,
                rs.getInt("heart_rate"), rs.getInt("sugar_level"), rs.getString("sugar_context"),
                spo2, cholesterol, rs.getString("notes"), rs.getTimestamp("recorded_at")
        );
    }
}
