package controller;

import config.Session;
import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField emailOrIdField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setText("");
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String identifier = emailOrIdField.getText() == null ? "" : emailOrIdField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showError("Please enter your ID/email and password.");
            return;
        }

        Optional<User> found = identifier.contains("@")
                ? userDAO.findByEmail(identifier)
                : userDAO.findByUserId(identifier);

        if (found.isEmpty()) {
            showError("No account found for \"" + identifier + "\".");
            return;
        }

        User user = found.get();
        boolean passwordOk;
        try {
            passwordOk = BCrypt.checkpw(password, user.getPasswordHash());
        } catch (Exception e) {
            passwordOk = false; // hash wasn't a valid bcrypt hash (e.g. placeholder seed row)
        }

        if (!passwordOk) {
            showError("Incorrect password.");
            return;
        }

        Session.login(user);
        goToDashboard(event);
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.showAndWait();
        }
    }

    private void goToDashboard(ActionEvent event) {
        try {
            Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 750));
            stage.setTitle("BioTrack - Dashboard");
        } catch (IOException e) {
            showError("Could not open dashboard: " + e.getMessage());
        }
    }
}