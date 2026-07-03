package service;

import config.Constants;
import model.HealthRecord;
import utils.ExcelUtil;
import utils.FileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles writing HealthRecord data out to Excel - both a full re-export
 * and an incremental "append on save" log kept per user
 * (BioTrackData/excel/{userId}.xlsx).
 */
public class ExportService {

    public Path exportToExcel(String userId, List<HealthRecord> records, String fileName) throws IOException {
        FileUtil.ensureDirectoriesExist();
        Path out = Paths.get(Constants.EXCEL_DIR, fileName);
        ExcelUtil.writeHealthRecords(records, userId, out);
        return out;
    }

    /** Appends the just-saved record to that user's running Excel log. Call this every time a record is saved. */
    public Path appendToUserExcelLog(String userId, HealthRecord record) throws IOException {
        FileUtil.ensureDirectoriesExist();
        Path userLog = Paths.get(Constants.EXCEL_DIR, userId + "_health_log.xlsx");
        ExcelUtil.appendHealthRecord(record, userLog);
        return userLog;
    }

    public Path getUserExcelLogPath(String userId) {
        return Paths.get(Constants.EXCEL_DIR, userId + "_health_log.xlsx");
    }
}