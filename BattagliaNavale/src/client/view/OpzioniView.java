package client.view;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import utility.Impostazioni;
import utility.ImpostazioniManager;

import java.io.File;

public class OpzioniView {

    private StackPane root;

    public void start(Stage primaryStage) {
        // Sfondo
        Image backgroundImage = new Image("file:resources/sfondo_opzioni.png", 1920, 1080, false, false);
        ImageView backgroundImageView = new ImageView(backgroundImage);

        // Titolo
        Label titolo = new Label("OPZIONI");
        titolo.getStyleClass().add("titolo");

        // Slider volume
        Label volumeLabel = new Label("Volume");
        volumeLabel.getStyleClass().add("impostazione-label");
        Slider volumeSlider = new Slider(0, 100, 25);
        volumeSlider.setPrefWidth(300);  // Imposta la larghezza preferita per il volume
        volumeSlider.setMaxWidth(300);   // Forza la larghezza massima dello slider

        // Slider luminosità
        Label luminositaLabel = new Label("Luminosità");
        luminositaLabel.getStyleClass().add("impostazione-label");
        Slider luminositaSlider = new Slider(0, 100, 50);
        luminositaSlider.setPrefWidth(300);  // Imposta la larghezza preferita per la luminosità
        luminositaSlider.setMaxWidth(300);   // Forza la larghezza massima dello slider

        // Aggiungere il quadrato sfocato dietro gli slider
        Rectangle backgroundRectangle = new Rectangle(350, 180);  // Aumentato per coprire tutti gli slider e i label
        backgroundRectangle.setArcWidth(20);  // Angoli arrotondati
        backgroundRectangle.setArcHeight(20);
        backgroundRectangle.setFill(Color.rgb(0, 0, 0, 0.8));  // Colore scuro semi-trasparente

        // Aggiungere un offset verticale per spostare il rettangolo più in basso
        backgroundRectangle.setTranslateY(20);  // Sposta il rettangolo verso il basso di 50 pixel

        // Pulsanti
        Button applicaButton = new Button("Applica");
        Button resetButton = new Button("Reset");
        Button tornaButton = new Button("Torna al Menu");

        Button[] buttons = {applicaButton, resetButton, tornaButton};
        for (Button btn : buttons) {
            btn.getStyleClass().add("button");
            applyPulseEffect(btn);
        }

        // Pulsante Applica
        applicaButton.setOnAction(e -> {
            Impostazioni impostazioni = new Impostazioni();
            impostazioni.setVolume(volumeSlider.getValue());
            impostazioni.setLuminosita(luminositaSlider.getValue());

            ImpostazioniManager.salvaImpostazioni(impostazioni);

            // Imposta il volume nel MediaPlayer
            if (SchermataInizialeView.getMediaPlayer() != null) {
                SchermataInizialeView.getMediaPlayer().setVolume(impostazioni.getVolume() / 100.0);
            }

            // Applica la luminosità
            applicaLuminosita(impostazioni.getLuminosita());

            System.out.println("Impostazioni applicate.");
        });

        // Pulsante Reset
        resetButton.setOnAction(e -> {
            volumeSlider.setValue(50);
            luminositaSlider.setValue(50);

            Impostazioni impostazioni = new Impostazioni();
            impostazioni.setVolume(50);
            impostazioni.setLuminosita(50);

            ImpostazioniManager.salvaImpostazioni(impostazioni);

            if (SchermataInizialeView.getMediaPlayer() != null) {
                SchermataInizialeView.getMediaPlayer().setVolume(0.5);
            }

            applicaLuminosita(50);

            System.out.println("Impostazioni ripristinate.");
        });

        // Torna al menu
        tornaButton.setOnAction(e -> {
            SchermataInizialeView schermataIniziale = new SchermataInizialeView();
            try {
                schermataIniziale.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Creare un HBox per i pulsanti "Applica" e "Reset" uno accanto all'altro
        HBox hboxButtons = new HBox(15, applicaButton, resetButton);
        hboxButtons.setAlignment(Pos.CENTER);

        // Impostiamo un VBox per gestire gli spazi tra i componenti
        VBox vbox = new VBox(15, titolo,
                volumeLabel, volumeSlider,
                luminositaLabel, luminositaSlider,
                hboxButtons, tornaButton
        );
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        // Posizioniamo il quadrato sfocato dietro gli slider
        StackPane stackSlider = new StackPane();
        stackSlider.getChildren().addAll(backgroundRectangle, vbox);

        // Creiamo lo StackPane con il background
        root = new StackPane();
        root.getChildren().addAll(backgroundImageView, stackSlider);
        root.getStyleClass().add("tema-scuro");

        // Carica le impostazioni salvate
        caricaImpostazioni(volumeSlider, luminositaSlider);

        // Crea la scena
        Scene scene = new Scene(root, 1920, 1080);
        try {
            scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("⚠️ Errore nel caricamento del CSS!");
            e.printStackTrace();
        }

        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void applyPulseEffect(Button button) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), button);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.08);
        st.setToY(1.08);
        st.setAutoReverse(true);
        st.setCycleCount(ScaleTransition.INDEFINITE);
        st.play();
    }

    private void caricaImpostazioni(Slider volumeSlider, Slider luminositaSlider) {
        File file = new File("resources/settings.json");

        if (file.exists()) {
            Impostazioni impostazioni = ImpostazioniManager.caricaImpostazioni();
            if (impostazioni != null) {
                volumeSlider.setValue(impostazioni.getVolume());
                luminositaSlider.setValue(impostazioni.getLuminosita());

                // Imposta subito volume e luminosità
                if (SchermataInizialeView.getMediaPlayer() != null) {
                    SchermataInizialeView.getMediaPlayer().setVolume(impostazioni.getVolume() / 100.0);
                }

                applicaLuminosita(impostazioni.getLuminosita());
            }
        }
    }

    private void applicaLuminosita(double percentuale) {
        double brightness = (percentuale - 50) / 50.0; // da -1 a +1
        ColorAdjust regolazione = new ColorAdjust();
        regolazione.setBrightness(brightness);
        root.setEffect(regolazione);
    }
}
