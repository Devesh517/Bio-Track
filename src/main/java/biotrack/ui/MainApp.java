package biotrack.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("BioTrack - Personal Health Record System");
        Scene scene = new Scene(LoginView.getView(), 900, 600);

        // Split stylesheets: a base theme/palette, shared component styles,
        // then screen-specific chrome. Order matters - later files can
        // override earlier ones for the same selector.
        scene.getStylesheets().addAll(
                requireStylesheet("/css/theme.css"),
                requireStylesheet("/css/components.css"),
                requireStylesheet("/css/dashboard.css"),
                requireStylesheet("/css/login.css")
        );

        stage.setScene(scene);
        stage.setMinWidth(850);
        stage.setMinHeight(550);
        stage.show();
    }

    /**
     * Loads a stylesheet from the classpath and fails with a clear,
     * actionable error message instead of a bare NullPointerException
     * if the file can't be found. This tells you exactly which file is
     * missing and where Java looked for it.
     */
    private String requireStylesheet(String path) {
        var url = getClass().getResource(path);
        if (url == null) {
            throw new IllegalStateException(
                    "Missing stylesheet on classpath: " + path +
                            " (expected at main/resources" + path + " - " +
                            "check the file exists there and the filename/case matches exactly, " +
                            "then do a clean rebuild)"
            );
        }
        return url.toExternalForm();
    }

    public static void setRoot(javafx.scene.Parent root) {
        primaryStage.getScene().setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}