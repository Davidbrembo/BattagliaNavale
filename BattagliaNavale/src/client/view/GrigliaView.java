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
    private Rectangle[][] grigliaPropria;      // Griglia con le proprie navi
    private Rectangle[][] grigliaAvversario;   // Griglia per attaccare l'avversario
    private boolean[][] celleAttaccateAvversario; // Celle già attaccate sulla griglia avversario
    private Label statoLabel;
    private boolean mioTurno = false;
    private int myPlayerID = -1;
    private ChatView chatView; // Componente chat

    public GrigliaView(ServerGameManager gameManager) {
        this.gameManager = gameManager;
        this.giocoController = GiocoController.getInstance();
        this.chatView = new ChatView(); // Inizializza la chat
    }

    public Scene creaScena(Stage primaryStage) {
        HBox mainContainer = new HBox(20); // Container principale orizzontale
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 20px;");

        // Container per il gioco (parte sinistra)
        VBox gameContainer = new VBox(10);
        gameContainer.setAlignment(Pos.CENTER);

        // Label per lo stato del gioco
        statoLabel = new Label("Attendere inizio partita...");
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Container orizzontale per le due griglie
        HBox griglie = new HBox(50);
        griglie.setAlignment(Pos.CENTER);

        // Crea la griglia propria (sinistra)
        VBox containerPropria = new VBox(10);
        containerPropria.setAlignment(Pos.CENTER);
        Label labelPropria = new Label("La tua griglia");
        labelPropria.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        GridPane gridPropria = creaGriglia(true);
        containerPropria.getChildren().addAll(labelPropria, gridPropria);

        // Crea la griglia avversario (destra)
        VBox containerAvversario = new VBox(10);
        containerAvversario.setAlignment(Pos.CENTER);
        Label labelAvversario = new Label("Griglia avversario");
        labelAvversario.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        GridPane gridAvversario = creaGriglia(false);
        containerAvversario.getChildren().addAll(labelAvversario, gridAvversario);

        griglie.getChildren().addAll(containerPropria, containerAvversario);
        gameContainer.getChildren().addAll(statoLabel, griglie);

        // Aggiungi il game container e la chat al container principale
        mainContainer.getChildren().addAll(gameContainer, chatView.getChatContainer());

        // Thread per ricevere messaggi dal server
        avviaAscoltoServer();

        Scene scena = new Scene(mainContainer, 1500, 700); // Aumentata la larghezza per la chat
        return scena;
    }

    private GridPane creaGriglia(boolean isPropria) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        int righe = 10;
        int colonne = 10;
        
        Rectangle[][] grigliaCorrente;
        if (isPropria) {
            grigliaPropria = new Rectangle[righe][colonne];
            grigliaCorrente = grigliaPropria;
        } else {
            grigliaAvversario = new Rectangle[righe][colonne];
            celleAttaccateAvversario = new boolean[righe][colonne];
            grigliaCorrente = grigliaAvversario;
        }

        // Aggiungi le celle alla griglia
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                Rectangle cella = new Rectangle(30, 30);
                
                if (isPropria) {
                    // Griglia propria: mostra le navi (per ora blu, poi andranno posizionate)
                    cella.setFill(Color.LIGHTBLUE);
                } else {
                    // Griglia avversario: inizialmente grigia (sconosciuta)
                    cella.setFill(Color.LIGHTGRAY);
                    
                    // Solo la griglia avversario è cliccabile per gli attacchi
                    Posizione posizione = new Posizione(i, j);
                    cella.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (!mioTurno) {
                                Platform.runLater(() -> {
                                    statoLabel.setText("Non è il tuo turno!");
                                    statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");
                                });
                                return;
                            }
                            
                            // Controlla se la cella è già stata attaccata
                            if (celleAttaccateAvversario[posizione.getRiga()][posizione.getColonna()]) {
                                Platform.runLater(() -> {
                                    statoLabel.setText("Cella già attaccata! Scegli un'altra posizione.");
                                    statoLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 18px; -fx-font-weight: bold;");
                                });
                                return;
                            }
                            
                            inviaAttaccoAlServer(posizione);
                        }
                    });
                }

                cella.setStroke(Color.BLACK);
                grigliaCorrente[i][j] = cella;
                grid.add(cella, j, i);
            }
        }

        return grid;
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
                statoLabel.setText("È il tuo turno! Clicca sulla griglia avversario per attaccare.");
                statoLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px; -fx-font-weight: bold;");
                chatView.mostraNotificaTurno(true); // Notifica nella chat
            }
            case STATO -> {
                mioTurno = false;
                statoLabel.setText((String) messaggio.getContenuto());
                statoLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 18px; -fx-font-weight: bold;");
                chatView.mostraNotificaTurno(false); // Notifica nella chat
            }
            case RISULTATO_ATTACCO -> {
                RisultatoAttacco risultato = (RisultatoAttacco) messaggio.getContenuto();
                // Questo è il risultato del MIO attacco sulla griglia avversario
                aggiornaCellaAvversario(risultato);
            }
            case ATTACCO_RICEVUTO -> {
                // Questo rappresenta un attacco che ho ricevuto sulla mia griglia
                RisultatoAttacco risultato = (RisultatoAttacco) messaggio.getContenuto();
                aggiornaCellaPropria(risultato);
            }
            case VITTORIA -> {
                mioTurno = false;
                statoLabel.setText("🎉 " + (String) messaggio.getContenuto() + " 🎉");
                statoLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 20px; -fx-font-weight: bold;");
                disabilitaGriglia();
            }
            case SCONFITTA -> {
                mioTurno = false;
                statoLabel.setText("💀 " + (String) messaggio.getContenuto() + " 💀");
                statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold;");
                disabilitaGriglia();
            }
            case MESSAGGIO_CHAT -> {
                // Gestisci i messaggi di chat ricevuti
                ChatView.MessaggioChat messaggioChat = (ChatView.MessaggioChat) messaggio.getContenuto();
                chatView.riceviMessaggio(messaggioChat);
            }
            case ERRORE -> {
                statoLabel.setText((String) messaggio.getContenuto());
                statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + messaggio.getComando());
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

    private void aggiornaCellaAvversario(RisultatoAttacco risultato) {
        Posizione pos = risultato.getPosizione();
        Rectangle cella = grigliaAvversario[pos.getRiga()][pos.getColonna()];
        
        // Marca la cella come attaccata
        celleAttaccateAvversario[pos.getRiga()][pos.getColonna()] = true;
        
        if (risultato.isColpito()) {
            cella.setFill(Color.RED); // Colpito
            if (risultato.isNaveAffondata()) {
                cella.setFill(Color.DARKRED); // Nave affondata
            }
        } else {
            cella.setFill(Color.BLUE); // Mancato (acqua)
        }
    }

    private void aggiornaCellaPropria(RisultatoAttacco risultato) {
        Posizione pos = risultato.getPosizione();
        Rectangle cella = grigliaPropria[pos.getRiga()][pos.getColonna()];
        
        if (risultato.isColpito()) {
            cella.setFill(Color.ORANGE); // La mia nave è stata colpita
            if (risultato.isNaveAffondata()) {
                cella.setFill(Color.DARKRED); // La mia nave è affondata
            }
        } else {
            cella.setFill(Color.CYAN); // Attacco mancato sulla mia griglia
        }
    }

    private void disabilitaGriglia() {
        // Disabilita tutti i click sulla griglia avversario quando la partita è finita
        for (int i = 0; i < grigliaAvversario.length; i++) {
            for (int j = 0; j < grigliaAvversario[i].length; j++) {
                grigliaAvversario[i][j].setOnMouseClicked(null);
            }
        }
    }
}