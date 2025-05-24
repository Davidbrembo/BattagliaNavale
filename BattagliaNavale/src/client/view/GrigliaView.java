package client.view;

import client.controller.GiocoController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import shared.model.Posizione;
import shared.model.RisultatoAttacco;
import server.model.ServerGameManager;
import shared.protocol.Comando;
import shared.protocol.Messaggio;

public class GrigliaView {

    private ServerGameManager gameManager;
    private GiocoController giocoController;
    private Rectangle[][] celle;
    private Label statoLabel;
    private boolean mioTurno = false;
    private int myPlayerID = -1;

    public GrigliaView(ServerGameManager gameManager) {
        this.gameManager = gameManager;
        this.giocoController = GiocoController.getInstance();
    }

    public Scene creaScena(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 20px;");

        // Label per lo stato del gioco
        statoLabel = new Label("Attendere inizio partita...");
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Crea un GridPane per la griglia di gioco
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        int righe = 10;
        int colonne = 10;
        celle = new Rectangle[righe][colonne];

        // Aggiungi le celle alla griglia
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                Rectangle cella = new Rectangle(30, 30);
                cella.setFill(Color.LIGHTBLUE);
                cella.setStroke(Color.BLACK);

                // Aggiungi evento di clic sulla cella
                Posizione posizione = new Posizione(i, j);
                cella.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (mioTurno) {
                            inviaAttaccoAlServer(posizione);
                        } else {
                            Platform.runLater(() -> {
                                statoLabel.setText("Non è il tuo turno!");
                                statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");
                            });
                        }
                    }
                });

                celle[i][j] = cella;
                grid.add(cella, j, i);
            }
        }

        root.getChildren().addAll(statoLabel, grid);

        // Thread per ricevere messaggi dal server
        avviaAscoltoServer();

        Scene scena = new Scene(root, 800, 600);
        return scena;
    }

    private void avviaAscoltoServer() {
        new Thread(() -> {
            try {
                while (true) {
                    Messaggio msg = giocoController.clientSocket.riceviMessaggio();
                    if (msg == null) break;

                    Platform.runLater(() -> gestisciMessaggioServer(msg));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void gestisciMessaggioServer(Messaggio messaggio) {
        switch (messaggio.getComando()) {
            case ASSEGNA_ID -> {
                myPlayerID = (Integer) messaggio.getContenuto();
                statoLabel.setText("Sei il giocatore " + (myPlayerID + 1));
            }
            case TURNO -> {
                mioTurno = true;
                statoLabel.setText("È il tuo turno! Clicca per attaccare.");
                statoLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
            case STATO -> {
                mioTurno = false;
                statoLabel.setText((String) messaggio.getContenuto());
                statoLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
            case RISULTATO_ATTACCO -> {
                RisultatoAttacco risultato = (RisultatoAttacco) messaggio.getContenuto();
                aggiornaCella(risultato);
            }
            case ERRORE -> {
                statoLabel.setText((String) messaggio.getContenuto());
                statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
        }
    }

    private void inviaAttaccoAlServer(Posizione posizione) {
        Messaggio messaggio = new Messaggio(Comando.ATTACCA, posizione);
        giocoController.inviaMessaggio(messaggio);
        
        // Disabilita temporaneamente il turno fino alla risposta del server
        mioTurno = false;
        statoLabel.setText("Attacco inviato... attendere risultato");
        statoLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 18px; -fx-font-weight: bold;");
    }

    private void aggiornaCella(RisultatoAttacco risultato) {
        Posizione pos = risultato.getPosizione();
        Rectangle cella = celle[pos.getRiga()][pos.getColonna()];
        
        if (risultato.isColpito()) {
            cella.setFill(Color.RED); // Colpito
            if (risultato.isNaveAffondata()) {
                cella.setFill(Color.DARKRED); // Nave affondata
            }
        } else {
            cella.setFill(Color.GRAY); // Mancato
        }
    }
}