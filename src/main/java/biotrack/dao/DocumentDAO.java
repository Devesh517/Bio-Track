package biotrack.dao;
import biotrack.db.DBConnection;
import biotrack.model.MedicalDocument;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    public void insertDocument(int userId, String fileName, String fileType, String category,
                                Integer linkedRecordId, byte[] data) throws SQLException {
        String sql = "INSERT INTO documents (user_id, file_name, file_type, category, linked_record_id, file_size, file_data) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, fileName);
            ps.setString(3, fileType);
            ps.setString(4, category);
            if (linkedRecordId == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, linkedRecordId);
            ps.setLong(6, data.length);
            ps.setBinaryStream(7, new ByteArrayInputStream(data), data.length);
            ps.executeUpdate();
        }
    }

    public List<MedicalDocument> getDocumentsByUser(int userId) throws SQLException {
        String sql = "SELECT doc_id, user_id, file_name, file_type, category, linked_record_id, file_size, uploaded_at " +
                "FROM documents WHERE user_id = ? ORDER BY uploaded_at DESC";
        List<MedicalDocument> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Filtered document list. All parameters optional (pass null to skip).
     * Built with fixed conditional clauses + PreparedStatement placeholders,
     * same approach as HealthRecordDAO.search - never string-concatenated.
     */
    public List<MedicalDocument> search(int userId, String category, LocalDate start, LocalDate end,
                                         Integer linkedRecordId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT doc_id, user_id, file_name, file_type, category, linked_record_id, file_size, uploaded_at " +
                "FROM documents WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (category != null && !category.isBlank() && !category.equals("All")) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (start != null && end != null) {
            sql.append(" AND DATE(uploaded_at) BETWEEN ? AND ?");
            params.add(Date.valueOf(start));
            params.add(Date.valueOf(end));
        }
        if (linkedRecordId != null) {
            sql.append(" AND linked_record_id = ?");
            params.add(linkedRecordId);
        }
        sql.append(" ORDER BY uploaded_at DESC");

        List<MedicalDocument> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public byte[] getDocumentData(int docId) throws SQLException {
        String sql = "SELECT file_data FROM documents WHERE doc_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try (InputStream is = rs.getBinaryStream("file_data");
                         ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
                        return bos.toByteArray();
                    } catch (Exception e) {
                        throw new SQLException(e);
                    }
                }
            }
        }
        return null;
    }

    public void deleteDocument(int docId) throws SQLException {
        String sql = "DELETE FROM documents WHERE doc_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docId);
            ps.executeUpdate();
        }
    }

    private MedicalDocument mapRow(ResultSet rs) throws SQLException {
        Integer linkedRecordId = (Integer) rs.getObject("linked_record_id");
        return new MedicalDocument(rs.getInt("doc_id"), rs.getInt("user_id"),
                rs.getString("file_name"), rs.getString("file_type"), rs.getString("category"),
                linkedRecordId, rs.getLong("file_size"), rs.getTimestamp("uploaded_at"));
    }
}
