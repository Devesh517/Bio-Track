package utils;

import model.HealthRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Reads/writes health records to .xlsx spreadsheets - covers the
 * "store BP/sugar/etc to Excel, date wise" requirement, and doubles as
 * one of the export formats offered by ReportService.
 */
public class ExcelUtil {

    private static final String[] HEADERS = {
            "Date", "Time", "Heart Rate (bpm)", "Blood Pressure (mmHg)",
            "Blood Sugar (mg/dL)", "SpO2 (%)", "Weight (kg)", "BMI", "Notes"
    };

    private ExcelUtil() { }

    /** Writes the full list, freshly, to the given .xlsx file (overwrites). */
    public static void writeHealthRecords(List<HealthRecord> records, String userId, Path outputFile) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Health Records");

            CellStyle headerStyle = headerStyle(wb);
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(HEADERS[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (HealthRecord r : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getRecordDate() != null ? r.getRecordDate().toString() : "");
                row.createCell(1).setCellValue(r.getRecordTime() != null ? r.getRecordTime().toString() : "");
                setNumericOrBlank(row.createCell(2), r.getHeartRate());
                row.createCell(3).setCellValue(r.getBloodPressureDisplay());
                setNumericOrBlank(row.createCell(4), r.getBloodSugar());
                setNumericOrBlank(row.createCell(5), r.getSpo2());
                setNumericOrBlank(row.createCell(6), r.getWeightKg());
                setNumericOrBlank(row.createCell(7), r.getBmi());
                row.createCell(8).setCellValue(r.getNotes() != null ? r.getNotes() : "");
            }

            for (int i = 0; i < HEADERS.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                wb.write(fos);
            }
        }
    }

    /**
     * Appends a single new record as the next row of an existing per-user
     * workbook, creating the workbook + header if it doesn't exist yet.
     * This is what keeps a running, date-wise Excel log every time a new
     * reading is saved from the Health Records screen.
     */
    public static void appendHealthRecord(HealthRecord r, Path userWorkbookFile) throws IOException {
        Workbook wb;
        Sheet sheet;

        if (userWorkbookFile.toFile().exists()) {
            try (FileInputStream fis = new FileInputStream(userWorkbookFile.toFile())) {
                wb = new XSSFWorkbook(fis);
            }
            sheet = wb.getSheetAt(0);
        } else {
            wb = new XSSFWorkbook();
            sheet = wb.createSheet("Health Records");
            CellStyle headerStyle = headerStyle(wb);
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(HEADERS[i]);
                c.setCellStyle(headerStyle);
            }
        }

        int newRowIdx = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(newRowIdx);
        row.createCell(0).setCellValue(r.getRecordDate() != null ? r.getRecordDate().toString() : "");
        row.createCell(1).setCellValue(r.getRecordTime() != null ? r.getRecordTime().toString() : "");
        setNumericOrBlank(row.createCell(2), r.getHeartRate());
        row.createCell(3).setCellValue(r.getBloodPressureDisplay());
        setNumericOrBlank(row.createCell(4), r.getBloodSugar());
        setNumericOrBlank(row.createCell(5), r.getSpo2());
        setNumericOrBlank(row.createCell(6), r.getWeightKg());
        setNumericOrBlank(row.createCell(7), r.getBmi());
        row.createCell(8).setCellValue(r.getNotes() != null ? r.getNotes() : "");

        for (int i = 0; i < HEADERS.length; i++) sheet.autoSizeColumn(i);

        try (FileOutputStream fos = new FileOutputStream(userWorkbookFile.toFile())) {
            wb.write(fos);
        }
        wb.close();
    }

    private static void setNumericOrBlank(Cell cell, Number value) {
        if (value != null) cell.setCellValue(value.doubleValue());
    }

    private static CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}