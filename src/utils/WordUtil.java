package utils;

import model.HealthRecord;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Generates .docx health reports using Apache POI (XWPF). */
public class WordUtil {

    private WordUtil() { }

    public static void writeHealthReport(String patientName, String userId, List<HealthRecord> records,
                                         List<String> analysisSummary, Path outputFile) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            titleParagraph(doc, "BioTrack Health Report");

            XWPFParagraph meta = doc.createParagraph();
            meta.createRun().setText("Patient: " + patientName + "   (ID: " + userId + ")");
            XWPFParagraph meta2 = doc.createParagraph();
            meta2.createRun().setText("Generated: " + DateUtil.format(java.time.LocalDateTime.now()));

            heading(doc, "Vitals Summary");
            XWPFTable table = doc.createTable(records.size() + 1, 7);
            String[] headers = {"Date", "Heart Rate", "Blood Pressure", "Sugar", "SpO2", "Weight", "BMI"};
            for (int i = 0; i < headers.length; i++) {
                table.getRow(0).getCell(i).setText(headers[i]);
            }
            int rowIdx = 1;
            for (HealthRecord r : records) {
                XWPFTableRow row = table.getRow(rowIdx++);
                row.getCell(0).setText(String.valueOf(r.getRecordDate()));
                row.getCell(1).setText(r.getHeartRate() == null ? "-" : r.getHeartRate() + " bpm");
                row.getCell(2).setText(r.getBloodPressureDisplay());
                row.getCell(3).setText(r.getBloodSugar() == null ? "-" : r.getBloodSugar() + " mg/dL");
                row.getCell(4).setText(r.getSpo2() == null ? "-" : r.getSpo2() + " %");
                row.getCell(5).setText(r.getWeightKg() == null ? "-" : r.getWeightKg() + " kg");
                row.getCell(6).setText(r.getBmi() == null ? "-" : String.valueOf(r.getBmi()));
            }

            heading(doc, "Analysis & Insights");
            for (String line : analysisSummary) {
                doc.createParagraph().createRun().setText(line);
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                doc.write(fos);
            }
        }
    }

    /** Generic single-column text export (used for plain notes / chatbot transcripts). */
    public static void writeTextDocument(String title, List<String> lines, Path outputFile) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            titleParagraph(doc, title);
            for (String line : lines) {
                doc.createParagraph().createRun().setText(line);
            }
            try (FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                doc.write(fos);
            }
        }
    }

    private static void titleParagraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(18);
    }

    private static void heading(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(14);
    }
}