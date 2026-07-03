package dao;

import database.DBConnection;
import model.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    public int create(Document doc) {
        String sql = "INSERT INTO documents (user_id, file_name, file_type, category, file_path, file_size_kb) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doc.getUserId());
            ps.setString(2, doc.getFileName());
            ps.setString(3, doc.getFileType());
            ps.setString(4, doc.getCategory());
            ps.setString(5, doc.getFilePath());
            ps.setDouble(6, doc.getFileSizeKb());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save document: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean delete(int id, String userId) {
        String sql = "DELETE FROM documents WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete document: " + e.getMessage(), e);
        }
    }

    public List<Document> findAllByUser(String userId) {
        String sql = "SELECT * FROM documents WHERE user_id=? ORDER BY uploaded_at DESC";
        List<Document> docs = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) docs.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch documents: " + e.getMessage(), e);
        }
        return docs;
    }

    public List<Document> findRecent(String userId, int limit) {
        String sql = "SELECT * FROM documents WHERE user_id=? ORDER BY uploaded_at DESC LIMIT ?";
        List<Document> docs = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) docs.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch recent documents: " + e.getMessage(), e);
        }
        return docs;
    }

    private Document map(ResultSet rs) throws SQLException {
        Document d = new Document();
        d.setId(rs.getInt("id"));
        d.setUserId(rs.getString("user_id"));
        d.setFileName(rs.getString("file_name"));
        d.setFileType(rs.getString("file_type"));
        d.setCategory(rs.getString("category"));
        d.setFilePath(rs.getString("file_path"));
        d.setFileSizeKb(rs.getDouble("file_size_kb"));
        Timestamp ts = rs.getTimestamp("uploaded_at");
        if (ts != null) d.setUploadedAt(ts.toLocalDateTime());
        return d;
    }
}