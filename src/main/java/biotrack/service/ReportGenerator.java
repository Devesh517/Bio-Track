package biotrack.service;

import biotrack.model.HealthRecord;
import biotrack.model.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * Professional-looking weekly PDF report: branded header band, section
 * dividers, a summary table with each vital's classification/status (from
 * HealthAnalyzer), and an optional embedded trend chart image (rendered by
 * the caller via JavaFX Node.snapshot() and passed in as a BufferedImage -
 * PDFBox has no charting of its own, so we just embed a raster of whatever
 * chart the UI already built).
 */
public class ReportGenerator {

    private static final float MARGIN = 42;
    private static final float LINE_HEIGHT = 15;
    private static final Color BRAND = new Color(0x1a, 0x3c, 0x6e);
    private static final Color BRAND_LIGHT = new Color(0xee, 0xf3, 0xfa);
    private static final Color WARNING_COLOR = new Color(0xb4, 0x6c, 0x00);
    private static final Color CRITICAL_COLOR = new Color(0xb9, 0x1c, 0x1c);
    private static final Color NORMAL_COLOR = new Color(0x15, 0x80, 0x3d);

    public static void generateWeeklyReport(User user, List<HealthRecord> records,
                                             LocalDate start, LocalDate end, File outFile) throws IOException {
        generateWeeklyReport(user, records, start, end, null, outFile);
    }

