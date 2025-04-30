package client.view;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import shared.protocol.Comando;
import shared.protocol.Messaggio;
import client.controller.GiocoController;

import java.io.File;
import java.util.Optional;

public class SchermataInizialeView extends Application {

    private static MediaPlayer mediaPlayer;
    private static boolean musicaInRiproduzione = false;
    private GiocoController giocoController;

    @Override
    public void start(Stage primaryStage) {
        giocoController = GiocoController.getInstance();

        // Connessione al server
        boolean connesso = giocoController.iniziaConnessione();
        if (!connesso) {
            System.out.println("Errore di connessione al server. L'app verrà chiusa.");
            return;
        }

        // Sfondo
        Image backgroundImage = new Image("file:resources/battaglia.jpg");
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setFitWidth(1920);
        backgroundImageView.setFitHeight(1080);

        // GIF nebbia
        Image fogImage = new Image("file:resources/nebbia.gif");
        ImageView fogImageView = new ImageView(fogImage);
        fogImageView.setPreserveRatio(false);
        fogImageView.setFitWidth(1920);
        fogImageView.setFitHeight(1080);
        fogImageView.setOpacity(0.2);

        // Input nome giocatore
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Scegli un nome");
        dialog.setHeaderText("Inserisci il tuo nome:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nome -> {
            Messaggio msg = new Messaggio(Comando.INVIA_NOME, nome);
            giocoController.inviaMessaggio(msg);
        });

        // Pulsanti
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

        // Audio
        if (mediaPlayer == null) {
            Media sound = new Media(new File("resources/audio_battaglia.mp3").toURI().toString());
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
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

    private void apriGioco(Stage primaryStage) {
        GiocoView giocoView = new GiocoView();
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

    public static void main(String[] args) {
        launch(args);
    }
}
