package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.FileUtil;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FileUtil.ensureDirectoriesExist();

        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        Scene scene = new Scene(root, 420, 520);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("BioTrack - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}