package biotrack.ui;

import biotrack.dao.HealthRecordDAO;
import biotrack.model.HealthRecord;
import biotrack.model.RecordFilter;
import biotrack.model.User;
import biotrack.service.ExcelExportService;
import biotrack.service.HealthAnalyzer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class ViewRecordsView {

    public static Parent getView(User user) {
        VBox root = new VBox(12);
        root.getStyleClass().add("view-card");
        root.setPadding(new Insets(10));

        Label title = new Label("Health Records - " + user.getName());
        title.getStyleClass().add("section-title");

        // ---- Search & filter bar ------------------------------------------------
        TextField keywordField = new TextField();
        keywordField.setPromptText("Search notes...");
        DatePicker fromPicker = new DatePicker();
        DatePicker toPicker = new DatePicker();
        ComboBox<String> valueFieldBox = new ComboBox<>(FXCollections.observableArrayList(
                "(none)", "bmi", "weight", "heart_rate", "sugar_level", "temperature"));
        valueFieldBox.setValue("(none)");
        TextField minField = new TextField();
        minField.setPromptText("min");
        minField.setPrefWidth(70);
        TextField maxField = new TextField();
        maxField.setPromptText("max");
        maxField.setPrefWidth(70);
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
                "Any status", "Normal", "Warning", "Critical"));
        statusBox.setValue("Any status");

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("primary-button");
        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("secondary-button");
        Button exportBtn = new Button("Export to Excel");
        exportBtn.getStyleClass().add("secondary-button");

        HBox filterRow1 = new HBox(8, new Label("Keyword:"), keywordField,
                new Label("From:"), fromPicker, new Label("To:"), toPicker);
        HBox filterRow2 = new HBox(8, new Label("Value field:"), valueFieldBox,
                minField, maxField, new Label("Status:"), statusBox, searchBtn, clearBtn, exportBtn);
        VBox filterBar = new VBox(6, filterRow1, filterRow2);
        filterBar.getStyleClass().add("filter-bar");

        Label status = new Label();
        status.getStyleClass().add("muted-text");

        TableView<HealthRecord> table = new TableView<>();
        table.getStyleClass().add("app-table");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        table.getColumns().addAll(
                col("Date", r -> sdf.format(r.getRecordedAt())),
                col("Weight (kg)", r -> String.valueOf(r.getWeight())),
                col("BMI", r -> String.format("%.1f", r.getBmi())),
                col("BP", r -> r.hasSystolicDiastolic() ? (r.getBpSystolic() + "/" + r.getBpDiastolic()) : "-"),
                col("Temp (°C)", r -> String.valueOf(r.getTemperature())),
                col("HR (bpm)", r -> String.valueOf(r.getHeartRate())),
                col("Sugar (mg/dL)", r -> r.getSugarLevel() + " (" + r.getSugarContext() + ")"),
                col("Status", r -> HealthAnalyzer.overallSeverity(r.getBmi(), r.getBpSystolic(), r.getBpDiastolic(),
                        r.getTemperature(), r.getHeartRate(), user.getAge(), r.getSugarLevel(), r.getSugarContext(),
                        r.getSpo2(), r.getCholesterolTotal()).toString())
        );

        Runnable[] refreshHolder = new Runnable[1];
        Runnable refresh = () -> {
            try {
                List<HealthRecord> records = new HealthRecordDAO().getRecordsByUser(user.getUserId());
                table.setItems(FXCollections.observableArrayList(records));
                status.setText(records.size() + " record(s).");
            } catch (Exception ex) {
                status.setText("Error loading records: " + ex.getMessage());
            }
        };
        refreshHolder[0] = refresh;
        refresh.run();

        searchBtn.setOnAction(e -> {
            try {
                RecordFilter filter = new RecordFilter();
                filter.keyword = keywordField.getText();
                LocalDate from = fromPicker.getValue();
                LocalDate to = toPicker.getValue();
                if (from != null && to != null) { filter.startDate = from; filter.endDate = to; }
                if (!"(none)".equals(valueFieldBox.getValue())) {
                    filter.valueField = valueFieldBox.getValue();
                    filter.valueMin = minField.getText().isBlank() ? null : Double.valueOf(minField.getText().trim());
                    filter.valueMax = maxField.getText().isBlank() ? null : Double.valueOf(maxField.getText().trim());
                }
                if (!"Any status".equals(statusBox.getValue())) {
                    filter.status = HealthAnalyzer.Severity.valueOf(statusBox.getValue().toUpperCase());
                }
                List<HealthRecord> results = new HealthRecordDAO().search(user.getUserId(), filter, user.getAge());
                table.setItems(FXCollections.observableArrayList(results));
                status.setText(results.size() + " record(s) match your filters.");
            } catch (NumberFormatException ex) {
                status.setText("Min/max must be numbers.");
            } catch (Exception ex) {
                status.setText("Search failed: " + ex.getMessage());
            }
        });

        clearBtn.setOnAction(e -> {
            keywordField.clear();
            fromPicker.setValue(null);
            toPicker.setValue(null);
            valueFieldBox.setValue("(none)");
            minField.clear();
            maxField.clear();
            statusBox.setValue("Any status");
            refreshHolder[0].run();
        });

        exportBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("BioTrack_Records_" + user.getUserId() + ".xlsx");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
            File dest = chooser.showSaveDialog(MainApp.primaryStage);
            if (dest != null) {
                try {
                    ExcelExportService.exportRecords(table.getItems(), user.getAge(), dest);
                    status.setText("Exported to " + dest.getAbsolutePath());
                } catch (Exception ex) {
                    status.setText("Export failed: " + ex.getMessage());
                }
            }
        });

        root.getChildren().addAll(title, filterBar, table, status);
        return root;
    }

    private static TableColumn<HealthRecord, String> col(String name, Function<HealthRecord, String> extractor) {
        TableColumn<HealthRecord, String> c = new TableColumn<>(name);
        c.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        return c;
    }
}