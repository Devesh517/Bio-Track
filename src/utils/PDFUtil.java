package utils;

import model.HealthRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates simple, readable PDF reports (health summary / analysis
 * report / single document export) using Apache PDFBox.
 */
public class PDFUtil {

    private static final float MARGIN = 50;
    private static final float LEADING = 16f;

    private PDFUtil() { }

    /**
     * Writes a titled report made of paragraph lines to a PDF file.
     * Long content automatically flows onto additional pages.
     */
    public static void writeTextReport(String title, List<String> lines, Path outputFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = page.getMediaBox().getHeight() - MARGIN;
            float width = page.getMediaBox().getWidth() - 2 * MARGIN;

            cs.beginText();
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(title);
            cs.endText();
            y -= LEADING * 2;

            PDType1Font body = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float fontSize = 11f;

            for (String rawLine : lines) {
                for (String wrapped : wrap(rawLine, body, fontSize, width)) {
                    if (y < MARGIN) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = page.getMediaBox().getHeight() - MARGIN;
                    }
                    cs.beginText();
                    cs.setFont(body, fontSize);
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText(wrapped);
                    cs.endText();
                    y -= LEADING;
                }
            }
            cs.close();
            doc.save(outputFile.toFile());
        }
    }

    /** Convenience: renders a vitals table + notes as a "Health Report". */
    public static void writeHealthReport(String patientName, String userId, List<HealthRecord> records,
                                         List<String> analysisSummary, Path outputFile) throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        lines.add("Patient: " + patientName + "   (ID: " + userId + ")");
        lines.add("Generated: " + DateUtil.format(java.time.LocalDateTime.now()));
        lines.add(" ");
        lines.add("---- Vitals Summary ----");
        for (HealthRecord r : records) {
            lines.add(String.format("%s | HR: %s bpm | BP: %s | Sugar: %s mg/dL | SpO2: %s%% | Weight: %s kg | BMI: %s",
                    r.getRecordDate(),
                    r.getHeartRate() == null ? "-" : r.getHeartRate(),
                    r.getBloodPressureDisplay(),
                    r.getBloodSugar() == null ? "-" : r.getBloodSugar(),
                    r.getSpo2() == null ? "-" : r.getSpo2(),
                    r.getWeightKg() == null ? "-" : r.getWeightKg(),
                    r.getBmi() == null ? "-" : r.getBmi()));
        }
        lines.add(" ");
        lines.add("---- Analysis & Insights ----");
        lines.addAll(analysisSummary);

        writeTextReport("BioTrack Health Report", lines, outputFile);
    }

    private static List<String> wrap(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> result = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            result.add("");
            return result;
        }
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            float w = font.getStringWidth(candidate) / 1000 * fontSize;
            if (w > maxWidth && !line.isEmpty()) {
                result.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) result.add(line.toString());
        return result;
    }
}