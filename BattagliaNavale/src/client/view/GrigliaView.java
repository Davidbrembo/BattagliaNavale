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

/**
 * View responsabile solo della visualizzazione delle griglie di gioco.
 * Tutta la logica di business è delegata al Controller.
 */
public class GrigliaView {

    private ServerGameManager gameManager;
    private GiocoController controller;
    private ChatView chatView;
    
    // UI Components
    private Rectangle[][] grigliaPropria;
    private Rectangle[][] grigliaAvversario;
    private boolean[][] celleAttaccateAvversario;
    private Label statoLabel;

    public GrigliaView(ServerGameManager gameManager) {
        this.gameManager = gameManager;
        this.controller = GiocoController.getInstance();
        this.chatView = new ChatView();
        
        // Registra questa view nel controller
        controller.registraGrigliaView(this);
    }

    public Scene creaScena(Stage primaryStage) {
        HBox mainContainer = new HBox(20);
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

        Scene scena = new Scene(mainContainer, 1500, 700);
        return scena;
    }

    // ================== UI CREATION ==================

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

        // Crea le celle della griglia
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                Rectangle cella = new Rectangle(30, 30);
                
                if (isPropria) {
                    // Griglia propria: mostra le navi
                    cella.setFill(Color.LIGHTBLUE);
                } else {
                    // Griglia avversario: inizialmente grigia (sconosciuta)
                    cella.setFill(Color.LIGHTGRAY);
                    
                    // Solo la griglia avversario è cliccabile per gli attacchi
                    Posizione posizione = new Posizione(i, j);
                    cella.setOnMouseClicked(createAttackHandler(posizione));
                }

                cella.setStroke(Color.BLACK);
                grigliaCorrente[i][j] = cella;
                grid.add(cella, j, i);
            }
        }

        return grid;
    }

    /**
     * Crea un handler per gli attacchi che delega al Controller
     */
    private EventHandler<MouseEvent> createAttackHandler(Posizione posizione) {
        return event -> {
            // Validazioni UI immediate
            if (!controller.isMioTurno()) {
                aggiornaStatoGioco("Non è il tuo turno!");
                return;
            }
            
            if (celleAttaccateAvversario[posizione.getRiga()][posizione.getColonna()]) {
                aggiornaStatoGioco("Cella già attaccata! Scegli un'altra posizione.");
                return;
            }
            
            // Delega l'attacco al Controller
            controller.attacca(posizione);
        };
    }

    // ================== PUBLIC INTERFACE - Chiamate dal Controller ==================

    /**
     * Aggiorna il testo di stato del gioco (chiamato dal Controller)
     */
    public void aggiornaStatoGioco(String stato) {
        Platform.runLater(() -> {
            statoLabel.setText(stato);
            // Colori diversi basati sul contenuto del messaggio
            if (stato.contains("turno")) {
                statoLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px; -fx-font-weight: bold;");
            } else if (stato.contains("Errore") || stato.contains("Non è")) {
                statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");
            } else if (stato.contains("attendere") || stato.contains("Attacco")) {
                statoLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 18px; -fx-font-weight: bold;");
            } else {
                statoLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
        });
    }

    /**
     * Attiva/disattiva la griglia avversario per gli attacchi (chiamato dal Controller)
     */
    public void attivaGrigliaAvversario(boolean attiva) {
        Platform.runLater(() -> {
            for (int i = 0; i < grigliaAvversario.length; i++) {
                for (int j = 0; j < grigliaAvversario[i].length; j++) {
                    Rectangle cella = grigliaAvversario[i][j];
                    if (attiva && !celleAttaccateAvversario[i][j]) {
                        // Riattiva solo le celle non ancora attaccate
                        Posizione pos = new Posizione(i, j);
                        cella.setOnMouseClicked(createAttackHandler(pos));
                        cella.setOpacity(1.0);
                    } else {
                        // Disattiva tutti i click
                        cella.setOnMouseClicked(null);
                        cella.setOpacity(attiva ? 1.0 : 0.7);
                    }
                }
            }
        });
    }

    /**
     * Aggiorna una cella della griglia avversario (chiamato dal Controller)
     */
    public void aggiornaCellaAvversario(RisultatoAttacco risultato) {
        Platform.runLater(() -> {
            Posizione pos = risultato.getPosizione();
            Rectangle cella = grigliaAvversario[pos.getRiga()][pos.getColonna()];
            
            // Marca la cella come attaccata
            celleAttaccateAvversario[pos.getRiga()][pos.getColonna()] = true;
            
            if (risultato.isColpito()) {
                if (risultato.isNaveAffondata()) {
                    cella.setFill(Color.DARKRED); // Nave affondata
                } else {
                    cella.setFill(Color.RED); // Colpito
                }
            } else {
                cella.setFill(Color.BLUE); // Mancato (acqua)
            }
            
            // Disabilita il click su questa cella
            cella.setOnMouseClicked(null);
        });
    }

    /**
     * Aggiorna una cella della griglia propria (chiamato dal Controller)
     */
    public void aggiornaCellaPropria(RisultatoAttacco risultato) {
        Platform.runLater(() -> {
            Posizione pos = risultato.getPosizione();
            Rectangle cella = grigliaPropria[pos.getRiga()][pos.getColonna()];
            
            if (risultato.isColpito()) {
                if (risultato.isNaveAffondata()) {
                    cella.setFill(Color.DARKRED); // La mia nave è affondata
                } else {
                    cella.setFill(Color.ORANGE); // La mia nave è stata colpita
                }
            } else {
                cella.setFill(Color.CYAN); // Attacco mancato sulla mia griglia
            }
        });
    }

    /**
     * Gestisce la vittoria (chiamato dal Controller)
     */
    public void gestisciVittoria(String messaggio) {
        Platform.runLater(() -> {
            statoLabel.setText("🎉 " + messaggio + " 🎉");
            statoLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 20px; -fx-font-weight: bold;");
            disabilitaGriglia();
        });
    }

    /**
     * Gestisce la sconfitta (chiamato dal Controller)
     */
    public void gestisciSconfitta(String messaggio) {
        Platform.runLater(() -> {
            statoLabel.setText("💀 " + messaggio + " 💀");
            statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold;");
            disabilitaGriglia();
        });
    }

    /**
     * Mostra un errore (chiamato dal Controller)
     */
    public void mostraErrore(String errore) {
        Platform.runLater(() -> {
            statoLabel.setText("❌ " + errore);
            statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");
        });
    }

    // ================== PRIVATE UTILITY METHODS ==================

    /**
     * Disabilita completamente la griglia quando la partita è finita
     */
    private void disabilitaGriglia() {
        for (int i = 0; i < grigliaAvversario.length; i++) {
            for (int j = 0; j < grigliaAvversario[i].length; j++) {
                grigliaAvversario[i][j].setOnMouseClicked(null);
                grigliaAvversario[i][j].setOpacity(0.5);
            }
        }
    }

    // ================== GETTERS ==================

    public ChatView getChatView() {
        return chatView;
    }
}