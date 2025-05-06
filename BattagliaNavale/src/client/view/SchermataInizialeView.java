package client.view;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import utility.Impostazioni;
import utility.ImpostazioniManager;
import server.model.ServerGameManager;

import java.io.File;

public class SchermataInizialeView extends Application {

    private static MediaPlayer mediaPlayer;
    private static boolean musicaInRiproduzione = false;
    private StackPane root; // usato per applicare la luminosità

    @Override
    public void start(Stage primaryStage) {
        Image backgroundImage = new Image("file:resources/battaglia.jpg");
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setFitWidth(1920);
        backgroundImageView.setFitHeight(1080);

        Image fogImage = new Image("file:resources/nebbia.gif");
        ImageView fogImageView = new ImageView(fogImage);
        fogImageView.setPreserveRatio(false);
        fogImageView.setFitWidth(1920);
        fogImageView.setFitHeight(1080);
        fogImageView.setOpacity(0.2);

        Button startButton = new Button("Inizia Gioco");
        Button optionsButton = new Button("Opzioni");
        Button exitButton = new Button("Esci");

        startButton.getStyleClass().add("button");
        optionsButton.getStyleClass().add("button");
        exitButton.getStyleClass().add("button");

        applyPulseEffect(startButton);
        applyPulseEffect(optionsButton);
        applyPulseEffect(exitButton);

        startButton.setOnAction(e -> {
            stopMusica();
            apriGioco(primaryStage);
        });

        optionsButton.setOnAction(e -> apriOpzioni(primaryStage));
        exitButton.setOnAction(e -> {
            stopMusica();
            primaryStage.close();
        });

        Label titolo = new Label("BATTAGLIA NAVALE");
        titolo.getStyleClass().add("titolo");

        VBox buttonLayout = new VBox(20, startButton, optionsButton, exitButton);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox layout = new VBox(100, titolo, buttonLayout);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setTranslateY(100);

        root = new StackPane();
        root.getChildren().addAll(backgroundImageView, fogImageView, layout);

        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());

        // Applica volume e luminosità dalle impostazioni salvate
        Impostazioni impostazioni = ImpostazioniManager.caricaImpostazioni();
        if (impostazioni != null) {
            // Volume
            if (mediaPlayer == null) {
                Media sound = new Media(new File("resources/audio_battaglia.mp3").toURI().toString());
                mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }
            mediaPlayer.setVolume(impostazioni.getVolume() / 100.0);

            // Luminosità
            applicaLuminosita(impostazioni.getLuminosita());
        }

        if (!musicaInRiproduzione) {
            mediaPlayer.play();
            musicaInRiproduzione = true;
        }

        primaryStage.setTitle("Battaglia Navale");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void applicaLuminosita(double percentuale) {
        double brightness = (percentuale - 50) / 50.0; // da -1 a +1
        ColorAdjust regolazione = new ColorAdjust();
        regolazione.setBrightness(brightness);
        root.setEffect(regolazione);
    }

    private void apriGioco(Stage primaryStage) {
        // Crea il ServerGameManager con righe e colonne
        int righe = 10;
        int colonne = 10;
        ServerGameManager gameManager = new ServerGameManager(righe, colonne); // Passaggio dei parametri

        GiocoView.setGameManager(gameManager); // ✅ Imposta prima
        GiocoView giocoView = new GiocoView(); // ✅ Nessun argomento
        
        try {
            giocoView.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private void stopMusica() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            musicaInRiproduzione = false;
        }
    }

    @Override
    public void stop() {
        stopMusica();
    }

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
