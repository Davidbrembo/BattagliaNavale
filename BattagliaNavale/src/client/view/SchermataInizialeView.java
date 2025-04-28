package client.view;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;

public class SchermataInizialeView extends Application {

    private static MediaPlayer mediaPlayer; // MediaPlayer statico
    private static boolean musicaInRiproduzione = false; // Flag statico per la musica

    @Override
    public void start(Stage primaryStage) {
        // Immagine di sfondo
        Image backgroundImage = new Image("file:resources/battaglia.jpg");
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setFitWidth(1920);
        backgroundImageView.setFitHeight(1080);

        // GIF di nebbia
        Image fogImage = new Image("file:resources/nebbia.gif");
        ImageView fogImageView = new ImageView(fogImage);
        fogImageView.setPreserveRatio(false);
        fogImageView.setFitWidth(1920);
        fogImageView.setFitHeight(1080);
        fogImageView.setOpacity(0.2);

        // Pulsanti
        Button startButton = new Button("Inizia Gioco");
        startButton.getStyleClass().add("button");

        Button optionsButton = new Button("Opzioni");
        optionsButton.getStyleClass().add("button");

        Button exitButton = new Button("Esci");
        exitButton.getStyleClass().add("button");

        // Applica l'effetto di pulsazione ai bottoni
        applyPulseEffect(startButton);
        applyPulseEffect(optionsButton);
        applyPulseEffect(exitButton);

        // Azioni dei pulsanti
        startButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                musicaInRiproduzione = false;
            }
            apriGioco(primaryStage);
        });

        optionsButton.setOnAction(e -> apriOpzioni(primaryStage));

        exitButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                musicaInRiproduzione = false;
            }
            primaryStage.close();
        });

        // Titolo
        Label titolo = new Label("BATTAGLIA NAVALE");
        titolo.getStyleClass().add("titolo");

        VBox buttonLayout = new VBox(20, startButton, optionsButton, exitButton);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox layout = new VBox(100, titolo, buttonLayout);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setTranslateY(100);

        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundImageView, fogImageView, layout);

        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());

        // Audio: gestisci la musica in modo statico
        if (mediaPlayer == null) {
            Media sound = new Media(new File("resources/audio_battaglia.mp3").toURI().toString());
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }

        if (!musicaInRiproduzione) {
            mediaPlayer.play();
            musicaInRiproduzione = true;
        }

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

    private void applyPulseEffect(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), button);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.15);
        st.setToY(1.15);
        st.setAutoReverse(true);
        st.setCycleCount(ScaleTransition.INDEFINITE);
        st.play();
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            musicaInRiproduzione = false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
