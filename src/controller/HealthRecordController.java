package controller;

import config.Session;
import dao.HealthRecordDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.HealthRecord;
import service.ExportService;
import service.ValidationService;
import utils.BMIUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class HealthRecordController {

    @FXML private DatePicker datePicker;
    @FXML private TextField heartRateField;
    @FXML private TextField systolicField;
    @FXML private TextField diastolicField;
    @FXML private TextField sugarField;
    @FXML private TextField spo2Field;
    @FXML private TextField weightField;
    @FXML private TextArea notesField;
    @FXML private Label statusLabel;

    @FXML private TableView<HealthRecord> recordsTable;
    @FXML private TableColumn<HealthRecord, LocalDate> dateColumn;
    @FXML private TableColumn<HealthRecord, Integer> heartRateColumn;
    @FXML private TableColumn<HealthRecord, String> bpColumn;
    @FXML private TableColumn<HealthRecord, Double> sugarColumn;
    @FXML private TableColumn<HealthRecord, Double> spo2Column;
    @FXML private TableColumn<HealthRecord, Double> weightColumn;
    @FXML private TableColumn<HealthRecord, Double> bmiColumn;

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final ValidationService validationService = new ValidationService();
    private final ExportService exportService = new ExportService();

    @FXML
    public void initialize() {
        if (datePicker != null) datePicker.setValue(LocalDate.now());

        if (recordsTable != null) {
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("recordDate"));
            heartRateColumn.setCellValueFactory(new PropertyValueFactory<>("heartRate"));
            bpColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getBloodPressureDisplay()));
            sugarColumn.setCellValueFactory(new PropertyValueFactory<>("bloodSugar"));
            spo2Column.setCellValueFactory(new PropertyValueFactory<>("spo2"));
            weightColumn.setCellValueFactory(new PropertyValueFactory<>("weightKg"));
            bmiColumn.setCellValueFactory(new PropertyValueFactory<>("bmi"));
            loadRecords();
        }
    }

    private void loadRecords() {
        String userId = Session.getCurrentUserId();
        if (userId == null) return;
        List<HealthRecord> records = healthRecordDAO.findAllByUser(userId);
        ObservableList<HealthRecord> data = FXCollections.observableArrayList(records);
        recordsTable.setItems(data);
    }

    @FXML
    public void handleSaveRecord() {
        String userId = Session.getCurrentUserId();
        if (userId == null) {
            setStatus("You must be logged in to save a record.", true);
            return;
        }

        HealthRecord r = new HealthRecord(userId, datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now());
        r.setRecordTime(LocalTime.now());
        r.setHeartRate(parseInt(heartRateField.getText()));
        r.setBpSystolic(parseInt(systolicField.getText()));
        r.setBpDiastolic(parseInt(diastolicField.getText()));
        r.setBloodSugar(parseDouble(sugarField.getText()));
        r.setSpo2(parseDouble(spo2Field.getText()));
        r.setWeightKg(parseDouble(weightField.getText()));
        r.setNotes(notesField != null ? notesField.getText() : null);

        // Auto-calculate BMI if we have both weight and the user's stored height.
        if (r.getWeightKg() != null && Session.getCurrentUser() != null && Session.getCurrentUser().getHeightCm() > 0) {
            r.setBmi(BMIUtil.calculate(r.getWeightKg(), Session.getCurrentUser().getHeightCm()));
        }

        List<String> errors = validationService.validate(r);
        if (!errors.isEmpty()) {
            setStatus(String.join(" ", errors), true);
            return;
        }

        int id = healthRecordDAO.create(r);
        if (id == -1) {
            setStatus("Could not save the record. Please try again.", true);
            return;
        }

        // Keep the per-user, date-wise Excel log up to date automatically.
        try {
            exportService.appendToUserExcelLog(userId, r);
        } catch (Exception e) {
            System.err.println("[HealthRecordController] Excel log append failed: " + e.getMessage());
        }

        setStatus("Record saved successfully.", false);
        clearForm();
        loadRecords();
    }

    @FXML
    public void handleDeleteSelected() {
        HealthRecord selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a record to delete.", true);
            return;
        }
        healthRecordDAO.delete(selected.getId(), Session.getCurrentUserId());
        loadRecords();
    }

    private void clearForm() {
        heartRateField.clear();
        systolicField.clear();
        diastolicField.clear();
        sugarField.clear();
        spo2Field.clear();
        weightField.clear();
        if (notesField != null) notesField.clear();
        datePicker.setValue(LocalDate.now());
    }

    private void setStatus(String message, boolean isError) {
        if (statusLabel == null) return;
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #22c55e;");
    }

    private Integer parseInt(String text) {
        try { return (text == null || text.isBlank()) ? null : Integer.parseInt(text.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private Double parseDouble(String text) {
        try { return (text == null || text.isBlank()) ? null : Double.parseDouble(text.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}