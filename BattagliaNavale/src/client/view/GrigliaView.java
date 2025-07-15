package client.view;

import client.controller.GiocoController;
import client.view.components.NaveGraphics;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import shared.model.Posizione;
import shared.model.RisultatoAttacco;
import shared.model.TipoNave;
import utility.LogUtility;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

public class GrigliaView {

    private GiocoController controller;
    private ChatView chatView;
    
    // UI Components
    private StackPane[][] celleProprie;
    private StackPane[][] celleAvversario;
    private boolean[][] celleAttaccateAvversario;
    private Label statoLabel;

    private Map<Posizione, NaveGraphics> naviGrafiche = new HashMap<>();

    public GrigliaView() {
        this.controller = GiocoController.getInstance();
        this.chatView = new ChatView();

        controller.registraGrigliaView(this);
        
        LogUtility.info("[GRIGLIA] GrigliaView creata e registrata nel controller");
    }

    public Scene creaScena(Stage primaryStage) {
        HBox mainContainer = new HBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 20px;");

        //Container per il gioco
        VBox gameContainer = new VBox(10);
        gameContainer.setAlignment(Pos.CENTER);

        //Label per lo stato del gioco
        statoLabel = new Label("Attendere inizio partita...");
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        //Container orizzontale per le due griglie
        HBox griglie = new HBox(50);
        griglie.setAlignment(Pos.CENTER);

        //Crea la griglia propria
        VBox containerPropria = creaGrigliaConCoordinate(true, "La tua flotta");

        // Crea la griglia avversario (destra) con coordinate
        VBox containerAvversario = creaGrigliaConCoordinate(false, "Flotta nemica");

        griglie.getChildren().addAll(containerPropria, containerAvversario);
        gameContainer.getChildren().addAll(statoLabel, griglie);

        // Aggiungi il game container e la chat al container principale
        mainContainer.getChildren().addAll(gameContainer, chatView.getChatContainer());

        Scene scena = new Scene(mainContainer, 1500, 700);
        
        // Gestione chiusura finestra
        primaryStage.setOnCloseRequest(event -> {
            LogUtility.info("[GRIGLIA] Richiesta chiusura finestra - disconnettendo dal server...");
            
            event.consume();
            
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
                
                try {
                    if (controller.isConnesso()) {
                        controller.disconnetti();
                        LogUtility.info("[GRIGLIA] Disconnessione completata");
                    }
                } catch (Exception e) {
                    LogUtility.error("[GRIGLIA] Errore durante disconnessione: " + e.getMessage());
                }
                
                Platform.exit();
                System.exit(0);
            } else {
                LogUtility.info("[GRIGLIA] Chiusura annullata dall'utente");
            }
        });
        
        // Forza la colorazione delle navi dopo che la scena è stata creata
        Platform.runLater(() -> {
            LogUtility.info("[GRIGLIA] Forzando visualizzazione navi dopo creazione scena...");
            visualizzaNaviProprie();
        });
        
