package controller;

import config.Session;
import dao.HealthRecordDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import model.HealthRecord;
import service.ReportService;

import java.nio.file.Path;
import java.util.List;

public class ReportController {

    @FXML private ComboBox<String> formatComboBox;
    @FXML private ComboBox<String> rangeComboBox; // "Last 7 days", "Last 30 days", "All time"
    @FXML private Label statusLabel;

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        if (formatComboBox != null) {
            formatComboBox.getItems().setAll("PDF", "Word", "TXT", "Excel");
            formatComboBox.getSelectionModel().selectFirst();
        }
        if (rangeComboBox != null) {
            rangeComboBox.getItems().setAll("Last 7 days", "Last 30 days", "All time");
            rangeComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleGenerateReport() {
        if (Session.getCurrentUser() == null) {
            setStatus("Please log in first.", true);
            return;
        }

        String userId = Session.getCurrentUserId();
        List<HealthRecord> records = switch (safeSelection(rangeComboBox, "Last 7 days")) {
            case "Last 30 days" -> healthRecordDAO.findLastNDays(userId, 30);
            case "All time" -> healthRecordDAO.findAllByUser(userId);
            default -> healthRecordDAO.findLastNDays(userId, 7);
        };

        if (records.isEmpty()) {
            setStatus("No health records found for the selected range.", true);
            return;
        }

        ReportService.Format format = switch (safeSelection(formatComboBox, "PDF")) {
            case "Word" -> ReportService.Format.WORD;
            case "TXT" -> ReportService.Format.TXT;
            case "Excel" -> ReportService.Format.EXCEL;
            default -> ReportService.Format.PDF;
        };

        try {
            Path file = reportService.generateHealthReport(Session.getCurrentUser(), records, format);
            setStatus("Report generated: " + file.toAbsolutePath(), false);
        } catch (Exception e) {
            setStatus("Failed to generate report: " + e.getMessage(), true);
        }
    }

    private String safeSelection(ComboBox<String> box, String fallback) {
        if (box == null || box.getSelectionModel().getSelectedItem() == null) return fallback;
        return box.getSelectionModel().getSelectedItem();
    }

    private void setStatus(String message, boolean isError) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #22c55e;");
        } else {
            new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION, message).showAndWait();
        }
    }
}