package biotrack.model;
import java.sql.Timestamp;

public class MedicalDocument {
    private final int docId;
    private final int userId;
    private final String fileName;
    private final String fileType;
    private final String category;
    private final Integer linkedRecordId;
    private final long fileSize;
    private final Timestamp uploadedAt;

    public MedicalDocument(int docId, int userId, String fileName, String fileType, String category,
                            Integer linkedRecordId, long fileSize, Timestamp uploadedAt) {
        this.docId = docId;
        this.userId = userId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.category = category;
        this.linkedRecordId = linkedRecordId;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public int getDocId() { return docId; }
    public int getUserId() { return userId; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public String getCategory() { return category; }
    public Integer getLinkedRecordId() { return linkedRecordId; }
    public long getFileSize() { return fileSize; }
    public Timestamp getUploadedAt() { return uploadedAt; }

    public static final String[] CATEGORIES = {
        "Lab Report", "Prescription", "Scan", "Insurance", "Other"
    };
}
