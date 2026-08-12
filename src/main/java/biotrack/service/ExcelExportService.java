package biotrack.service;

import biotrack.model.HealthRecord;
import biotrack.model.MedicalDocument;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Exports records/documents to a formatted .xlsx workbook using Apache POI.
 * Health records and documents get their own sheets (they have unrelated
 * columns, so a single combined sheet with a "type" column would mean a lot
 * of empty cells either way - separate sheets keep each one clean and easy
 * to open directly in Excel).
 */
public class ExcelExportService {

    public static void exportRecords(List<HealthRecord> records, int userAgeYears, File outFile) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Health Records");
            CellStyle headerStyle = headerStyle(wb);
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd hh:mm"));

            String[] headers = {
                    "Date", "Weight (kg)", "Height (m)", "BMI", "BMI Status", "Temp (C)", "Temp Status",
                    "Systolic", "Diastolic", "BP Status", "Heart Rate", "HR Status",
                    "Sugar (mg/dL)", "Sugar Context", "Sugar Status", "SpO2 (%)", "Cholesterol", "Notes"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            int rowIdx = 1;
            for (HealthRecord r : records) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                row.createCell(col++).setCellValue(sdf.format(r.getRecordedAt()));
                row.createCell(col++).setCellValue(r.getWeight());
                row.createCell(col++).setCellValue(r.getHeight());
                row.createCell(col++).setCellValue(r.getBmi());
                row.createCell(col++).setCellValue(HealthAnalyzer.classifyBMI(r.getBmi()).label);
                row.createCell(col++).setCellValue(r.getTemperature());
                row.createCell(col++).setCellValue(HealthAnalyzer.classifyTemperature(r.getTemperature()).label);
                if (r.hasSystolicDiastolic()) {
                    row.createCell(col++).setCellValue(r.getBpSystolic());
                    row.createCell(col++).setCellValue(r.getBpDiastolic());
                    row.createCell(col++).setCellValue(
                            HealthAnalyzer.classifyBloodPressure(r.getBpSystolic(), r.getBpDiastolic()).label);
                } else {
                    row.createCell(col++);
                    row.createCell(col++);
                    row.createCell(col++).setCellValue("Legacy record - no systolic/diastolic");
                }
                row.createCell(col++).setCellValue(r.getHeartRate());
                row.createCell(col++).setCellValue(HealthAnalyzer.classifyHeartRate(r.getHeartRate(), userAgeYears).label);
                row.createCell(col++).setCellValue(r.getSugarLevel());
                row.createCell(col++).setCellValue(r.getSugarContext());
                row.createCell(col++).setCellValue(HealthAnalyzer.classifyGlucose(r.getSugarLevel(), r.getSugarContext()).label);
                if (r.getSpo2() != null) row.createCell(col++).setCellValue(r.getSpo2()); else row.createCell(col++);
                if (r.getCholesterolTotal() != null) row.createCell(col++).setCellValue(r.getCholesterolTotal()); else row.createCell(col++);
                row.createCell(col).setCellValue(r.getNotes() == null ? "" : r.getNotes());
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                wb.write(fos);
            }
        }
    }

    public static void exportDocuments(List<MedicalDocument> docs, File outFile) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Documents");
            CellStyle headerStyle = headerStyle(wb);

            String[] headers = { "File Name", "Type", "Category", "Linked Record ID", "Size (KB)", "Uploaded" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            int rowIdx = 1;
            for (MedicalDocument d : docs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getFileName());
                row.createCell(1).setCellValue(d.getFileType() == null ? "" : d.getFileType().toUpperCase());
                row.createCell(2).setCellValue(d.getCategory() == null ? "Other" : d.getCategory());
                row.createCell(3).setCellValue(d.getLinkedRecordId() == null ? "" : String.valueOf(d.getLinkedRecordId()));
                row.createCell(4).setCellValue(d.getFileSize() / 1024.0);
                row.createCell(5).setCellValue(sdf.format(d.getUploadedAt()));
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                wb.write(fos);
            }
        }
    }

    private static CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