    public static void generateWeeklyReport(User user, List<HealthRecord> records, LocalDate start, LocalDate end,
                                             BufferedImage trendChart, File outFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            Page p = new Page(doc);
            drawHeader(p, "BioTrack Health Report", "Generated " + LocalDate.now());

            PDType1Font titleFont = font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font bodyFont = font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font monoFont = font(Standard14Fonts.FontName.COURIER);

            p.text(bodyFont, 11, "Name: " + user.getName() + "    Age: " + user.getAge() + "    Gender: " + nz(user.getGender()));
            p.text(bodyFont, 11, "User ID: " + user.getUserId() + "    Period: " + start + " to " + end);
            p.gap(6);
            sectionDivider(p);

            if (records.isEmpty()) {
                p.text(bodyFont, 11, "No health records found for this period.");
                p.close();
                doc.save(outFile);
                return;
            }

            // ---- Summary table -------------------------------------------------
            p.sectionTitle(titleFont, "Summary Table");
            String[] cols = { "Date", "Wt(kg)", "BMI", "Status", "BP", "Status", "HR", "Status", "Sugar", "Status" };
            float[] widths = { 78, 42, 38, 78, 55, 92, 34, 78, 42, 78 };
            p.tableHeader(monoFont, cols, widths);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (HealthRecord r : records) {
                HealthAnalyzer.Classification bmiC = HealthAnalyzer.classifyBMI(r.getBmi());
                HealthAnalyzer.Classification hrC = HealthAnalyzer.classifyHeartRate(r.getHeartRate(), user.getAge());
                HealthAnalyzer.Classification sugarC = HealthAnalyzer.classifyGlucose(r.getSugarLevel(), r.getSugarContext());
                String bpText = r.hasSystolicDiastolic() ? (r.getBpSystolic() + "/" + r.getBpDiastolic()) : "-";
                HealthAnalyzer.Classification bpC = r.hasSystolicDiastolic()
                        ? HealthAnalyzer.classifyBloodPressure(r.getBpSystolic(), r.getBpDiastolic()) : null;

                p.tableRow(monoFont, widths,
                        new Cell(sdf.format(r.getRecordedAt()), null),
                        new Cell(String.format("%.1f", r.getWeight()), null),
                        new Cell(String.format("%.1f", r.getBmi()), null),
                        new Cell(bmiC.label, colorFor(bmiC.severity)),
                        new Cell(bpText, null),
                        new Cell(bpC == null ? "-" : bpC.label, bpC == null ? null : colorFor(bpC.severity)),
                        new Cell(String.valueOf(r.getHeartRate()), null),
                        new Cell(hrC.label, colorFor(hrC.severity)),
                        new Cell(String.valueOf(r.getSugarLevel()), null),
                        new Cell(sugarC.label, colorFor(sugarC.severity))
                );
            }
            p.gap(10);
            sectionDivider(p);

            // ---- Averages --------------------------------------------------
            p.sectionTitle(titleFont, "Period Averages");
            double sumWeight = 0, sumBmi = 0, sumTemp = 0, sumHR = 0, sumSugar = 0;
            for (HealthRecord r : records) {
                sumWeight += r.getWeight(); sumBmi += r.getBmi(); sumTemp += r.getTemperature();
                sumHR += r.getHeartRate(); sumSugar += r.getSugarLevel();
            }
            int n = records.size();
            p.text(bodyFont, 10, String.format("Average weight: %.1f kg    Average BMI: %.1f    Average temperature: %.1f C",
                    sumWeight / n, sumBmi / n, sumTemp / n));
            p.text(bodyFont, 10, String.format("Average heart rate: %.0f bpm    Average sugar: %.0f mg/dL", sumHR / n, sumSugar / n));
            p.gap(10);
            sectionDivider(p);

            // ---- Trend chart -------------------------------------------------
            if (trendChart != null) {
                p.sectionTitle(titleFont, "Trends");
                p.image(trendChart);
                p.gap(10);
                sectionDivider(p);
            }

            // ---- Health flags --------------------------------------------------
            p.sectionTitle(titleFont, "Health Flags (latest reading)");
            HealthRecord latest = records.get(0);
            String analysis = HealthAnalyzer.analyze(latest.getBmi(), latest.getBpSystolic(), latest.getBpDiastolic(),
                    latest.getTemperature(), latest.getHeartRate(), user.getAge(), latest.getSugarLevel(),
                    latest.getSugarContext(), latest.getSpo2(), latest.getCholesterolTotal());
            for (String line : analysis.split("\n")) {
                if (!line.isBlank()) p.text(bodyFont, 10, line);
            }

            p.close();
            doc.save(outFile);
        }
    }

    private static Color colorFor(HealthAnalyzer.Severity s) {
        return switch (s) {
            case NORMAL -> NORMAL_COLOR;
            case WARNING -> WARNING_COLOR;
            case CRITICAL -> CRITICAL_COLOR;
        };
    }

    private static void drawHeader(Page p, String title, String subtitle) throws IOException {
        p.filledBand(BRAND, 54);
        p.textAt(font(Standard14Fonts.FontName.HELVETICA_BOLD), 18, MARGIN, p.y - 22, Color.WHITE, title);
        p.textAt(font(Standard14Fonts.FontName.HELVETICA), 10, MARGIN, p.y - 40, Color.WHITE, subtitle);
        p.y -= 70;
    }

    private static void sectionDivider(Page p) throws IOException {
        p.horizontalRule(BRAND_LIGHT);
        p.gap(8);
    }

    private static PDType1Font font(Standard14Fonts.FontName name) { return new PDType1Font(name); }

    private static String nz(String s) { return (s == null || s.isBlank()) ? "-" : s; }

    /** A single table cell: text + optional colour override. */
    private record Cell(String text, Color color) {}

    /**
     * Small drawing helper that owns the current page/content-stream and the
     * running Y cursor, handling page breaks transparently so the caller
     * never has to think about page boundaries.
     */
    private static final class Page {
        final PDDocument doc;
        PDPage page;
        PDPageContentStream cs;
        float y;

        Page(PDDocument doc) throws IOException {
            this.doc = doc;
            newPage();
        }

        void newPage() throws IOException {
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        void ensureSpace(float needed) throws IOException {
            if (y - needed < 50) {
                cs.close();
                newPage();
            }
        }

        void gap(float amount) { y -= amount; }

        void sectionTitle(PDType1Font font, String text) throws IOException {
            ensureSpace(LINE_HEIGHT * 2);
            cs.setNonStrokingColor(BRAND);
            cs.setFont(font, 13);
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(text);
            cs.endText();
            y -= LINE_HEIGHT * 1.4f;
            cs.setNonStrokingColor(Color.BLACK);
        }

        void text(PDType1Font font, float size, String text) throws IOException {
            ensureSpace(LINE_HEIGHT);
            cs.setFont(font, size);
            cs.beginText();
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(text);
            cs.endText();
            y -= LINE_HEIGHT;
        }

        void textAt(PDType1Font font, float size, float x, float yPos, Color color, String text) throws IOException {
            cs.setNonStrokingColor(color);
            cs.setFont(font, size);
            cs.beginText();
            cs.newLineAtOffset(x, yPos);
            cs.showText(text);
            cs.endText();
            cs.setNonStrokingColor(Color.BLACK);
        }

        void filledBand(Color color, float height) throws IOException {
            cs.setNonStrokingColor(color);
            cs.addRect(0, y - height, page.getMediaBox().getWidth(), height);
            cs.fill();
            cs.setNonStrokingColor(Color.BLACK);
        }

        void horizontalRule(Color color) throws IOException {
            cs.setStrokingColor(color);
            cs.moveTo(MARGIN, y);
            cs.lineTo(page.getMediaBox().getWidth() - MARGIN, y);
            cs.stroke();
            cs.setStrokingColor(Color.BLACK);
        }

        void tableHeader(PDType1Font font, String[] cols, float[] widths) throws IOException {
            ensureSpace(LINE_HEIGHT);
            cs.setFont(font, 8);
            cs.setNonStrokingColor(BRAND);
            float x = MARGIN;
            for (int i = 0; i < cols.length; i++) {
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText(cols[i]);
                cs.endText();
                x += widths[i];
            }
            cs.setNonStrokingColor(Color.BLACK);
            y -= LINE_HEIGHT * 0.8f;
            horizontalRule(BRAND_LIGHT);
            y -= LINE_HEIGHT * 0.6f;
        }

        void tableRow(PDType1Font font, float[] widths, Cell... cells) throws IOException {
            ensureSpace(LINE_HEIGHT);
            cs.setFont(font, 8);
            float x = MARGIN;
            for (int i = 0; i < cells.length; i++) {
                Cell c = cells[i];
                cs.setNonStrokingColor(c.color() == null ? Color.BLACK : c.color());
                cs.beginText();
                cs.newLineAtOffset(x, y);
                cs.showText(truncate(c.text(), widths[i]));
                cs.endText();
                x += widths[i];
            }
            cs.setNonStrokingColor(Color.BLACK);
            y -= LINE_HEIGHT * 0.95f;
        }

        void image(BufferedImage img) throws IOException {
            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, img);
            float maxWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
            float scale = Math.min(1f, maxWidth / img.getWidth());
            float w = img.getWidth() * scale;
            float h = img.getHeight() * scale;
            ensureSpace(h + 10);
            cs.drawImage(pdImage, MARGIN, y - h, w, h);
            y -= (h + 10);
        }

        private static String truncate(String s, float width) {
            int max = (int) (width / 4.6);
            return s.length() > max ? s.substring(0, Math.max(0, max - 1)) + "." : s;
        }

        void close() throws IOException { cs.close(); }
    }
}
