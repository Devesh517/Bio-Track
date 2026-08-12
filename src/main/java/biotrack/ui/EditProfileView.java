package biotrack.ui;
import biotrack.dao.UserDAO;
import biotrack.model.User;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

public class EditProfileView {

    private static final long MAX_PICTURE_BYTES = 2 * 1024 * 1024; // 2 MB

    public static Parent getView(User user) {
        VBox root = new VBox(16);
        root.getStyleClass().add("view-card");
        root.setPadding(new Insets(10));

        Label title = new Label("Edit Profile");
        title.getStyleClass().add("section-title");

        // ---- Profile picture ---------------------------------------------
        ImageView pictureView = new ImageView();
        pictureView.setFitWidth(84);
        pictureView.setFitHeight(84);
        pictureView.setClip(new Circle(42, 42, 42));
        loadPicture(pictureView, user);

        Label pictureStatus = new Label();
        pictureStatus.getStyleClass().add("muted-text");
        pictureStatus.setWrapText(true);

        byte[][] pendingPicture = new byte[1][];
        String[] pendingPictureType = new String[1];

        Button choosePictureBtn = new Button("Change Photo");
        choosePictureBtn.getStyleClass().add("secondary-button");
        choosePictureBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File file = chooser.showOpenDialog(MainApp.primaryStage);
            if (file == null) return;
            try {
                if (file.length() > MAX_PICTURE_BYTES) {
                    pictureStatus.setText("Image is too large (max 2 MB).");
                    return;
                }
                String name = file.getName().toLowerCase();
                String type = name.endsWith(".png") ? "png" : (name.endsWith(".jpg") || name.endsWith(".jpeg")) ? "jpg" : null;
                if (type == null) {
                    pictureStatus.setText("Only PNG and JPG images are supported.");
                    return;
                }
                byte[] data = Files.readAllBytes(file.toPath());
                pendingPicture[0] = data;
                pendingPictureType[0] = type;
                pictureView.setImage(new Image(new ByteArrayInputStream(data)));
                pictureStatus.setText("New photo selected - click Save Changes to apply.");
            } catch (Exception ex) {
                pictureStatus.setText("Couldn't read that file: " + ex.getMessage());
            }
        });

        VBox pictureBox = new VBox(6, pictureView, choosePictureBtn, pictureStatus);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        TextField nameField = new TextField(user.getName());
        TextField ageField = new TextField(String.valueOf(user.getAge()));
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        genderBox.setValue(user.getGender());
        genderBox.setMaxWidth(Double.MAX_VALUE);

        TextField phoneField = new TextField(nullToEmpty(user.getPhone()));
        TextField emailField = new TextField(nullToEmpty(user.getEmail()));
        ComboBox<String> bloodGroupBox = new ComboBox<>();
        bloodGroupBox.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown");
        if (user.getBloodGroup() != null) bloodGroupBox.setValue(user.getBloodGroup());
        bloodGroupBox.setMaxWidth(Double.MAX_VALUE);
        TextField emergencyContactField = new TextField(nullToEmpty(user.getEmergencyContact()));
        TextField conditionsField = new TextField(nullToEmpty(user.getConditions()));
        conditionsField.setPromptText("e.g. Asthma, Type 2 Diabetes");
        TextField allergiesField = new TextField(nullToEmpty(user.getAllergies()));
        allergiesField.setPromptText("e.g. Penicillin, Peanuts");

        int r = 0;
        grid.addRow(r++, new Label("Name"), nameField);
        grid.addRow(r++, new Label("Age"), ageField);
        grid.addRow(r++, new Label("Gender"), genderBox);
        grid.addRow(r++, new Label("Phone"), phoneField);
        grid.addRow(r++, new Label("Email"), emailField);
        grid.addRow(r++, new Label("Blood Group"), bloodGroupBox);
        grid.addRow(r++, new Label("Emergency Contact"), emergencyContactField);
        grid.addRow(r++, new Label("Conditions"), conditionsField);
        grid.addRow(r, new Label("Allergies"), allergiesField);

        Label status = new Label();
        status.setWrapText(true);

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("primary-button");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");

        saveBtn.setOnAction(e -> {
            status.getStyleClass().remove("error-label");
            try {
                String name = nameField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                String gender = genderBox.getValue() == null ? "Other" : genderBox.getValue();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String bloodGroup = bloodGroupBox.getValue();
                String emergencyContact = emergencyContactField.getText().trim();
                String conditions = conditionsField.getText().trim();
                String allergies = allergiesField.getText().trim();

                if (name.isEmpty()) {
                    showError(status, "Name is required.");
                    return;
                }
                if (age < 0 || age > 130) {
                    showError(status, "Age must be between 0 and 130.");
                    return;
                }

                new UserDAO().updateProfile(user.getUserId(), name, age, gender, phone, email,
                        bloodGroup, emergencyContact, conditions, allergies);
                if (pendingPicture[0] != null) {
                    new UserDAO().updateProfilePicture(user.getUserId(), pendingPicture[0], pendingPictureType[0]);
                }
                User refreshed = new UserDAO().getUserById(user.getUserId());
                MainApp.setRoot(DashboardView.getView(refreshed));
            } catch (NumberFormatException ex) {
                showError(status, "Age must be a number.");
            } catch (Exception ex) {
                showError(status, "Failed to save changes: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> MainApp.setRoot(DashboardView.getView(user)));

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        HBox topRow = new HBox(20, pictureBox, grid);

        root.getChildren().addAll(title, topRow, buttons, status);
        return root;
    }

    private static void loadPicture(ImageView view, User user) {
        if (user.hasProfilePicture()) {
            try {
                view.setImage(new Image(new ByteArrayInputStream(user.getProfilePicture())));
                return;
            } catch (Exception ignored) { /* fall through to placeholder */ }
        }
        // No picture yet - leave the ImageView empty; the circular clip plus
        // the sidebar's initial-based placeholder is enough of a placeholder.
    }

    private static void showError(Label label, String message) {
        if (!label.getStyleClass().contains("error-label")) {
            label.getStyleClass().add("error-label");
        }
        label.setText(message);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}