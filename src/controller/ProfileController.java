package controller;

import config.Session;
import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.User;

public class ProfileController {

    @FXML private Label userIdLabel;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField heightField;
    @FXML private Label statusLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        if (user == null) return;

        if (userIdLabel != null) userIdLabel.setText("User ID: " + user.getUserId());
        if (nameField != null) nameField.setText(user.getFullName());
        if (emailField != null) emailField.setText(user.getEmail());
        if (phoneField != null) phoneField.setText(user.getPhone());
        if (heightField != null) heightField.setText(String.valueOf(user.getHeightCm()));
    }

    @FXML
    public void handleSaveProfile() {
        User user = Session.getCurrentUser();
        if (user == null) return;

        user.setFullName(nameField.getText());
        user.setEmail(emailField.getText());
        user.setPhone(phoneField.getText());
        try {
            user.setHeightCm(Double.parseDouble(heightField.getText()));
        } catch (NumberFormatException ignored) { }

        boolean ok = userDAO.update(user);
        if (statusLabel != null) {
            statusLabel.setText(ok ? "Profile updated." : "Could not update profile.");
            statusLabel.setStyle(ok ? "-fx-text-fill: #22c55e;" : "-fx-text-fill: #ef4444;");
        }
    }
}