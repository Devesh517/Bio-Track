package biotrack.ui;
import biotrack.dao.HealthRecordDAO;
import biotrack.dao.UserSettingsDAO;
import biotrack.model.HealthRecord;
import biotrack.model.User;
import biotrack.model.UserSettings;
import biotrack.service.HealthAnalyzer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * Landing page shown right after login: profile snapshot, a "Recent Records"
 * list, and a "Trends" chart with a date-range selector. Both the recent
 * records and the chart load asynchronously (JavaFX Task on a background
 * thread) so a large record count never freezes the UI.
 */
public class OverviewView {

    public static Parent getView(User user) {
        VBox root = new VBox(20);
        root.getStyleClass().add("view-card");
        root.setPadding(new Insets(10));

        Label title = new Label("Welcome back, " + user.getName());
        title.getStyleClass().add("section-title");

        FlowPane profileGrid = new FlowPane();
        profileGrid.getStyleClass().add("profile-grid");
        profileGrid.setHgap(16);
        profileGrid.setVgap(16);
        profileGrid.getChildren().addAll(
                profileTile("Age", String.valueOf(user.getAge())),
                profileTile("Gender", user.getGender()),
                profileTile("Phone", emptyDash(user.getPhone())),
                profileTile("Email", emptyDash(user.getEmail())),
                profileTile("Blood Group", emptyDash(user.getBloodGroup())),
                profileTile("Emergency Contact", emptyDash(user.getEmergencyContact()))
        );

        // ---- Recent Records --------------------------------------------------
        Label recentTitle = new Label("Recent Records");
        recentTitle.getStyleClass().add("subsection-title");
        VBox recentBox = new VBox(8);
        ProgressIndicator recentSpinner = new ProgressIndicator();
        recentSpinner.setMaxSize(24, 24);
        recentBox.getChildren().add(recentSpinner);

        int recentN = safeRecentCount(user);
        Task<List<HealthRecord>> recentTask = new Task<>() {
            @Override protected List<HealthRecord> call() throws Exception {
                return new HealthRecordDAO().getRecentRecords(user.getUserId(), recentN);
            }
        };
        recentTask.setOnSucceeded(ev -> Platform.runLater(() -> renderRecent(recentBox, recentTask.getValue(), user)));
        recentTask.setOnFailed(ev -> Platform.runLater(() -> {
            recentBox.getChildren().setAll(new Label("Couldn't load recent records."));
        }));
        new Thread(recentTask, "recent-records-loader").start();

        // ---- Trends -------------------------------------------------------
        Label trendsTitle = new Label("Trends");
        trendsTitle.getStyleClass().add("subsection-title");

        ComboBox<String> rangeBox = new ComboBox<>(FXCollections.observableArrayList(
                "Last 7 days", "Last 30 days", "Last 90 days", "Custom"));
        rangeBox.setValue("Last 30 days");
        DatePicker fromPicker = new DatePicker(LocalDate.now().minusDays(30));
        DatePicker toPicker = new DatePicker(LocalDate.now());
        fromPicker.setDisable(true);
        toPicker.setDisable(true);
        ComboBox<String> metricBox = new ComboBox<>(FXCollections.observableArrayList(
                "Weight", "BMI", "Systolic BP", "Heart Rate", "Sugar Level"));
        metricBox.setValue("Weight");

        HBox rangeRow = new HBox(8, new Label("Range:"), rangeBox, fromPicker, toPicker, new Label("Metric:"), metricBox);

        StackPane chartArea = new StackPane();
        chartArea.setPrefHeight(260);

        Runnable[] loadTrend = new Runnable[1];
        loadTrend[0] = () -> {
            LocalDate end = LocalDate.now();
            LocalDate start = switch (rangeBox.getValue()) {
                case "Last 7 days" -> end.minusDays(7);
                case "Last 90 days" -> end.minusDays(90);
                case "Custom" -> fromPicker.getValue() != null ? fromPicker.getValue() : end.minusDays(30);
                default -> end.minusDays(30);
            };
            if ("Custom".equals(rangeBox.getValue()) && toPicker.getValue() != null) end = toPicker.getValue();

            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setMaxSize(24, 24);
            chartArea.getChildren().setAll(spinner);

            LocalDate finalStart = start, finalEnd = end;
            Task<List<HealthRecord>> trendTask = new Task<>() {
                @Override protected List<HealthRecord> call() throws Exception {
                    return new HealthRecordDAO().getRecordsBetween(user.getUserId(), finalStart, finalEnd);
                }
            };
            trendTask.setOnSucceeded(ev -> Platform.runLater(() ->
                    chartArea.getChildren().setAll(buildChart(trendTask.getValue(), metricBox.getValue()))));
            trendTask.setOnFailed(ev -> Platform.runLater(() ->
                    chartArea.getChildren().setAll(new Label("Couldn't load trend data."))));
            new Thread(trendTask, "trend-loader").start();
        };

        rangeBox.setOnAction(e -> {
            boolean custom = "Custom".equals(rangeBox.getValue());
            fromPicker.setDisable(!custom);
            toPicker.setDisable(!custom);
            loadTrend[0].run();
        });
        metricBox.setOnAction(e -> loadTrend[0].run());
        fromPicker.setOnAction(e -> { if ("Custom".equals(rangeBox.getValue())) loadTrend[0].run(); });
        toPicker.setOnAction(e -> { if ("Custom".equals(rangeBox.getValue())) loadTrend[0].run(); });
        loadTrend[0].run();

        Button editProfileBtn = new Button("Edit Profile");
        editProfileBtn.getStyleClass().add("primary-button");
        editProfileBtn.setOnAction(e -> MainApp.setRoot(EditProfileView.getView(user)));

        root.getChildren().addAll(title, profileGrid, recentTitle, recentBox,
                trendsTitle, rangeRow, chartArea, editProfileBtn);
        return root;
    }

