package controller;

import config.Session;
import dao.DocumentDAO;
import dao.HealthRecordDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.Document;
import model.HealthRecord;
import service.AnalysisService;

import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label heartRateValue;
    @FXML private Label bpValue;
    @FXML private Label sugarValue;
    @FXML private Label spo2Value;
    @FXML private Label weightValue;
    @FXML private Label bmiValue;
    @FXML private Label overallStatusValue;

    @FXML private LineChart<String, Number> heartRateChart;
    @FXML private ListView<String> recentRecordsList;
    @FXML private ListView<String> recentDocumentsList;

    private final HealthRecordDAO healthRecordDAO = new HealthRecordDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final AnalysisService analysisService = new AnalysisService();

    @FXML
    public void initialize() {
        String userId = Session.getCurrentUserId();
        if (userId == null) return;

        if (welcomeLabel != null && Session.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome back, " + Session.getCurrentUser().getFullName() + " \uD83D\uDC4B");
        }

        healthRecordDAO.findLatest(userId).ifPresent(this::showLatest);

        List<HealthRecord> lastWeek = healthRecordDAO.findLastNDays(userId, 7);
        buildChart(lastWeek);

        AnalysisService.Summary summary = analysisService.summarize(lastWeek);
        if (overallStatusValue != null) overallStatusValue.setText(summary.overallStatus.getLabel());

        if (recentRecordsList != null) {
            List<String> rows = lastWeek.stream()
                    .sorted((a, b) -> b.getRecordDate().compareTo(a.getRecordDate()))
                    .limit(5)
                    .map(r -> r.getRecordDate() + "  |  HR " + nullSafe(r.getHeartRate(), "bpm")
                            + "  |  BP " + r.getBloodPressureDisplay())
                    .collect(Collectors.toList());
            recentRecordsList.getItems().setAll(rows);
        }

        if (recentDocumentsList != null) {
            List<Document> docs = documentDAO.findRecent(userId, 5);
            recentDocumentsList.getItems().setAll(
                    docs.stream().map(d -> d.getFileName() + "  (" + d.getCategory() + ")").collect(Collectors.toList())
            );
        }
    }

    private void showLatest(HealthRecord r) {
        if (heartRateValue != null) heartRateValue.setText(nullSafe(r.getHeartRate(), "bpm"));
        if (bpValue != null) bpValue.setText(r.getBloodPressureDisplay());
        if (sugarValue != null) sugarValue.setText(nullSafe(r.getBloodSugar(), "mg/dL"));
        if (spo2Value != null) spo2Value.setText(nullSafe(r.getSpo2(), "%"));
        if (weightValue != null) weightValue.setText(nullSafe(r.getWeightKg(), "kg"));
        if (bmiValue != null) bmiValue.setText(r.getBmi() == null ? "-" : String.valueOf(r.getBmi()));
    }

    private void buildChart(List<HealthRecord> records) {
        if (heartRateChart == null) return;
        heartRateChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Heart Rate (bpm)");
        records.stream()
                .sorted((a, b) -> a.getRecordDate().compareTo(b.getRecordDate()))
                .forEach(r -> {
                    if (r.getHeartRate() != null) {
                        series.getData().add(new XYChart.Data<>(r.getRecordDate().toString(), r.getHeartRate()));
                    }
                });
        heartRateChart.getData().add(series);
    }

    private String nullSafe(Number value, String unit) {
        return value == null ? "-" : value + " " + unit;
    }
}