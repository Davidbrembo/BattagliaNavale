package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Applicazione extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("JavaFX funziona!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 200);
        
        primaryStage.setTitle("Battaglia Navale");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
