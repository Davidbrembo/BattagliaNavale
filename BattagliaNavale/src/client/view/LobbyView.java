package client.view;

import client.controller.GiocoController;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.model.ServerGameManager;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.Impostazioni;
import utility.ImpostazioniManager;

public class LobbyView extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");
        root.setAlignment(Pos.CENTER);

        // Etichetta di richiesta nome
        Label promptLabel = new Label("Inserisci il tuo nome:");
        promptLabel.getStyleClass().add("impostazione-label");

        // Campo di testo per l'inserimento del nome
        TextField nomeField = new TextField();
        nomeField.setPromptText("Lobby");
        nomeField.setMaxWidth(300);
        nomeField.getStyleClass().add("nome-field");

        // Aggiungi elementi al layout
        root.getChildren().addAll(promptLabel);

        // Crea scena
        Scene scene = new Scene(root, 800, 600);

        // Applica CSS
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());

        // ✅ Applica luminosità da settings.json
        applicaLuminosita(root);

        // Finestra
        primaryStage.setScene(scene);
        primaryStage.setTitle("Battaglia Navale");
        primaryStage.setFullScreen(false);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
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
    
    private void mostraPosizionamentoNavi(Stage primaryStage) {
        // Avvia la fase di posizionamento delle navi
        PosizionamentoNaviView posizionamentoView = new PosizionamentoNaviView();
        Scene scenaPosizionamento = posizionamentoView.creaScena(primaryStage);
        primaryStage.setScene(scenaPosizionamento);
        primaryStage.show();
    }

    public static void setGameManager(ServerGameManager manager) {
    	
    }

    public Scene creaScena(Stage primaryStage) {
        if (!GiocoController.getInstance().isConnesso()) {
            System.out.println("[CLIENT] Connessione non riuscita.");
            return new Scene(new VBox(new Label("Errore di connessione.")), 800, 600);
        }

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");
        root.setAlignment(Pos.CENTER);

        Label titoloLabel = new Label("Battaglia Navale - Lobby");
        titoloLabel.getStyleClass().add("impostazione-label");

        Label attesaLabel = new Label("Attendere il secondo giocatore...");
        attesaLabel.getStyleClass().add("impostazione-label");

        root.getChildren().addAll(titoloLabel, attesaLabel);

        applicaLuminosita(root);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());

        // ✅ Thread per ricevere messaggio START (ora avvia il posizionamento delle navi)
        new Thread(() -> {
            try {
                while (true) {
                    Messaggio msg = GiocoController.getInstance().clientSocket.riceviMessaggio();
                    if (msg == null) break;
                    
                    System.out.println("Messaggio ricevuto in lobby: " + msg);

                    if (msg.getComando() == Comando.START) {
                        javafx.application.Platform.runLater(() -> mostraPosizionamentoNavi(primaryStage));
                        break; // Esci dal loop della lobby
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return scene;
    }
}