package client.view;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import java.io.File;

public class SchermataInizialeView extends Application {

    private MediaPlayer mediaPlayer;

    @Override
    public void start(Stage primaryStage) {
        // Immagine di sfondo
        Image backgroundImage = new Image("file:resources/battaglia.jpg");
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setFitWidth(1920);
        backgroundImageView.setFitHeight(1080);

        // Pulsanti
        Button startButton = new Button("Inizia Gioco");
        startButton.getStyleClass().add("button");

        Button optionsButton = new Button("Opzioni");
        optionsButton.getStyleClass().add("button");

        Button exitButton = new Button("Esci");
        exitButton.getStyleClass().add("button");

        // Azioni dei pulsanti
        startButton.setOnAction(e -> {
            mediaPlayer.stop();
            apriGioco(primaryStage);
        });

        optionsButton.setOnAction(e -> {
        	apriOpzioni(primaryStage);
        });

        exitButton.setOnAction(e -> {
            mediaPlayer.stop();
            primaryStage.close();
        });

        // Layout dei pulsanti
        VBox buttonLayout = new VBox(20, startButton, optionsButton, exitButton);
        buttonLayout.setAlignment(Pos.CENTER);

        // StackPane per sovrapporre l'immagine di sfondo e i pulsanti
        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundImageView, buttonLayout);

        // Crea la scena
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());



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

    // Metodo per aprire la schermata delle opzioni
    private void apriOpzioni(Stage primaryStage) {
		OpzioniView opzioniView = new OpzioniView();
		try {
			opzioniView.start(primaryStage);
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
