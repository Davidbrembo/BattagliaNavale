package client.view;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
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
        imageView.setPreserveRatio(false); // Disabilita il mantenimento delle proporzioni
        imageView.setFitWidth(screenWidth); // Imposta la larghezza dell'immagine a tutta la finestra
        imageView.setFitHeight(screenHeight); // Imposta l'altezza dell'immagine a tutta la finestra
        imageView.setSmooth(true); // Migliora la qualità dell'immagine
        imageView.setCache(true); // Abilita la cache per migliorare le prestazioni
        
        // Usando AnchorPane per evitare spazi extra
        AnchorPane root = new AnchorPane();
        root.getChildren().add(imageView);

        // Fissiamo i limiti dell'immagine alla finestra
        AnchorPane.setTopAnchor(imageView, 0.0);
        AnchorPane.setRightAnchor(imageView, 0.0);
        AnchorPane.setBottomAnchor(imageView, 0.0);
        AnchorPane.setLeftAnchor(imageView, 0.0);

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
        primaryStage.setFullScreen(true); // Forza la modalità a schermo intero
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
