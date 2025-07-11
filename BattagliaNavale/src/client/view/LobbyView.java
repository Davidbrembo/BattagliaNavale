package client.view;

import client.controller.GiocoController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.Impostazioni;
import utility.ImpostazioniManager;
import utility.LogUtility;

/**
 * View della Lobby che attende l'arrivo del secondo giocatore.
 * Segue il pattern MVC - no logica di business, solo UI.
 */
public class LobbyView extends Application {

    private GiocoController controller;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.controller = GiocoController.getInstance();
        
        Scene scene = creaScena(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Scene creaScena(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.controller = GiocoController.getInstance();
        
        // Verifica connessione
        if (!controller.isConnesso()) {
            return creaScenaErroreConnessione();
        }

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");
        root.setAlignment(Pos.CENTER);

        Label titoloLabel = new Label("🌊 Battaglia Navale - Lobby");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label nomeLabel = new Label("Giocatore: " + 
            (controller.getNomeGiocatore() != null ? controller.getNomeGiocatore() : "Sconosciuto"));
        nomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        Label attesaLabel = new Label("⏳ Attendere il secondo giocatore...");
        attesaLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-style: italic;");

        // Indicatore di caricamento
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);
        progressIndicator.setStyle("-fx-progress-color: #4CAF50;");

        Label contatoreLabel = new Label("Giocatori connessi: 1/2");
        contatoreLabel.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");

        root.getChildren().addAll(titoloLabel, nomeLabel, attesaLabel, progressIndicator, contatoreLabel);

        applicaLuminosita(root);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());

        // Avvia l'ascolto per il messaggio START
        avviaAscoltoStart();

        return scene;
    }

    /**
     * Avvia un thread per ascoltare il messaggio START dal server
     */
    private void avviaAscoltoStart() {
        // Registra il callback nel Controller invece di creare un thread separato
        LogUtility.info("[LOBBY] Registrando callback START per player ID: " + controller.getMyPlayerID());
        controller.setOnStartCallback(() -> {
            LogUtility.info("[LOBBY] ⭐ Callback START eseguito! Transizione a posizionamento navi");
            mostraPosizionamentoNavi();
        });
    }

    /**
     * Transizione alla fase di posizionamento navi
     */
    private void mostraPosizionamentoNavi() {
        if (primaryStage != null) {
            PosizionamentoNaviView posizionamentoView = new PosizionamentoNaviView();
            Scene scenaPosizionamento = posizionamentoView.creaScena(primaryStage);
            primaryStage.setScene(scenaPosizionamento);
        }
    }

    /**
     * Mostra errore di connessione
     */
    private void mostraErroreConnessione() {
        if (primaryStage != null) {
            Scene scenaErrore = creaScenaErroreConnessione();
            primaryStage.setScene(scenaErrore);
        }
    }

    /**
     * Crea la scena di errore connessione
     */
    private Scene creaScenaErroreConnessione() {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");
        root.setAlignment(Pos.CENTER);

        Label erroreLabel = new Label("❌ Connessione Persa");
        erroreLabel.setStyle("-fx-text-fill: red; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label descrizioneLabel = new Label("La connessione al server è stata interrotta.");
        descrizioneLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Button tornaMenuButton = new Button("Torna al Menu");
        tornaMenuButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        tornaMenuButton.setOnAction(e -> {
            SchermataInizialeView schermataIniziale = new SchermataInizialeView();
            try {
                schermataIniziale.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(erroreLabel, descrizioneLabel, tornaMenuButton);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
        
        return scene;
    }

    private void applicaLuminosita(VBox root) {
        Impostazioni impostazioni = ImpostazioniManager.caricaImpostazioni();
        if (impostazioni != null) {
            double percentuale = impostazioni.getLuminosita();
            double brightness = (percentuale - 50) / 50.0;
            ColorAdjust regolazione = new ColorAdjust();
            regolazione.setBrightness(brightness);
            root.setEffect(regolazione);
        }
    }

    @Deprecated
    public static void setGameManager(server.model.ServerGameManager manager) {
        // Metodo deprecato - non più necessario con il nuovo pattern MVC
    }
}