    private static int safeRecentCount(User user) {
        try {
            UserSettings s = new UserSettingsDAO().getSettings(user.getUserId());
            return s.getRecentRecordsCount();
        } catch (Exception e) {
            return 5;
        }
    }

    private static void renderRecent(VBox recentBox, List<HealthRecord> records, User user) {
        recentBox.getChildren().clear();
        if (records.isEmpty()) {
            Label empty = new Label("No health records yet. Add your first record to see it here.");
            empty.getStyleClass().add("muted-text");
            recentBox.getChildren().add(empty);
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (HealthRecord r : records) {
            HealthAnalyzer.Severity sev = HealthAnalyzer.overallSeverity(r.getBmi(), r.getBpSystolic(), r.getBpDiastolic(),
                    r.getTemperature(), r.getHeartRate(), user.getAge(), r.getSugarLevel(), r.getSugarContext(),
                    r.getSpo2(), r.getCholesterolTotal());
            HBox card = new HBox(14);
            card.getStyleClass().add("record-card");
            card.setPadding(new Insets(8, 12, 8, 12));

            Label dot = new Label("\u25CF");
            dot.getStyleClass().add(AddRecordView.severityStyleClass(sev));

            String bp = r.hasSystolicDiastolic() ? (r.getBpSystolic() + "/" + r.getBpDiastolic() + " mmHg") : "BP n/a";
            Label summary = new Label(String.format("%s   Wt %.1fkg   BMI %.1f   %s   HR %d   Sugar %d",
                    sdf.format(r.getRecordedAt()), r.getWeight(), r.getBmi(), bp, r.getHeartRate(), r.getSugarLevel()));

            Label statusLabel = new Label(sev.toString());
            statusLabel.getStyleClass().add(AddRecordView.severityStyleClass(sev));

            card.getChildren().addAll(dot, summary, statusLabel);
            recentBox.getChildren().add(card);
        }
    }

    private static LineChart<String, Number> buildChart(List<HealthRecord> records, String metric) {
        NumberAxis yAxis = new NumberAxis();
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        for (HealthRecord r : records) {
            double value = switch (metric) {
                case "BMI" -> r.getBmi();
                case "Systolic BP" -> r.getBpSystolic() != null ? r.getBpSystolic() : 0;
                case "Heart Rate" -> r.getHeartRate();
                case "Sugar Level" -> r.getSugarLevel();
                default -> r.getWeight();
            };
            series.getData().add(new XYChart.Data<>(sdf.format(r.getRecordedAt()), value));
        }
        chart.getData().add(series);
        if (records.isEmpty()) {
            Label empty = new Label("No records in this range.");
            empty.getStyleClass().add("muted-text");
        }
        return chart;
    }

    private static VBox profileTile(String label, String value) {
        VBox tile = new VBox(4);
        tile.getStyleClass().add("profile-tile");
        Label labelNode = new Label(label.toUpperCase());
        labelNode.getStyleClass().add("profile-tile-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("profile-tile-value");
        tile.getChildren().addAll(labelNode, valueNode);
        return tile;
    }

    private static String emptyDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}