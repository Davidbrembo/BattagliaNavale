package client.view;

import client.controller.GiocoController;
import javafx.application.Application;
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

public class GiocoView extends Application {

    @Override
    public void start(Stage primaryStage) {
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

        confermaButton.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            if (nome.isEmpty()) {
                erroreLabel.setText("Il nome non può essere vuoto.");
            } else {
                erroreLabel.setText("");
                GiocoController.getInstance().setNomeGiocatore(nome);
                Messaggio msg = new Messaggio(Comando.INVIA_NOME, nome);
                GiocoController.getInstance().inviaMessaggio(msg);

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

        // Qui il listener di chiusura finestra che invia il messaggio di disconnessione
        primaryStage.setOnCloseRequest(event -> {
            GiocoController.getInstance().inviaMessaggio(new Messaggio(Comando.DISCONNESSIONE, " Questo client si disconnette"));
            client.network.ClientSocket.getInstance().chiudiConnessione();
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

    public static void setGameManager(server.model.ServerGameManager manager) {
        // se ti serve puoi implementare
    }
}
