package client.view;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.File;

public class SchermataInizialeView extends Application {

    private MediaPlayer mediaPlayer;

    @Override
    public void start(Stage primaryStage) {
        // Ottieni dimensioni dello schermo
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Immagine di sfondo
        Image image = new Image("file:resources/battaglia.jpg");
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.setFitWidth(screenWidth);
        imageView.setFitHeight(screenHeight);
        imageView.setSmooth(true);
        imageView.setCache(true);

        // Pulsante "Inizia Gioco"
        Button startButton = new Button("Inizia Gioco");
        startButton.setStyle("-fx-font-size: 24px; -fx-background-color: #00aaff; -fx-text-fill: white;");
        startButton.setPrefWidth(300);
        startButton.setPrefHeight(60);

        // Posizionamento pulsante
        startButton.setLayoutX((screenWidth - 300) / 2);
        startButton.setLayoutY(screenHeight - 200);

        // Usando AnchorPane
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(imageView, startButton);

        // Crea la scena
        Scene scene = new Scene(root, screenWidth, screenHeight);

        // Audio
        Media sound = new Media(new File("resources/audio_battaglia.mp3").toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();

        // Setup finestra
        primaryStage.setTitle("Battaglia Navale");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        // Azione al click sul bottone
        startButton.setOnAction(event -> {
            mediaPlayer.stop(); // Ferma la musica
            apriGioco(primaryStage);
        });
    }

    // Metodo per aprire la schermata di gioco
    private void apriGioco(Stage primaryStage) {
        GiocoView giocoView = new GiocoView();
        try {
            giocoView.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
