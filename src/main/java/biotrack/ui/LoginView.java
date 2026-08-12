package biotrack.ui;
import biotrack.dao.UserDAO;
import biotrack.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class LoginView {

    public static Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("auth-screen");
        root.setPadding(new Insets(30));

        Label title = new Label("BioTrack");
        title.setFont(Font.font("Arial", 32));
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Your personal health record, in one place.");
        subtitle.getStyleClass().add("app-subtitle");

        VBox header = new VBox(4, title, subtitle);
        header.setAlignment(Pos.CENTER);
        root.setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 30, 0));

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("auth-tabs");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab loginTab = new Tab("Login with User ID", buildLoginForm());
        Tab registerTab = new Tab("Register New User", buildRegisterForm());
        tabPane.getTabs().addAll(loginTab, registerTab);

        StackPane center = new StackPane(tabPane);
        center.getStyleClass().add("auth-card");
        center.setMaxWidth(440);
        root.setCenter(center);

        return root;
    }

    private static VBox buildLoginForm() {
        VBox box = new VBox(12);
        box.getStyleClass().add("auth-form");
        box.setPadding(new Insets(20));

        TextField idField = new TextField();
        idField.setPromptText("Enter your User ID");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label error = new Label();
        error.getStyleClass().add("error-label");
        error.setWrapText(true);

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("primary-button");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            error.setText("");
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String password = passwordField.getText();
                if (password.isEmpty()) {
                    error.setText("Please enter your password.");
                    return;
                }
                User user = new UserDAO().verifyLogin(id, password);
                if (user == null) {
                    // Deliberately generic: don't reveal whether the ID or the password was wrong.
                    error.setText("Invalid User ID or password.");
                } else {
                    MainApp.setRoot(DashboardView.getView(user));
                }
            } catch (NumberFormatException ex) {
                error.setText("Please enter a valid numeric ID.");
            } catch (Exception ex) {
                error.setText("Something went wrong. Please try again.");
            }
        });

        box.getChildren().addAll(new Label("User ID"), idField, new Label("Password"), passwordField,
                loginBtn, error);
        return box;
    }

    private static VBox buildRegisterForm() {
        VBox box = new VBox(12);
        box.getStyleClass().add("auth-form");
        box.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        TextField ageField = new TextField();
        ageField.setPromptText("Age");
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        genderBox.setPromptText("Gender");
        genderBox.setMaxWidth(Double.MAX_VALUE);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");
        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        ComboBox<String> bloodGroupBox = new ComboBox<>();
        bloodGroupBox.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown");
        bloodGroupBox.setPromptText("Blood group (optional)");
        bloodGroupBox.setMaxWidth(Double.MAX_VALUE);
        TextField emergencyContactField = new TextField();
        emergencyContactField.setPromptText("Emergency contact name & phone (optional)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min. 8 characters)");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");

        Label result = new Label();
        result.setWrapText(true);

        Button registerBtn = new Button("Create Account");
        registerBtn.getStyleClass().add("primary-button");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> {
            result.getStyleClass().remove("error-label");
            try {
                String name = nameField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                String gender = genderBox.getValue() == null ? "Other" : genderBox.getValue();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String bloodGroup = bloodGroupBox.getValue();
                String emergencyContact = emergencyContactField.getText().trim();
                String password = passwordField.getText();
                String confirm = confirmField.getText();

                if (name.isEmpty()) {
                    showError(result, "Name is required.");
                    return;
                }
                if (password.length() < 8) {
                    showError(result, "Password must be at least 8 characters.");
                    return;
                }
                if (!password.equals(confirm)) {
                    showError(result, "Passwords do not match.");
                    return;
                }

                int newId = new UserDAO().insertUser(name, age, gender, phone, email,
                        bloodGroup, emergencyContact, password);
                if (newId != -1) {
                    result.getStyleClass().remove("error-label");
                    result.setText("Account created! Your User ID is " + newId +
                            ". Save this ID — you'll need it, along with your password, to log in.");
                } else {
                    showError(result, "Failed to create account.");
                }
            } catch (NumberFormatException ex) {
                showError(result, "Age must be a number.");
            } catch (Exception ex) {
                showError(result, "Database error: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(
                new Label("Name"), nameField,
                new Label("Age"), ageField,
                new Label("Gender"), genderBox,
                new Label("Phone"), phoneField,
                new Label("Email"), emailField,
                new Label("Blood Group"), bloodGroupBox,
                new Label("Emergency Contact"), emergencyContactField,
                new Label("Password"), passwordField,
                new Label("Confirm Password"), confirmField,
                registerBtn, result);

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("auth-scroll");
        VBox wrapper = new VBox(scroll);
        return wrapper;
    }

    private static void showError(Label label, String message) {
        if (!label.getStyleClass().contains("error-label")) {
            label.getStyleClass().add("error-label");
        }
        label.setText(message);
    }
}