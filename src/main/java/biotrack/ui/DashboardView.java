package biotrack.ui;
import biotrack.dao.UserSettingsDAO;
import biotrack.model.User;
import biotrack.model.UserSettings;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;

public class DashboardView {

    public static Parent getView(User user) {
        BorderPane root = new BorderPane();

        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(210);

        VBox userBox = new VBox(2);
        Label userLabel = new Label(user.getName());
        userLabel.getStyleClass().add("sidebar-user");
        Label userIdLabel = new Label("ID: " + user.getUserId());
        userIdLabel.getStyleClass().add("sidebar-user-sub");
        userBox.getChildren().addAll(userLabel, userIdLabel);

        StackPane content = new StackPane();
        content.setPadding(new Insets(20));
        content.getChildren().add(OverviewView.getView(user));

        Button overviewBtn = navButton("Overview", () -> content.getChildren().setAll(OverviewView.getView(user)));
        Button editProfileBtn = navButton("Edit Profile", () -> content.getChildren().setAll(EditProfileView.getView(user)));
        Button addRecordBtn = navButton("Add Health Record", () -> content.getChildren().setAll(AddRecordView.getView(user)));
        Button viewRecordsBtn = navButton("View Records", () -> content.getChildren().setAll(ViewRecordsView.getView(user)));
        Button documentsBtn = navButton("Documents", () -> content.getChildren().setAll(DocumentsView.getView(user)));
        Button reportBtn = navButton("Weekly Report", () -> content.getChildren().setAll(ReportView.getView(user)));
        Button emergencyBtn = navButton("Emergency Card", () -> content.getChildren().setAll(EmergencyCardView.getView(user)));
        Button logoutBtn = navButton("Logout", () -> MainApp.setRoot(LoginView.getView()));

        ToggleButton themeToggle = new ToggleButton("Dark Mode");
        themeToggle.getStyleClass().add("nav-button");
        themeToggle.setMaxWidth(Double.MAX_VALUE);
        try {
            UserSettings settings = new UserSettingsDAO().getSettings(user.getUserId());
            themeToggle.setSelected(settings.isDark());
        } catch (Exception ignored) { /* default light */ }
        themeToggle.setOnAction(e -> ThemeManager.applyAndPersist(
                MainApp.primaryStage.getScene(), user.getUserId(), themeToggle.isSelected()));
        // Apply whatever the saved preference was as soon as the dashboard loads.
        if (themeToggle.isSelected()) {
            ThemeManager.apply(MainApp.primaryStage.getScene(), true);
        }

        sidebar.getChildren().addAll(userBox, new Separator(), overviewBtn, editProfileBtn,
                new Separator(), addRecordBtn, viewRecordsBtn, documentsBtn, reportBtn, emergencyBtn,
                new Separator(), themeToggle, logoutBtn);

        root.setLeft(sidebar);
        root.setCenter(content);
        return root;
    }

    private static Button navButton(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> action.run());
        return b;
    }
}