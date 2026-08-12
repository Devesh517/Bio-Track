package biotrack.ui;
import biotrack.dao.HealthRecordDAO;
import biotrack.dao.UserDAO;
import biotrack.model.User;
import biotrack.model.HealthRecord;
import biotrack.service.ReportGenerator;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

public class ReportView {

    public static Parent getView(User user) {
        VBox root = new VBox(16);
        root.getStyleClass().add("view-card");
        root.setPadding(new Insets(10));

        Label title = new Label("Generate Weekly Report");
        title.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        DatePicker startPicker = new DatePicker(LocalDate.now().minusDays(6));
        DatePicker endPicker = new DatePicker(LocalDate.now());
        CheckBox includeChart = new CheckBox("Include weight trend chart");
        includeChart.setSelected(true);

        grid.addRow(0, new Label("From"), startPicker);
        grid.addRow(1, new Label("To"), endPicker);
        grid.addRow(2, new Label(""), includeChart);

        Label status = new Label();
        status.setWrapText(true);

        Button generateBtn = new Button("Generate PDF Report");
        generateBtn.getStyleClass().add("primary-button");
        generateBtn.setOnAction(e -> {
            try {
                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();
                if (start == null || end == null || start.isAfter(end)) {
                    status.setText("Please select a valid date range.");
                    return;
                }
                List<HealthRecord> records = new HealthRecordDAO().getRecordsBetween(user.getUserId(), start, end);
                User freshUser = new UserDAO().getUserById(user.getUserId());

                FileChooser chooser = new FileChooser();
                chooser.setInitialFileName("BioTrack_Report_" + user.getUserId() + "_" + start + "_to_" + end + ".pdf");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File dest = chooser.showSaveDialog(MainApp.primaryStage);
                if (dest != null) {
                    BufferedImage chartImage = (includeChart.isSelected() && !records.isEmpty())
                            ? renderChartImage(records) : null;
                    ReportGenerator.generateWeeklyReport(freshUser, records, start, end, chartImage, dest);
                    status.setText("Report saved to " + dest.getAbsolutePath() +
                            " (" + records.size() + " records included).");
                }
            } catch (Exception ex) {
                status.setText("Failed to generate report: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(title, grid, generateBtn, status);
        return root;
    }

    /**
     * Builds a small off-screen weight-trend LineChart and rasterizes it with
     * Node.snapshot() so it can be embedded as an image in the PDF - this app
     * doesn't have a headless charting library, so we reuse the same JavaFX
     * chart the on-screen Trends view already renders with.
     */
    private static BufferedImage renderChartImage(List<HealthRecord> records) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setPrefSize(480, 260);
        chart.setTitle("Weight (kg) Over Time");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        for (HealthRecord r : records) {
            series.getData().add(new XYChart.Data<>(sdf.format(r.getRecordedAt()), r.getWeight()));
        }
        chart.getData().add(series);

        // Off-screen scene so we can snapshot without showing a window.
        javafx.scene.Scene scene = new javafx.scene.Scene(chart, 480, 260);
        chart.applyCss();
        chart.layout();
        WritableImage snapshot = chart.snapshot(new SnapshotParameters(), null);
        return SwingFXUtils.fromFXImage(snapshot, null);
    }
}