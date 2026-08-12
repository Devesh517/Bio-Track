package biotrack.ui;

import biotrack.dao.UserDAO;
import biotrack.model.User;
import biotrack.service.EmergencyCardGenerator;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class EmergencyCardView {

    public static Parent getView(User user) {
        VBox root = new VBox(14);
        root.getStyleClass().add("view-card");
        root.setPadding(new Insets(10));

        Label title = new Label("Emergency Health Card");
        title.getStyleClass().add("section-title");

        Label description = new Label(
                "A compact, wallet/ID-card sized PDF with your blood group, conditions, allergies, and " +
                        "emergency contact - meant to be printed and carried, separate from the full health report.");
        description.setWrapText(true);
        description.getStyleClass().add("muted-text");

        Label warn = null;
        if ((user.getConditions() == null || user.getConditions().isBlank())
                && (user.getAllergies() == null || user.getAllergies().isBlank())) {
            warn = new Label("Tip: add your conditions/allergies in Edit Profile so the card is actually useful in an emergency.");
            warn.getStyleClass().add("error-label");
            warn.setWrapText(true);
        }

        Label status = new Label();
        status.setWrapText(true);

        Button generateBtn = new Button("Download Emergency Card (PDF)");
        generateBtn.getStyleClass().add("primary-button");
        generateBtn.setOnAction(e -> {
            try {
                User fresh = new UserDAO().getUserById(user.getUserId());
                FileChooser chooser = new FileChooser();
                chooser.setInitialFileName("BioTrack_EmergencyCard_" + user.getUserId() + ".pdf");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File dest = chooser.showSaveDialog(MainApp.primaryStage);
                if (dest != null) {
                    EmergencyCardGenerator.generate(fresh, dest);
                    status.setText("Saved to " + dest.getAbsolutePath());
                }
            } catch (Exception ex) {
                status.setText("Failed to generate card: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(title, description);
        if (warn != null) root.getChildren().add(warn);
        root.getChildren().addAll(generateBtn, status);
        return root;
    }
}