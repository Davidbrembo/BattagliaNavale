package client.view;

import client.controller.GiocoController;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utility.Impostazioni;
import utility.ImpostazioniManager;

/**
 * View per l'inserimento del nome giocatore.
 * Segue il pattern MVC delegando la logica al Controller.
 */
public class GiocoView extends Application {

    private GiocoController controller;

    @Override
    public void start(Stage primaryStage) {
        // Ottieni il controller
        controller = GiocoController.getInstance();
        
        // Verifica connessione
        if (!controller.isConnesso()) {
            mostraErroreConnessione(primaryStage);
            return;
        }

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");
        root.setAlignment(Pos.CENTER);

        Label promptLabel = new Label("Inserisci il tuo nome:");
        promptLabel.getStyleClass().add("impostazione-label");

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome giocatore");
        nomeField.setMaxWidth(300);
        nomeField.getStyleClass().add("nome-field");

        Button confermaButton = new Button("Conferma");
        confermaButton.getStyleClass().add("button");

        Label erroreLabel = new Label();
        erroreLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Label nomeLabel = new Label();
        nomeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        // Gestione eventi - delegata al Controller
        confermaButton.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            if (nome.isEmpty()) {
                erroreLabel.setText("Il nome non può essere vuoto.");
            } else {
                erroreLabel.setText("");
                
                // Delega al Controller l'impostazione del nome
                controller.impostaNomeGiocatore(nome);

                nomeLabel.setText("Giocatore: " + nome);
                root.getChildren().removeAll(promptLabel, nomeField, confermaButton, erroreLabel);
                root.getChildren().add(nomeLabel);

                mostraLobby(primaryStage);
            }
        });

        nomeField.setOnAction(e -> confermaButton.fire());

        root.getChildren().addAll(promptLabel, nomeField, confermaButton, erroreLabel);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());

        applicaLuminosita(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Battaglia Navale");
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();

        // Gestione chiusura finestra - delega al Controller
        primaryStage.setOnCloseRequest(event -> {
            controller.disconnetti();
        });

        primaryStage.show();
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

    private void mostraLobby(Stage primaryStage) {
        LobbyView lobbyView = new LobbyView();
        Scene scenaLobby = lobbyView.creaScena(primaryStage);
        primaryStage.setScene(scenaLobby);
        primaryStage.show();
    }
    
    private void mostraErroreConnessione(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");
        root.setAlignment(Pos.CENTER);

        Label erroreLabel = new Label("❌ Errore di Connessione");
        erroreLabel.setStyle("-fx-text-fill: red; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label descrizioneLabel = new Label("Impossibile connettersi al server.\nAssicurati che il server sia avviato e riprova.");
        descrizioneLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-text-alignment: center;");

        Button ritentaButton = new Button("Riprova");
        ritentaButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        ritentaButton.setOnAction(e -> {
            // Ricrea il controller per ritentare la connessione
            start(primaryStage);
        });

        Button esciButton = new Button("Esci");
        esciButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        esciButton.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(erroreLabel, descrizioneLabel, ritentaButton, esciButton);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Deprecated
    public static void setGameManager(server.model.ServerGameManager manager) {
        // Metodo deprecato - non più necessario con il nuovo pattern MVC
    }
}