        return scena;
    }

    //Creazione UI 

    //Metodo per creare griglia di battaglia con coordinate
    private VBox creaGrigliaConCoordinate(boolean isPropria, String titolo) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        
        //Titolo della griglia
        Label titoloLabel = new Label(titolo);
        titoloLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        //Riga superiore con lettere A-J
        HBox headerRow = new HBox(2);
        headerRow.setAlignment(Pos.CENTER);
        
        //Spazio vuoto per l'angolo
        Label cornerSpace = new Label("  ");
        cornerSpace.setPrefSize(37, 25);
        cornerSpace.setAlignment(Pos.CENTER);
        headerRow.getChildren().add(cornerSpace);
        
        //Lettere
        for (char c = 'A'; c <= 'J'; c++) {
            Label coordLabel = new Label(String.valueOf(c));
            coordLabel.setPrefSize(37, 25);
            coordLabel.setMinSize(37, 25);
            coordLabel.setMaxSize(37, 25);
            coordLabel.setAlignment(Pos.CENTER);
            coordLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 12px;");
            headerRow.getChildren().add(coordLabel);
        }
        
        //Container per la griglia con numeri laterali
        HBox gridWithNumbers = new HBox(2);
        gridWithNumbers.setAlignment(Pos.CENTER);
        
        // Colonna sinistra con numeri 1-10
        VBox leftNumbers = new VBox(2);
        leftNumbers.setAlignment(Pos.CENTER);
        
        for (int i = 1; i <= 10; i++) {
            Label numLabel = new Label(String.valueOf(i));
            numLabel.setPrefSize(25, 37);
            numLabel.setMinSize(25, 37);
            numLabel.setMaxSize(25, 37);
            numLabel.setAlignment(Pos.CENTER);
            numLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 12px;");
            leftNumbers.getChildren().add(numLabel);
        }
        
        // Griglia vera e propria
        GridPane grid = creaGriglia(isPropria);
        
        gridWithNumbers.getChildren().addAll(leftNumbers, grid);
        container.getChildren().addAll(titoloLabel, headerRow, gridWithNumbers);
        
        return container;
    }

    private GridPane creaGriglia(boolean isPropria) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        int righe = 10;
        int colonne = 10;
        double cellSize = 35;
        
        StackPane[][] grigliaCorrente;
        if (isPropria) {
            celleProprie = new StackPane[righe][colonne];
            grigliaCorrente = celleProprie;
        } else {
            celleAvversario = new StackPane[righe][colonne];
            celleAttaccateAvversario = new boolean[righe][colonne];
            grigliaCorrente = celleAvversario;
        }

        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                final int riga = i;
                final int colonna = j;
                
                StackPane cella = new StackPane();
                cella.setPrefSize(cellSize, cellSize);
                cella.setMaxSize(cellSize, cellSize);
                cella.setMinSize(cellSize, cellSize);
                cella.setAlignment(Pos.CENTER);

                Rectangle sfondo = new Rectangle(cellSize, cellSize);
                if (isPropria) {
                    sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.7));
                } else {
                    sfondo.setFill(Color.LIGHTGRAY.deriveColor(0, 1, 1, 0.7));
                }
                sfondo.setStroke(Color.DARKBLUE);
                sfondo.setStrokeWidth(1);
                
                cella.getChildren().add(sfondo);
                
                if (!isPropria) {
                    Posizione posizione = new Posizione(riga, colonna);
                    cella.setOnMouseClicked(createAttackHandler(posizione));
                    
                    // Effetto hover
                    cella.setOnMouseEntered(e -> {
                        if (controller.isMioTurno() && !celleAttaccateAvversario[riga][colonna]) {
                            sfondo.setFill(Color.YELLOW.deriveColor(0, 1, 1, 0.5));
                        }
                    });
                    
                    cella.setOnMouseExited(e -> {
                        if (!celleAttaccateAvversario[riga][colonna]) {
                            sfondo.setFill(Color.LIGHTGRAY.deriveColor(0, 1, 1, 0.7));
                        }
                    });
                }

                grigliaCorrente[riga][colonna] = cella;
                grid.add(cella, colonna, riga);
            }
        }

        return grid;
    }

    //handler per gli attacchi che delega al Controller
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

    //Public interface - chiamate dal controller

    public void visualizzaNaviProprie() {
        Platform.runLater(() -> {
            LogUtility.info("[GRIGLIA] Tentativo di visualizzazione navi...");
            List<List<Posizione>> mieNavi = controller.getMieNavi();
            
            if (mieNavi != null && !mieNavi.isEmpty()) {
                LogUtility.info("[GRIGLIA] Visualizzando " + mieNavi.size() + " navi nella griglia propria");

                naviGrafiche.clear();
                
                for (int i = 0; i < mieNavi.size(); i++) {
                    List<Posizione> posizioniNave = mieNavi.get(i);
                    TipoNave tipoNave = determinaTipoNave(posizioniNave.size());

                    boolean orizzontale = posizioniNave.size() > 1 && 
                        posizioniNave.get(0).getRiga() == posizioniNave.get(1).getRiga();

                    visualizzaNave(posizioniNave, tipoNave, orizzontale);
                    
                    LogUtility.info("[GRIGLIA] Visualizzata " + tipoNave.getNome() + 
                                   " (lunghezza " + posizioniNave.size() + ") " + 
                                   (orizzontale ? "orizzontale" : "verticale"));
                }
            } else {
                LogUtility.warning("[GRIGLIA] Nessuna nave trovata per la visualizzazione");
            }
        });
    }

    private void visualizzaNave(List<Posizione> posizioni, TipoNave tipo, boolean orizzontale) {
        if (posizioni.isEmpty()) return;
        
        if (posizioni.size() == 1) {
            // Nave singola cella (per debug)
            Posizione pos = posizioni.get(0);
            NaveGraphics nave = new NaveGraphics(tipo, true, 35);
            StackPane.setAlignment(nave, Pos.CENTER);
            celleProprie[pos.getRiga()][pos.getColonna()].getChildren().add(nave);
            naviGrafiche.put(pos, nave);
        } else {
            for (int i = 0; i < posizioni.size(); i++) {
                Posizione pos = posizioni.get(i);
            
                NaveGraphics segmento = new NaveGraphics(tipo, orizzontale, 35);
                
                StackPane.setAlignment(segmento, Pos.CENTER);

                if (i == 0) {
                    segmento.setId("prua");
                } else if (i == posizioni.size() - 1) {
                    segmento.setId("poppa");
                } else {
                    segmento.setId("centro");
                }
                
                celleProprie[pos.getRiga()][pos.getColonna()].getChildren().add(segmento);
                naviGrafiche.put(pos, segmento);
            }
        }
    }
    
    private TipoNave determinaTipoNave(int lunghezza) {
        return switch (lunghezza) {
            case 5 -> TipoNave.PORTAEREI;
            case 4 -> TipoNave.INCROCIATORE;
            case 3 -> TipoNave.CACCIATORPEDINIERE;
            case 2 -> TipoNave.SOTTOMARINO;
            default -> TipoNave.SOTTOMARINO; // Fallback
        };
    }

    public void aggiornaStatoGioco(String stato) {
        Platform.runLater(() -> {
            statoLabel.setText(stato);
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

    public void attivaGrigliaAvversario(boolean attiva) {
        Platform.runLater(() -> {
            for (int i = 0; i < celleAvversario.length; i++) {
                for (int j = 0; j < celleAvversario[i].length; j++) {
                    StackPane cella = celleAvversario[i][j];
                    if (attiva && !celleAttaccateAvversario[i][j]) {
                        // Riattiva solo le celle non ancora attaccate
                        Posizione pos = new Posizione(i, j);
                        cella.setOnMouseClicked(createAttackHandler(pos));
                        cella.setOpacity(1.0);
                    } else {
                        cella.setOnMouseClicked(null);
                        cella.setOpacity(attiva ? 1.0 : 0.7);
                    }
                }
            }
        });
    }

    public void aggiornaCellaAvversario(RisultatoAttacco risultato) {
        Platform.runLater(() -> {
            Posizione pos = risultato.getPosizione();
            StackPane cella = celleAvversario[pos.getRiga()][pos.getColonna()];

            celleAttaccateAvversario[pos.getRiga()][pos.getColonna()] = true;
            
            Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
            
            if (risultato.isColpito()) {
                if (risultato.isNaveAffondata()) {
                    sfondo.setFill(Color.DARKRED);
                    aggiungiEffettoAffondamento(cella);
                } else {
                    sfondo.setFill(Color.RED);
                    aggiungiEffettoColpo(cella);
                }
            } else {
                sfondo.setFill(Color.DARKBLUE);
                aggiungiEffettoSplash(cella);
            }
            cella.setOnMouseClicked(null);
        });
    }
    
    public void aggiornaCellaPropria(RisultatoAttacco risultato) {
        Platform.runLater(() -> {
            Posizione pos = risultato.getPosizione();
            StackPane cella = celleProprie[pos.getRiga()][pos.getColonna()];
            
            if (risultato.isColpito()) {
                NaveGraphics nave = naviGrafiche.get(pos);
                if (nave != null) {
                    if (risultato.isNaveAffondata()) {
                        nave.mostraAffondata();
                    } else {
                        nave.mostraColpita();
                    }
                }
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                if (risultato.isNaveAffondata()) {
                    sfondo.setFill(Color.DARKRED.deriveColor(0, 1, 1, 0.5));
                } else {
                    sfondo.setFill(Color.ORANGE.deriveColor(0, 1, 1, 0.5));
                }
            } else {
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                sfondo.setFill(Color.CYAN.deriveColor(0, 1, 1, 0.3));
                aggiungiEffettoSplash(cella);
            }
        });
    }

    //Effetti visivi
    private void aggiungiEffettoColpo(StackPane cella) {
        Circle esplosione = new Circle(8);
        esplosione.setFill(Color.YELLOW);
        esplosione.setStroke(Color.RED);
        esplosione.setStrokeWidth(2);
        StackPane.setAlignment(esplosione, Pos.CENTER);
        cella.getChildren().add(esplosione);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), esplosione);
        scale.setFromX(0.1);
        scale.setFromY(0.1);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }
    
    private void aggiungiEffettoAffondamento(StackPane cella) {
        // Prima l'esplosione
        aggiungiEffettoColpo(cella);
        
        Platform.runLater(() -> {
            Text simbolo = new Text("💀");
            simbolo.setStyle("-fx-font-size: 16px;");
            
            StackPane.setAlignment(simbolo, Pos.CENTER);
            cella.getChildren().add(simbolo);
        });
    }
    
    private void aggiungiEffettoSplash(StackPane cella) {
        Circle splash = new Circle(6);
        splash.setFill(Color.LIGHTBLUE);
        splash.setStroke(Color.BLUE);
        splash.setStrokeWidth(1);
        
        StackPane.setAlignment(splash, Pos.CENTER);
        cella.getChildren().add(splash);
        
        FadeTransition fade = new FadeTransition(Duration.millis(500), splash);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.play();
    }

    public void gestisciVittoria(String messaggio) {
        Platform.runLater(() -> {
            statoLabel.setText("🎉 " + messaggio + " 🎉");
            statoLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 20px; -fx-font-weight: bold;");
            disabilitaGriglia();
            
            mostraPopupVittoria(messaggio);
        });
    }

    public void gestisciSconfitta(String messaggio) {
        Platform.runLater(() -> {
            statoLabel.setText("💀 " + messaggio + " 💀");
            statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold;");
            disabilitaGriglia();
            
            mostraPopupSconfitta(messaggio);
        });
    }

    public void mostraErrore(String errore) {
        Platform.runLater(() -> {
            statoLabel.setText("❌ " + errore);
            statoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");
        });
    }

    private void mostraPopupVittoria(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🏆 VITTORIA!");
        alert.setHeaderText("Complimenti, hai vinto!");
        alert.setContentText(messaggio + "\n\nGrazie per aver giocato!");
        
        ButtonType chiudiButton = new ButtonType("Chiudi Gioco", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(chiudiButton);
        
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("victory-dialog");
        } catch (Exception e) {
            LogUtility.warning("[GRIGLIA] Impossibile caricare CSS per dialog vittoria: " + e.getMessage());
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        
        result.ifPresent(buttonType -> {
            if (buttonType == chiudiButton) {
                disconnettiEChiudi();
            }
        });
    }

    private void mostraPopupSconfitta(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("💀 SCONFITTA");
        alert.setHeaderText("Hai perso la battaglia...");
        alert.setContentText(messaggio + "\n\nGrazie per aver giocato!");
        
        ButtonType chiudiButton = new ButtonType("Chiudi Gioco", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(chiudiButton);
        
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("defeat-dialog");
        } catch (Exception e) {
            LogUtility.warning("[GRIGLIA] Impossibile caricare CSS per dialog sconfitta: " + e.getMessage());
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        
        result.ifPresent(buttonType -> {
            if (buttonType == chiudiButton) {
                disconnettiEChiudi();
            }
        });
    }

    private void disconnettiEChiudi() {
        try {
            LogUtility.info("[GRIGLIA] Disconnessione e chiusura applicazione...");
            
            if (controller.isConnesso()) {
                controller.disconnetti();
                LogUtility.info("[GRIGLIA] Disconnessione completata");
            }
        } catch (Exception e) {
            LogUtility.error("[GRIGLIA] Errore durante disconnessione: " + e.getMessage());
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    private void disabilitaGriglia() {
        for (int i = 0; i < celleAvversario.length; i++) {
            for (int j = 0; j < celleAvversario[i].length; j++) {
                celleAvversario[i][j].setOnMouseClicked(null);
                celleAvversario[i][j].setOpacity(0.5);
            }
        }
    }
    
    public ChatView getChatView() {
        return chatView;
    }
    public void coloraNaviProprie() {
        visualizzaNaviProprie();
    }

    public boolean isInizializzata() {
        return celleProprie != null && celleAvversario != null && 
               celleAttaccateAvversario != null && statoLabel != null;
    }

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

    public void resetGriglia() {
        Platform.runLater(() -> {
            LogUtility.info("[GRIGLIA] Reset griglia in corso...");
            
            // Reset griglia avversario
            if (celleAvversario != null && celleAttaccateAvversario != null) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        StackPane cella = celleAvversario[i][j];
                        
                        if (cella.getChildren().size() > 1) {
                            cella.getChildren().subList(1, cella.getChildren().size()).clear();
                        }
                        Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                        sfondo.setFill(Color.LIGHTGRAY.deriveColor(0, 1, 1, 0.7));
                        cella.setOpacity(1.0);
                        cella.setOnMouseClicked(null);
                        celleAttaccateAvversario[i][j] = false;
                    }
                }
            }
            
            if (celleProprie != null) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        StackPane cella = celleProprie[i][j];

                        if (cella.getChildren().size() > 1) {
                            cella.getChildren().subList(1, cella.getChildren().size()).clear();
                        }
                        Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                        sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.7));
                        cella.setOpacity(1.0);
                    }
                }
            }
            
            naviGrafiche.clear();
            
            // Reset stato
            if (statoLabel != null) {
                statoLabel.setText("Griglia resettata");
                statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
            
            LogUtility.info("[GRIGLIA] Reset griglia completato");
        });
    }
}