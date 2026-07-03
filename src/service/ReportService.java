package service;

import config.Constants;
import dao.DocumentDAO;
import model.Document;
import model.HealthRecord;
import model.User;
import utils.DateUtil;
import utils.FileUtil;
import utils.PDFUtil;
import utils.WordUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Ties AnalysisService's insights together with the export utils to
 * generate a finished, downloadable "Health Report" in the format the
 * user wants (PDF, Word, TXT or Excel), and files it into the documents
 * table/folder so it shows up alongside uploaded documents too.
 */
public class ReportService {

    public enum Format { PDF, WORD, TXT, EXCEL }

    private final AnalysisService analysisService = new AnalysisService();
    private final ExportService exportService = new ExportService();
    private final DocumentDAO documentDAO = new DocumentDAO();

    /** Generates a report file and returns its path. Also registers it in the Documents list. */
    public Path generateHealthReport(User user, List<HealthRecord> records, Format format) throws IOException {
        FileUtil.ensureDirectoriesExist();
        AnalysisService.Summary summary = analysisService.summarize(records);

        String baseName = "HealthReport_" + user.getUserId() + "_" + DateUtil.fileTimestamp();
        Path outFile;

        switch (format) {
            case PDF -> {
                outFile = Paths.get(Constants.REPORTS_DIR, baseName + ".pdf");
                PDFUtil.writeHealthReport(user.getFullName(), user.getUserId(), records, summary.insights, outFile);
            }
            case WORD -> {
                outFile = Paths.get(Constants.REPORTS_DIR, baseName + ".docx");
                WordUtil.writeHealthReport(user.getFullName(), user.getUserId(), records, summary.insights, outFile);
            }
            case TXT -> {
                outFile = Paths.get(Constants.REPORTS_DIR, baseName + ".txt");
                writeTxtReport(user, records, summary, outFile);
            }
            case EXCEL -> {
                outFile = Paths.get(Constants.REPORTS_DIR, baseName + ".xlsx");
                utils.ExcelUtil.writeHealthRecords(records, user.getUserId(), outFile);
            }
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }

        registerAsDocument(user.getUserId(), outFile, format);
        return outFile;
    }

    private void writeTxtReport(User user, List<HealthRecord> records, AnalysisService.Summary summary, Path outFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("BIOTRACK HEALTH REPORT\n");
        sb.append("=======================\n");
        sb.append("Patient: ").append(user.getFullName()).append(" (ID: ").append(user.getUserId()).append(")\n");
        sb.append("Generated: ").append(DateUtil.format(java.time.LocalDateTime.now())).append("\n\n");

        sb.append("---- Vitals Summary ----\n");
        for (HealthRecord r : records) {
            sb.append(r.getRecordDate())
                    .append(" | HR: ").append(r.getHeartRate() == null ? "-" : r.getHeartRate())
                    .append(" bpm | BP: ").append(r.getBloodPressureDisplay())
                    .append(" | Sugar: ").append(r.getBloodSugar() == null ? "-" : r.getBloodSugar())
                    .append(" mg/dL | SpO2: ").append(r.getSpo2() == null ? "-" : r.getSpo2())
                    .append("% | Weight: ").append(r.getWeightKg() == null ? "-" : r.getWeightKg())
                    .append(" kg | BMI: ").append(r.getBmi() == null ? "-" : r.getBmi())
                    .append("\n");
        }

        sb.append("\n---- Analysis & Insights ----\n");
        for (String line : summary.insights) sb.append("- ").append(line).append("\n");

        Files.writeString(outFile, sb.toString());
    }

    private void registerAsDocument(String userId, Path file, Format format) throws IOException {
        Document doc = new Document();
        doc.setUserId(userId);
        doc.setFileName(file.getFileName().toString());
        doc.setFileType(format.name().toLowerCase());
        doc.setCategory("Generated Report");
        doc.setFilePath(file.toString());
        doc.setFileSizeKb(FileUtil.sizeInKb(file));
        documentDAO.create(doc);
    }
}