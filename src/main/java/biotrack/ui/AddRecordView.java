package biotrack.ui;
import biotrack.dao.HealthRecordDAO;
import biotrack.model.User;
import biotrack.service.HealthAnalyzer;
import biotrack.service.Validators;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AddRecordView {

    public static Parent getView(User user) {
        VBox root = new VBox(16);
        root.getStyleClass().add("view-card");
        root.setPadding(new Insets(10));

        Label title = new Label("Add Health Record");
        title.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        TextField weightField = new TextField();
        weightField.setTextFormatter(Validators.nonNegativeDecimalFormatter());
        TextField heightField = new TextField();
        heightField.setPromptText("in meters, e.g. 1.72");
        heightField.setTextFormatter(Validators.nonNegativeDecimalFormatter());
        TextField tempField = new TextField();
        tempField.setTextFormatter(Validators.nonNegativeDecimalFormatter());
        TextField systolicField = new TextField();
        systolicField.setPromptText("Systolic");
        systolicField.setTextFormatter(Validators.nonNegativeIntegerFormatter());
        TextField diastolicField = new TextField();
        diastolicField.setPromptText("Diastolic");
        diastolicField.setTextFormatter(Validators.nonNegativeIntegerFormatter());
        TextField hrField = new TextField();
        hrField.setTextFormatter(Validators.nonNegativeIntegerFormatter());
        TextField sugarField = new TextField();
        sugarField.setTextFormatter(Validators.nonNegativeIntegerFormatter());
        ComboBox<String> sugarContextBox = new ComboBox<>();
        sugarContextBox.getItems().addAll("Fasting", "Post-meal");
        sugarContextBox.setValue("Fasting");
        TextField spo2Field = new TextField();
        spo2Field.setPromptText("optional");
        spo2Field.setTextFormatter(Validators.nonNegativeDecimalFormatter());
        TextField cholesterolField = new TextField();
        cholesterolField.setPromptText("optional, total mg/dL");
        cholesterolField.setTextFormatter(Validators.nonNegativeIntegerFormatter());
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        int r = 0;
        grid.addRow(r++, new Label("Weight (kg)"), weightField, refLabel("0 – " + (int) Validators.WEIGHT_MAX_KG + " kg"));
        grid.addRow(r++, new Label("Height (m)"), heightField, refLabel("0 – " + Validators.HEIGHT_MAX_M + " m"));
        grid.addRow(r++, new Label("Body Temperature (°C)"), tempField, refLabel("ref: 36.1–37.2 °C"));
        grid.addRow(r++, new Label("Blood Pressure (mmHg)"), new HBox(6, systolicField, new Label("/"), diastolicField), refLabel("ref: <120 / <80"));
        grid.addRow(r++, new Label("Heart Rate (bpm)"), hrField, refLabel("ref: 60–100 (adult resting)"));
        grid.addRow(r++, new Label("Sugar Level (mg/dL)"), new HBox(6, sugarField, sugarContextBox), refLabel("ref: 70–99 fasting / <140 post-meal"));
        grid.addRow(r++, new Label("SpO2 (%)"), spo2Field, refLabel("ref: 95–100%"));
        grid.addRow(r++, new Label("Total Cholesterol (mg/dL)"), cholesterolField, refLabel("ref: <200 mg/dL"));
        grid.addRow(r, new Label("Notes"), notesArea);

        VBox resultBox = new VBox(4);
        resultBox.getStyleClass().add("analysis-box");

        Button saveBtn = new Button("Save Record");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setOnAction(e -> {
            errorLabel.setText("");
            resultBox.getChildren().clear();
            try {
                double weight = Validators.requireRange("Weight", parseD(weightField, "Weight"), 0.1, Validators.WEIGHT_MAX_KG);
                double height = Validators.requireRange("Height", parseD(heightField, "Height"), 0.3, Validators.HEIGHT_MAX_M);
                double temp = Validators.requireRange("Temperature", parseD(tempField, "Temperature"), Validators.TEMP_MIN_C, Validators.TEMP_MAX_C);
                int systolic = Validators.requireRange("Systolic BP", parseI(systolicField, "Systolic BP"), Validators.BP_MIN, Validators.BP_MAX);
                int diastolic = Validators.requireRange("Diastolic BP", parseI(diastolicField, "Diastolic BP"), Validators.BP_MIN, Validators.BP_MAX);
                int hr = Validators.requireRange("Heart rate", parseI(hrField, "Heart rate"), Validators.HR_MIN, Validators.HR_MAX);
                int sugar = Validators.requireRange("Sugar level", parseI(sugarField, "Sugar level"), Validators.SUGAR_MIN, Validators.SUGAR_MAX);
                String sugarContext = "Post-meal".equals(sugarContextBox.getValue())
                        ? HealthAnalyzer.SUGAR_CONTEXT_POST_MEAL : HealthAnalyzer.SUGAR_CONTEXT_FASTING;

                Double spo2 = spo2Field.getText().isBlank() ? null :
                        Validators.requireRange("SpO2", Double.parseDouble(spo2Field.getText().trim()), Validators.SPO2_MIN, Validators.SPO2_MAX);
                Integer cholesterol = cholesterolField.getText().isBlank() ? null :
                        Validators.requireRange("Cholesterol", Integer.parseInt(cholesterolField.getText().trim()), Validators.CHOLESTEROL_MIN, Validators.CHOLESTEROL_MAX);
                String notes = notesArea.getText();

                double bmi = HealthAnalyzer.calculateBMI(weight, height);
                new HealthRecordDAO().insertRecord(user.getUserId(), weight, height, bmi, temp,
                        systolic, diastolic, hr, sugar, sugarContext, spo2, cholesterol, notes);

                showResult(resultBox, bmi, systolic, diastolic, temp, hr, user.getAge(), sugar, sugarContext, spo2, cholesterol);
            } catch (Validators.ValidationException ex) {
                errorLabel.setText(ex.getMessage());
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please fill all numeric fields correctly.");
            } catch (Exception ex) {
                errorLabel.setText("Error saving record: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(title, grid, saveBtn, errorLabel, resultBox);
        return root;
    }

    private static Label refLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted-text");
        return l;
    }

    private static double parseD(TextField f, String name) throws Validators.ValidationException {
        String t = f.getText() == null ? "" : f.getText().trim();
        if (t.isEmpty()) throw new Validators.ValidationException(name + " is required.");
        return Double.parseDouble(t);
    }

    private static int parseI(TextField f, String name) throws Validators.ValidationException {
        String t = f.getText() == null ? "" : f.getText().trim();
        if (t.isEmpty()) throw new Validators.ValidationException(name + " is required.");
        return Integer.parseInt(t);
    }

    private static void showResult(VBox box, double bmi, int systolic, int diastolic, double temp, int hr,
                                   int age, int sugar, String sugarContext, Double spo2, Integer cholesterol) {
        Label saved = new Label(String.format("Saved! BMI: %.1f", bmi));
        saved.getStyleClass().add("subsection-title");
        box.getChildren().add(saved);
        badge(box, "BMI", HealthAnalyzer.classifyBMI(bmi));
        badge(box, "Blood Pressure", HealthAnalyzer.classifyBloodPressure(systolic, diastolic));
        badge(box, "Temperature", HealthAnalyzer.classifyTemperature(temp));
        badge(box, "Heart Rate", HealthAnalyzer.classifyHeartRate(hr, age));
        badge(box, "Blood Sugar", HealthAnalyzer.classifyGlucose(sugar, sugarContext));
        if (spo2 != null) badge(box, "SpO2", HealthAnalyzer.classifySpo2(spo2));
        if (cholesterol != null) badge(box, "Cholesterol", HealthAnalyzer.classifyCholesterol(cholesterol));
    }

    static void badge(VBox box, String vital, HealthAnalyzer.Classification c) {
        Label l = new Label(vital + ": " + c.label + "  (ref: " + c.referenceRange + ")");
        l.getStyleClass().add(severityStyleClass(c.severity));
        box.getChildren().add(l);
    }

    /**
     * Style class for a severity level, defined in theme.css against the
     * -success/-warning/-error palette variables so the color adapts to
     * light/dark mode. (Previously this returned a hardcoded Color used via
     * setTextFill(), which always overrides CSS and never adapted for dark
     * mode - that's why these badges went unreadable on dark backgrounds.)
     */
    static String severityStyleClass(HealthAnalyzer.Severity s) {
        return switch (s) {
            case NORMAL -> "severity-normal";
            case WARNING -> "severity-warning";
            case CRITICAL -> "severity-critical";
        };
    }
}