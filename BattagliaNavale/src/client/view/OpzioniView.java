package client.view;

import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OpzioniView {

    public void start(Stage primaryStage) {

        // Immagine di sfondo
        Image backgroundImage = new Image("file:resources/sfondo_opzioni.png");
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setFitWidth(1920);
        backgroundImageView.setFitHeight(1080);

        // Titolo
        Label titolo = new Label("OPZIONI");
        titolo.getStyleClass().add("titolo");

        // Pulsanti
        Button applicaButton = new Button("Applica");
        Button tornaButton = new Button("Torna al Menu");

        applicaButton.getStyleClass().add("button");
        tornaButton.getStyleClass().add("button");

        // Effetti pulsazione
        applyPulseEffect(applicaButton);
        applyPulseEffect(tornaButton);

        // Azione pulsante Torna
        tornaButton.setOnAction(e -> {
            // Torna al menu principale senza avviare il gioco
            SchermataInizialeView schermataIniziale = new SchermataInizialeView();
            try {
                schermataIniziale.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Layout
        VBox layout = new VBox(40, titolo, applicaButton, tornaButton);
        layout.setAlignment(Pos.CENTER);
        layout.setTranslateY(50);

        // StackPane root per disporre gli elementi
        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundImageView, layout);

        // Scena
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    // Metodo per applicare effetto pulsazione ai bottoni
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
}