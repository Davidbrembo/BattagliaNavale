package client.view;

import client.controller.GiocoController;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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

/**
 * View per il posizionamento delle navi.
 * Segue il pattern MVC delegando la logica al Controller.
 */
public class PosizionamentoNaviView {

    private Rectangle[][] celle;
    private Label statoLabel;
    private Button confermaButton;
    private List<List<Posizione>> naviPosizionate = new ArrayList<>();
    private List<Posizione> naviOccupate = new ArrayList<>();
    private int naviRimanenti = 3; // Numero di navi da posizionare
    private GiocoController controller;
    private Stage primaryStage;

    public PosizionamentoNaviView() {
        this.controller = GiocoController.getInstance();
    }

    public Scene creaScena(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 20px;");

        // Titolo
        Label titoloLabel = new Label("🚢 POSIZIONA LE TUE NAVI");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        // Istruzioni
        VBox istruzioniBox = new VBox(5);
        istruzioniBox.setAlignment(Pos.CENTER);
        
        Label istruzioni1 = new Label("🖱️ Click sinistro: nave orizzontale");
        istruzioni1.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
        
        Label istruzioni2 = new Label("🖱️ Click destro: nave verticale");
        istruzioni2.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
        
        Label istruzioni3 = new Label("📏 Ogni nave è lunga 3 caselle");
        istruzioni3.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        istruzioniBox.getChildren().addAll(istruzioni1, istruzioni2, istruzioni3);

        // Label per lo stato
        statoLabel = new Label("Navi da posizionare: " + naviRimanenti + "/3");
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Crea la griglia
        GridPane grid = creaGrigliaPosizionamento();

        // Pulsanti
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button resetButton = new Button("🔄 Reset");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #FF5722; -fx-text-fill: white;");
        resetButton.setOnAction(e -> resetGriglia());

        confermaButton = new Button("✅ Conferma Posizionamento");
        confermaButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        confermaButton.setDisable(true);
        confermaButton.setOnAction(e -> {
            inviaNaviAlController();
            mostraAttesaAvversario();
        });

        buttonBox.getChildren().addAll(resetButton, confermaButton);

        root.getChildren().addAll(titoloLabel, istruzioniBox, statoLabel, grid, buttonBox);

        Scene scena = new Scene(root, 900, 800);
        scena.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
        return scena;
    }

    private GridPane creaGrigliaPosizionamento() {
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
                Rectangle cella = new Rectangle(35, 35);
                cella.setFill(Color.LIGHTBLUE);
                cella.setStroke(Color.BLACK);
                cella.setStrokeWidth(1);

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

                // Effetto hover
                cella.setOnMouseEntered(e -> {
                    if (naviRimanenti > 0 && !naviOccupate.contains(new Posizione(riga, colonna))) {
                        cella.setFill(Color.LIGHTYELLOW);
                    }
                });

                cella.setOnMouseExited(e -> {
                    if (!naviOccupate.contains(new Posizione(riga, colonna))) {
                        cella.setFill(Color.LIGHTBLUE);
                    }
                });

                celle[i][j] = cella;
                grid.add(cella, j, i);
            }
        }

        return grid;
    }

    private void posizionaNave(int riga, int colonna, boolean orizzontale) {
        List<Posizione> posizioniNave = new ArrayList<>();
        
        // Calcola le posizioni della nave
        for (int i = 0; i < 3; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            // Controlla se la posizione è valida
            if (nuovaRiga >= 10 || nuovaColonna >= 10) {
                mostraErrore("⚠️ Nave fuori dai limiti della griglia!");
                return;
            }
            
            Posizione pos = new Posizione(nuovaRiga, nuovaColonna);
            
            // Controlla se la posizione è già occupata
            if (naviOccupate.contains(pos)) {
                mostraErrore("⚠️ Posizione già occupata da un'altra nave!");
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
            statoLabel.setText("Navi da posizionare: " + naviRimanenti + "/3");
            statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        } else {
            statoLabel.setText("✅ Tutte le navi posizionate! Clicca Conferma per continuare.");
            statoLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 18px; -fx-font-weight: bold;");
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
        aggiornaStato();
    }

    /**
     * Invia le navi al Controller che le gestirà
     */
    private void inviaNaviAlController() {
        controller.inviaPosizionamentoNavi(naviPosizionate);
    }

    /**
     * Mostra la schermata di attesa dell'avversario
     */
    private void mostraAttesaAvversario() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");

        Label titoloLabel = new Label("🎯 Navi Posizionate!");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label attesaLabel = new Label("⏳ Attendere che l'avversario posizioni le sue navi...");
        attesaLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 18px; -fx-font-style: italic;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);
        progressIndicator.setStyle("-fx-progress-color: #4CAF50;");

        Label statusLabel = new Label("Sincronizzazione in corso...");
        statusLabel.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");

        root.getChildren().addAll(titoloLabel, attesaLabel, progressIndicator, statusLabel);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
        primaryStage.setScene(scene);

        // Avvia l'ascolto per l'inizio battaglia
        avviaAscoltoInizioBattaglia();
    }

    /**
     * Avvia l'ascolto per il messaggio INIZIO_BATTAGLIA
     */
    private void avviaAscoltoInizioBattaglia() {
        // Registra il callback nel Controller invece di creare un thread separato
        controller.setOnInizioBattagliaCallback(() -> mostraGrigliaAttacco());
    }

    /**
     * Passa alla griglia di attacco
     */
    private void mostraGrigliaAttacco() {
        server.model.ServerGameManager gameManager = new server.model.ServerGameManager(10, 10);
        GrigliaView grigliaView = new GrigliaView(gameManager);
        Scene scenaGriglia = grigliaView.creaScena(primaryStage);
        primaryStage.setScene(scenaGriglia);
    }

    /**
     * Mostra errore di connessione
     */
    private void mostraErroreConnessione() {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");
        root.setAlignment(Pos.CENTER);

        Label erroreLabel = new Label("❌ Connessione Persa");
        erroreLabel.setStyle("-fx-text-fill: red; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label descrizioneLabel = new Label("La connessione al server è stata interrotta durante il posizionamento.");
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
        primaryStage.setScene(scene);
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Errore Posizionamento");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}