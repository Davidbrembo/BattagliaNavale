package client.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GiocoView extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Qui inizia il Gioco!");
        label.setStyle("-fx-font-size: 36px; -fx-text-fill: #003366;");

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Battaglia Navale - Gioco");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }
}