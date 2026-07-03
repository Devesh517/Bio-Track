package model;

import java.time.LocalDateTime;

public class Document {

    private int id;
    private String userId;
    private String fileName;
    private String fileType;      // pdf, png, jpg, docx, xlsx, txt
    private String category;      // Lab Report, X-Ray, Prescription, Generated Report ...
    private String filePath;
    private double fileSizeKb;
    private LocalDateTime uploadedAt;

    public Document() { }

    public Document(String userId, String fileName, String fileType, String category, String filePath) {
        this.userId = userId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.category = category;
        this.filePath = filePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public double getFileSizeKb() { return fileSizeKb; }
    public void setFileSizeKb(double fileSizeKb) { this.fileSizeKb = fileSizeKb; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}