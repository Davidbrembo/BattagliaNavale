package client.view;

import client.controller.GiocoController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import shared.model.Posizione;
import shared.model.RisultatoAttacco;
import utility.LogUtility;

import java.util.List;
import java.util.Optional;

/**
 * View responsabile solo della visualizzazione delle griglie di gioco.
 * Tutta la logica di business è delegata al Controller.
 */
public class GrigliaView {

    private GiocoController controller;
    private ChatView chatView;
    
    // UI Components
    private Rectangle[][] grigliaPropria;
    private Rectangle[][] grigliaAvversario;
    private boolean[][] celleAttaccateAvversario;
    private Label statoLabel;

    public GrigliaView() {
        this.controller = GiocoController.getInstance();
        this.chatView = new ChatView();
        
        // Registra questa view nel controller
        controller.registraGrigliaView(this);
        
        LogUtility.info("[GRIGLIA] GrigliaView creata e registrata nel controller");
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
        
        // *** NUOVO: Gestione chiusura finestra ***
        primaryStage.setOnCloseRequest(event -> {
            LogUtility.info("[GRIGLIA] Richiesta chiusura finestra - disconnettendo dal server...");
            
            // Previeni la chiusura immediata
            event.consume();
            
            // Mostra dialog di conferma
            Alert confermaChiusura = new Alert(Alert.AlertType.CONFIRMATION);
            confermaChiusura.setTitle("Conferma Uscita");
            confermaChiusura.setHeaderText("Sei sicuro di voler uscire?");
            confermaChiusura.setContentText("Se esci durante una partita, l'avversario vincerà automaticamente.");
            
            ButtonType esciButton = new ButtonType("Esci");
            ButtonType annullaButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            confermaChiusura.getButtonTypes().setAll(esciButton, annullaButton);
            
            Optional<ButtonType> result = confermaChiusura.showAndWait();
            
            if (result.isPresent() && result.get() == esciButton) {
                LogUtility.info("[GRIGLIA] Uscita confermata - disconnessione in corso...");
                
                // Disconnetti dal server prima di chiudere
                try {
                    if (controller.isConnesso()) {
                        controller.disconnetti();
                        LogUtility.info("[GRIGLIA] Disconnessione completata");
                    }
                } catch (Exception e) {
                    LogUtility.error("[GRIGLIA] Errore durante disconnessione: " + e.getMessage());
                }
                
                // Ora chiudi l'applicazione
                Platform.exit();
                System.exit(0);
            } else {
                LogUtility.info("[GRIGLIA] Chiusura annullata dall'utente");
            }
        });
        
        // Forza la colorazione delle navi dopo che la scena è stata creata
        Platform.runLater(() -> {
            LogUtility.info("[GRIGLIA] Forzando colorazione navi dopo creazione scena...");
            coloraNaviProprie();
        });
        
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
     * Metodo pubblico per colorare le navi (chiamato dal Controller)
     */
    public void coloraNaviProprie() {
        Platform.runLater(() -> {
            LogUtility.info("[GRIGLIA] Tentativo di colorazione navi...");
            List<List<Posizione>> mieNavi = controller.getMieNavi();
            
            if (mieNavi != null && !mieNavi.isEmpty()) {
                LogUtility.info("[GRIGLIA] Colorando " + mieNavi.size() + " navi nella griglia propria");
                
                for (int i = 0; i < mieNavi.size(); i++) {
                    List<Posizione> nave = mieNavi.get(i);
                    Color coloreNave = getColoreNavePerLunghezza(nave.size());
                    
                    for (Posizione pos : nave) {
                        if (pos.getRiga() < 10 && pos.getColonna() < 10) {
                            grigliaPropria[pos.getRiga()][pos.getColonna()].setFill(coloreNave);
                            LogUtility.info("[GRIGLIA] Colorata cella (" + pos.getRiga() + "," + pos.getColonna() + ")");
                        }
                    }
                    
                    LogUtility.info("[GRIGLIA] Colorata nave " + (i+1) + " di lunghezza " + nave.size());
                }
            } else {
                LogUtility.warning("[GRIGLIA] Nessuna nave trovata per la colorazione - getMieNavi() = " + mieNavi);
            }
        });
    }

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
            
            // Mostra un popup di vittoria più elaborato
            mostraPopupVittoria(messaggio);
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
            
            // Mostra un popup di sconfitta più elaborato
            mostraPopupSconfitta(messaggio);
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

    // ================== POPUP METHODS ==================

    /**
     * Mostra un popup di vittoria con opzioni
     */
    private void mostraPopupVittoria(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🏆 VITTORIA!");
        alert.setHeaderText("Complimenti, hai vinto!");
        alert.setContentText(messaggio + "\n\nGrazie per aver giocato!");
        
        // Solo il pulsante di chiusura
        ButtonType chiudiButton = new ButtonType("Chiudi Gioco", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(chiudiButton);
        
        // Applica stile CSS se disponibile
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("victory-dialog");
        } catch (Exception e) {
            LogUtility.warning("[GRIGLIA] Impossibile caricare CSS per dialog vittoria: " + e.getMessage());
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        
        // Dopo aver mostrato il messaggio, chiudi l'applicazione
        result.ifPresent(buttonType -> {
            if (buttonType == chiudiButton) {
                // Disconnetti e chiudi l'applicazione
                disconnettiEChiudi();
            }
        });
    }

    /**
     * Mostra un popup di sconfitta con opzioni
     */
    private void mostraPopupSconfitta(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("💀 SCONFITTA");
        alert.setHeaderText("Hai perso la battaglia...");
        alert.setContentText(messaggio + "\n\nGrazie per aver giocato!");
        
        // Solo il pulsante di chiusura
        ButtonType chiudiButton = new ButtonType("Chiudi Gioco", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(chiudiButton);
        
        // Applica stile CSS se disponibile
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("defeat-dialog");
        } catch (Exception e) {
            LogUtility.warning("[GRIGLIA] Impossibile caricare CSS per dialog sconfitta: " + e.getMessage());
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        
        // Dopo aver mostrato il messaggio, chiudi l'applicazione
        result.ifPresent(buttonType -> {
            if (buttonType == chiudiButton) {
                // Disconnetti e chiudi l'applicazione
                disconnettiEChiudi();
            }
        });
    }

    /**
     * Disconnette dal server e chiude l'applicazione
     */
    private void disconnettiEChiudi() {
        try {
            LogUtility.info("[GRIGLIA] Disconnessione e chiusura applicazione...");
            
            // Disconnetti dal controller corrente
            if (controller.isConnesso()) {
                controller.disconnetti();
                LogUtility.info("[GRIGLIA] Disconnessione completata");
            }
        } catch (Exception e) {
            LogUtility.error("[GRIGLIA] Errore durante disconnessione: " + e.getMessage());
        } finally {
            // Chiudi l'applicazione
            Platform.exit();
            System.exit(0);
        }
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

    /**
     * Restituisce il colore della nave basato sulla sua lunghezza
     */
    private Color getColoreNavePerLunghezza(int lunghezza) {
        return switch (lunghezza) {
            case 5 -> Color.DARKRED;     // Portaerei
            case 4 -> Color.DARKBLUE;    // Incrociatore
            case 3 -> Color.DARKGREEN;   // Cacciatorpediniere
            case 2 -> Color.DARKORANGE;  // Sottomarino
            default -> Color.GRAY;       // Fallback
        };
    }

    // ================== GETTERS ==================

    public ChatView getChatView() {
        return chatView;
    }

    /**
     * Verifica se la griglia è stata inizializzata correttamente
     */
    public boolean isInizializzata() {
        return grigliaPropria != null && grigliaAvversario != null && 
               celleAttaccateAvversario != null && statoLabel != null;
    }

    /**
     * Restituisce lo stato corrente della griglia avversario per debug
     */
    public String getStatoGrigliaAvversario() {
        if (celleAttaccateAvversario == null) return "Non inizializzata";
        
        int celleAttaccate = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (celleAttaccateAvversario[i][j]) {
                    celleAttaccate++;
                }
            }
        }
        return "Celle attaccate: " + celleAttaccate + "/100";
    }

    /**
     * Reset della griglia per debug/testing
     */
    public void resetGriglia() {
        Platform.runLater(() -> {
            LogUtility.info("[GRIGLIA] Reset griglia in corso...");
            
            // Reset griglia avversario
            if (grigliaAvversario != null && celleAttaccateAvversario != null) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        grigliaAvversario[i][j].setFill(Color.LIGHTGRAY);
                        grigliaAvversario[i][j].setOpacity(1.0);
                        grigliaAvversario[i][j].setOnMouseClicked(null);
                        celleAttaccateAvversario[i][j] = false;
                    }
                }
            }
            
            // Reset griglia propria
            if (grigliaPropria != null) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        grigliaPropria[i][j].setFill(Color.LIGHTBLUE);
                        grigliaPropria[i][j].setOpacity(1.0);
                    }
                }
            }
            
            // Reset stato
            if (statoLabel != null) {
                statoLabel.setText("Griglia resettata");
                statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
            
            LogUtility.info("[GRIGLIA] Reset griglia completato");
        });
    }
}