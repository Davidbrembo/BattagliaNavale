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
import utility.Impostazioni;
import utility.ImpostazioniManager;
import utility.LogUtility;

import java.util.Optional;

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

        if (!controller.isConnesso()) {
            return creaScenaErroreConnessione();
        }

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");
        root.setAlignment(Pos.CENTER);

        Label titoloLabel = new Label("Battaglia Navale - Lobby");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label nomeLabel = new Label("Giocatore: " + 
            (controller.getNomeGiocatore() != null ? controller.getNomeGiocatore() : "Sconosciuto"));
        nomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        Label attesaLabel = new Label("Attendere il secondo giocatore...");
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

        //Gestione chiusura finestra
        primaryStage.setOnCloseRequest(event -> {
            LogUtility.info("[LOBBY] Richiesta chiusura finestra - disconnettendo dal server...");
            
            event.consume();
            
            Alert confermaChiusura = new Alert(Alert.AlertType.CONFIRMATION);
            confermaChiusura.setTitle("Conferma Uscita");
            confermaChiusura.setHeaderText("Sei sicuro di voler uscire?");
            confermaChiusura.setContentText("Se esci dalla lobby, l'altra persona dovrà aspettare un nuovo compagno di gioco.");
            
            ButtonType esciButton = new ButtonType("Esci");
            ButtonType annullaButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            confermaChiusura.getButtonTypes().setAll(esciButton, annullaButton);
            
            Optional<ButtonType> result = confermaChiusura.showAndWait();
            
            if (result.isPresent() && result.get() == esciButton) {
                LogUtility.info("[LOBBY] Uscita confermata - disconnessione in corso...");
                
                // Disconnetti dal server prima di chiudere
                try {
                    if (controller.isConnesso()) {
                        controller.disconnetti();
                        LogUtility.info("[LOBBY] Disconnessione completata");
                    }
                } catch (Exception e) {
                    LogUtility.error("[LOBBY] Errore durante disconnessione: " + e.getMessage());
                }
                
                Platform.exit();
                System.exit(0);
            } else {
                LogUtility.info("[LOBBY] Chiusura annullata dall'utente");
            }
        });

        avviaAscoltoStart();

        return scene;
    }

    private void avviaAscoltoStart() {
        LogUtility.info("[LOBBY] Registrando callback START per player ID: " + controller.getMyPlayerID());
        controller.setOnStartCallback(() -> {
            LogUtility.info("[LOBBY] ⭐ Callback START eseguito! Transizione a posizionamento navi");
            mostraPosizionamentoNavi();
        });
    }
    
    private void mostraPosizionamentoNavi() {
        if (primaryStage != null) {
            PosizionamentoNaviView posizionamentoView = new PosizionamentoNaviView();
            Scene scenaPosizionamento = posizionamentoView.creaScena(primaryStage);
            primaryStage.setScene(scenaPosizionamento);
        }
    }

    @SuppressWarnings("unused")
	private void mostraErroreConnessione() {
        if (primaryStage != null) {
            Scene scenaErrore = creaScenaErroreConnessione();
            primaryStage.setScene(scenaErrore);
        }
    }

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
    }
}