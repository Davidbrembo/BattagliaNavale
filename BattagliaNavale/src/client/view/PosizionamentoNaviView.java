package client.view;

import client.controller.GiocoController;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import shared.model.Posizione;
import shared.protocol.Comando;
import shared.protocol.Messaggio;

import java.util.ArrayList;
import java.util.List;

public class PosizionamentoNaviView {

    private Rectangle[][] celle;
    private Label statoLabel;
    private Button confermaButton;
    private List<List<Posizione>> naviPosizionate = new ArrayList<>();
    private List<Posizione> naviOccupate = new ArrayList<>();
    private int naviRimanenti = 3; // Numero di navi da posizionare
    private GiocoController giocoController;

    public PosizionamentoNaviView() {
        this.giocoController = GiocoController.getInstance();
    }

    public Scene creaScena(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 20px;");

        // Titolo
        Label titoloLabel = new Label("POSIZIONA LE TUE NAVI");
        titoloLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Istruzioni
        Label istruzioniLabel = new Label("Click sinistro: nave orizzontale | Click destro: nave verticale");
        istruzioniLabel.setStyle("-fx-text-fill: lightgray; -fx-font-size: 14px;");

        // Label per lo stato
        statoLabel = new Label("Navi da posizionare: " + naviRimanenti + " (lunghezza: 3 caselle)");
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Crea la griglia
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        int righe = 10;
        int colonne = 10;
        celle = new Rectangle[righe][colonne];

        // Crea le celle della griglia
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                Rectangle cella = new Rectangle(30, 30);
                cella.setFill(Color.LIGHTBLUE);
                cella.setStroke(Color.BLACK);

                final int riga = i;
                final int colonna = j;

                // Gestione click del mouse
                cella.setOnMouseClicked(event -> {
                    if (naviRimanenti > 0) {
                        if (event.getButton() == MouseButton.PRIMARY) {
                            // Click sinistro: nave orizzontale
                            posizionaNave(riga, colonna, true);
                        } else if (event.getButton() == MouseButton.SECONDARY) {
                            // Click destro: nave verticale
                            posizionaNave(riga, colonna, false);
                        }
                    }
                });

                celle[i][j] = cella;
                grid.add(cella, j, i);
            }
        }

        // Pulsante conferma (inizialmente disabilitato)
        confermaButton = new Button("Conferma Posizionamento");
        confermaButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        confermaButton.setDisable(true);
        
        confermaButton.setOnAction(e -> {
            inviaNaviAlServer();
            mostraAttesaAvversario(primaryStage);
        });

        // Pulsante reset
        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");
        resetButton.setOnAction(e -> resetGriglia());

        HBox buttonBox = new HBox(20, resetButton, confermaButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(titoloLabel, istruzioniLabel, statoLabel, grid, buttonBox);

        Scene scena = new Scene(root, 800, 700);
        return scena;
    }

    private void posizionaNave(int riga, int colonna, boolean orizzontale) {
        List<Posizione> posizioniNave = new ArrayList<>();
        
        // Calcola le posizioni della nave
        for (int i = 0; i < 3; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            // Controlla se la posizione è valida
            if (nuovaRiga >= 10 || nuovaColonna >= 10) {
                mostraErrore("Nave fuori dai limiti della griglia!");
                return;
            }
            
            Posizione pos = new Posizione(nuovaRiga, nuovaColonna);
            
            // Controlla se la posizione è già occupata
            if (naviOccupate.contains(pos)) {
                mostraErrore("Posizione già occupata da un'altra nave!");
                return;
            }
            
            posizioniNave.add(pos);
        }
        
        // Se arriviamo qui, la nave può essere posizionata
        naviPosizionate.add(posizioniNave);
        naviOccupate.addAll(posizioniNave);
        naviRimanenti--;
        
        // Colora le celle della nave
        for (Posizione pos : posizioniNave) {
            celle[pos.getRiga()][pos.getColonna()].setFill(Color.DARKGREEN);
        }
        
        // Aggiorna lo stato
        aggiornaStato();
    }

    private void aggiornaStato() {
        if (naviRimanenti > 0) {
            statoLabel.setText("Navi da posizionare: " + naviRimanenti + " (lunghezza: 3 caselle)");
        } else {
            statoLabel.setText("Tutte le navi posizionate! Clicca Conferma per continuare.");
            statoLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 16px; -fx-font-weight: bold;");
            confermaButton.setDisable(false);
        }
    }

    private void resetGriglia() {
        // Reset delle liste
        naviPosizionate.clear();
        naviOccupate.clear();
        naviRimanenti = 3;
        
        // Reset delle celle
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                celle[i][j].setFill(Color.LIGHTBLUE);
            }
        }
        
        // Reset dei controlli
        confermaButton.setDisable(true);
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        aggiornaStato();
    }

    private void inviaNaviAlServer() {
        // Invia le navi posizionate al server
        Messaggio messaggio = new Messaggio(Comando.POSIZIONA_NAVI, naviPosizionate);
        giocoController.inviaMessaggio(messaggio);
    }

    private void mostraAttesaAvversario(Stage primaryStage) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");

        Label titoloLabel = new Label("Navi Posizionate!");
        titoloLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label attesaLabel = new Label("Attendere che l'avversario posizioni le sue navi...");
        attesaLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        root.getChildren().addAll(titoloLabel, attesaLabel);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);

        // Thread per aspettare il messaggio di inizio battaglia
        new Thread(() -> {
            try {
                while (true) {
                    Messaggio msg = giocoController.clientSocket.riceviMessaggio();
                    if (msg == null) break;
                    
                    if (msg.getComando() == Comando.INIZIO_BATTAGLIA) {
                        Platform.runLater(() -> mostraGrigliaAttacco(primaryStage));
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void mostraGrigliaAttacco(Stage primaryStage) {
        server.model.ServerGameManager gameManager = new server.model.ServerGameManager(10, 10);
        GrigliaView grigliaView = new GrigliaView(gameManager);
        Scene scenaGriglia = grigliaView.creaScena(primaryStage);
        primaryStage.setScene(scenaGriglia);
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Errore Posizionamento");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}