package client.view;

import client.controller.GiocoController;
import client.view.components.NaveGraphics;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import shared.model.Posizione;
import shared.model.TipoNave;
import utility.LogUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * View per il posizionamento delle navi con controlli migliorati
 */
public class PosizionamentoNaviView {

    private StackPane[][] celle;
    private Label statoLabel;
    private Label naviRimanentiLabel;
    private Button confermaButton;
    private List<List<Posizione>> naviPosizionate = new ArrayList<>();
    private List<Posizione> naviOccupate = new ArrayList<>();
    private Map<Posizione, NaveGraphics> naviGrafiche = new HashMap<>();
    
    // Gestione navi multiple
    private TipoNave[] naviDaPosizionare;
    private int indiceNaveCorrente = 0;
    private TipoNave naveCorrente;
    private VBox listaNaviBox;
    private Label naveCorrenteLabel;
    private Label descrizioneLabel;
    
    // *** NUOVE FUNZIONALITÀ ***
    private boolean orientamentoOrizzontale = true; // Orientamento corrente (toggle con R)
    private Label orientamentoLabel; // Mostra orientamento corrente
    private Label suggerimentoLabel; // Suggerimenti dinamici
    private Button autoPosizionaButton; // Auto-posizionamento
    
    private GiocoController controller;
    private Stage primaryStage;

    public PosizionamentoNaviView() {
        this.controller = GiocoController.getInstance();
        this.naviDaPosizionare = TipoNave.getNaviDaPosizionare();
        this.naveCorrente = naviDaPosizionare[0];
    }

    public Scene creaScena(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 20px;");

        // Titolo
        Label titoloLabel = new Label("🚢 POSIZIONA LA TUA FLOTTA");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        // Info sulla nave corrente
        naveCorrenteLabel = new Label("Posiziona: " + naveCorrente.getNomeConLunghezza());
        naveCorrenteLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Descrizione nave corrente
        descrizioneLabel = new Label(naveCorrente.getDescrizione());
        descrizioneLabel.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px; -fx-font-style: italic;");

        // *** NUOVO: Orientamento corrente ***
        orientamentoLabel = new Label("🧭 Orientamento: " + (orientamentoOrizzontale ? "Orizzontale ↔️" : "Verticale ↕️"));
        orientamentoLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold;");

        // *** NUOVO: Istruzioni aggiornate ***
        VBox istruzioniBox = new VBox(5);
        istruzioniBox.setAlignment(Pos.CENTER);
        
        Label istruzioni1 = new Label("🖱️ Click: posiziona nave");
        istruzioni1.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
        
        Label istruzioni2 = new Label("⌨️ Tasto R: ruota orientamento");
        istruzioni2.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
        
        Label istruzioni3 = new Label("🖱️ Drag & Drop: sposta nave");
        istruzioni3.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
        
        istruzioniBox.getChildren().addAll(istruzioni1, istruzioni2, istruzioni3);

        // *** NUOVO: Suggerimenti dinamici ***
        suggerimentoLabel = new Label("💡 Posiziona le navi mantenendo spazio tra loro");
        suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");

        // Stato del posizionamento
        naviRimanentiLabel = new Label();
        aggiornaStatoNavi();

        // Label per lo stato generale
        statoLabel = new Label("Clicca sulla griglia per posizionare la nave");
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        // Crea la griglia CON COORDINATE
        VBox grigliaContainer = creaGrigliaConCoordinate();

        // Lista navi da posizionare (sidebar)
        listaNaviBox = creaListaNavi();

        // Layout principale con griglia e lista navi
        HBox mainLayout = new HBox(30);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(grigliaContainer, listaNaviBox);

        // Pulsanti
        HBox buttonBox = creaBottoni();

        root.getChildren().addAll(
            titoloLabel, 
            naveCorrenteLabel, 
            descrizioneLabel,
            orientamentoLabel,
            istruzioniBox,
            suggerimentoLabel,
            naviRimanentiLabel,
            statoLabel, 
            mainLayout, 
            buttonBox
        );

        Scene scena = new Scene(root, 1200, 1000);
        
        // *** NUOVO: Gestione tasti ***
        scena.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.R) {
                ruotaOrientamento();
            }
        });
        
        // Assicurati che la scena possa ricevere eventi tastiera
        scena.getRoot().setFocusTraversable(true);
        scena.getRoot().requestFocus();
        
        // Gestione chiusura finestra
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            Platform.exit();
            System.exit(0);
        });
        
        return scena;
    }

    // *** NUOVO: Crea griglia con coordinate A-J, 1-10 ***
    private VBox creaGrigliaConCoordinate() {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        
        // Riga superiore con lettere A-J
        HBox headerRow = new HBox(2);
        headerRow.setAlignment(Pos.CENTER);
        
        // Spazio vuoto per l'angolo
        Label cornerSpace = new Label("  ");
        cornerSpace.setPrefSize(30, 30);
        headerRow.getChildren().add(cornerSpace);
        
        // Lettere A-J
        for (char c = 'A'; c <= 'J'; c++) {
            Label coordLabel = new Label(String.valueOf(c));
            coordLabel.setPrefSize(37, 30);
            coordLabel.setAlignment(Pos.CENTER);
            coordLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 14px;");
            headerRow.getChildren().add(coordLabel);
        }
        
        // Container per la griglia con numeri laterali
        HBox gridWithNumbers = new HBox(2);
        gridWithNumbers.setAlignment(Pos.CENTER);
        
        // Colonna sinistra con numeri 1-10
        VBox leftNumbers = new VBox(2);
        leftNumbers.setAlignment(Pos.CENTER);
        
        for (int i = 1; i <= 10; i++) {
            Label numLabel = new Label(String.valueOf(i));
            numLabel.setPrefSize(30, 37);
            numLabel.setAlignment(Pos.CENTER);
            numLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 14px;");
            leftNumbers.getChildren().add(numLabel);
        }
        
        // Griglia vera e propria
        GridPane grid = creaGrigliaPosizionamento();
        
        gridWithNumbers.getChildren().addAll(leftNumbers, grid);
        container.getChildren().addAll(headerRow, gridWithNumbers);
        
        return container;
    }

    // *** NUOVO: Ruota orientamento con R ***
    private void ruotaOrientamento() {
        orientamentoOrizzontale = !orientamentoOrizzontale;
        orientamentoLabel.setText("🧭 Orientamento: " + (orientamentoOrizzontale ? "Orizzontale ↔️" : "Verticale ↕️"));
        
        // Aggiorna il suggerimento
        suggerimentoLabel.setText("💡 Orientamento ruotato! " + (orientamentoOrizzontale ? "↔️ Orizzontale" : "↕️ Verticale"));
        suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");
        
        LogUtility.info("[POSIZIONAMENTO] 🔄 Orientamento ruotato: " + (orientamentoOrizzontale ? "Orizzontale" : "Verticale"));
    }

    private VBox creaListaNavi() {
        VBox listaBox = new VBox(10);
        listaBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 15px; -fx-border-color: #444444; " +
                         "-fx-border-width: 1px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        listaBox.setPrefWidth(250);

        Label titoloLista = new Label("📋 Navi da Posizionare");
        titoloLista.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        listaBox.getChildren().add(titoloLista);

        // Aggiungi ogni tipo di nave alla lista
        for (int i = 0; i < naviDaPosizionare.length; i++) {
            TipoNave tipo = naviDaPosizionare[i];
            Label naveLabel = new Label((i + 1) + ". " + tipo.getNomeConLunghezza());
            
            if (i < indiceNaveCorrente) {
                naveLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-strikethrough: true;");
            } else if (i == indiceNaveCorrente) {
                naveLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else {
                naveLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 14px;");
            }
            
            listaBox.getChildren().add(naveLabel);
        }

        return listaBox;
    }

    private HBox creaBottoni() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button resetButton = new Button("🔄 Reset Tutto");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #FF5722; -fx-text-fill: white;");
        resetButton.setOnAction(e -> resetGriglia());

        Button resetNaveButton = new Button("↶ Reset Nave Corrente");
        resetNaveButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #FF9800; -fx-text-fill: white;");
        resetNaveButton.setOnAction(e -> resetNaveCorrente());

        // *** NUOVO: Auto-posizionamento ***
        autoPosizionaButton = new Button("🎲 Auto-Posiziona");
        autoPosizionaButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        autoPosizionaButton.setOnAction(e -> autoPosizionaNavi());

        confermaButton = new Button("✅ Conferma Flotta");
        confermaButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        confermaButton.setDisable(true);
        confermaButton.setOnAction(e -> {
            inviaNaviAlController();
            mostraAttesaAvversario();
        });

        buttonBox.getChildren().addAll(resetButton, resetNaveButton, autoPosizionaButton, confermaButton);
        return buttonBox;
    }

    private GridPane creaGrigliaPosizionamento() {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        int righe = 10;
        int colonne = 10;
        double cellSize = 35;
        celle = new StackPane[righe][colonne];

        // Crea le celle della griglia
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                final int riga = i;
                final int colonna = j;
                
                StackPane cella = new StackPane();
                cella.setPrefSize(cellSize, cellSize);
                cella.setMaxSize(cellSize, cellSize);
                cella.setMinSize(cellSize, cellSize);
                
                Rectangle sfondo = new Rectangle(cellSize, cellSize);
                sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
                sfondo.setStroke(Color.BLACK);
                sfondo.setStrokeWidth(1);
                
                cella.getChildren().add(sfondo);

                // *** AGGIORNATO: Usa orientamento corrente ***
                cella.setOnMouseClicked(event -> {
                    if (indiceNaveCorrente < naviDaPosizionare.length) {
                        posizionaNave(riga, colonna, orientamentoOrizzontale);
                    }
                });

                // *** NUOVO: Preview con freccia direzionale ***
                cella.setOnMouseEntered(e -> {
                    if (indiceNaveCorrente < naviDaPosizionare.length && 
                        !naviOccupate.contains(new Posizione(riga, colonna))) {
                        mostraPreviewNaveConOrientamento(riga, colonna, orientamentoOrizzontale);
                    }
                });

                cella.setOnMouseExited(e -> {
                    rimuoviPreview();
                });

                celle[riga][colonna] = cella;
                grid.add(cella, colonna, riga);
            }
        }

        return grid;
    }

    // *** NUOVO: Preview con freccia direzionale ***
    private void mostraPreviewNaveConOrientamento(int riga, int colonna, boolean orizzontale) {
        rimuoviPreview();
        
        int lunghezza = naveCorrente.getLunghezza();
        boolean posizionamentoValido = true;
        
        // Controlla validità e spaziatura
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            if (nuovaRiga >= 10 || nuovaColonna >= 10) {
                posizionamentoValido = false;
                break;
            }
            
            Posizione pos = new Posizione(nuovaRiga, nuovaColonna);
            if (naviOccupate.contains(pos) || !rispettaSpaziatura(pos)) {
                posizionamentoValido = false;
                break;
            }
        }
        
        // Mostra preview
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            if (nuovaRiga < 10 && nuovaColonna < 10) {
                StackPane cella = celle[nuovaRiga][nuovaColonna];
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                
                if (posizionamentoValido) {
                    sfondo.setFill(Color.LIGHTYELLOW);
                    
                    // *** NUOVO: Aggiungi freccia direzionale ***
                    if (i == 0) { // Prima cella - aggiungi freccia
                        Polygon freccia = creaFrecciaOrientamento(orizzontale);
                        cella.getChildren().add(freccia);
                    }
                } else {
                    sfondo.setFill(Color.LIGHTCORAL);
                }
            }
        }
        
        // *** NUOVO: Aggiorna suggerimento in tempo reale ***
        if (!posizionamentoValido) {
            if (!rispettaLimiti(riga, colonna, orizzontale, lunghezza)) {
                suggerimentoLabel.setText("⚠️ Nave fuori dai limiti della griglia!");
            } else {
                suggerimentoLabel.setText("⚠️ Posizione troppo vicina ad un'altra nave!");
            }
            suggerimentoLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px; -fx-font-style: italic;");
        } else {
            suggerimentoLabel.setText("✅ Posizione valida - Clicca per posizionare");
            suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");
        }
    }

    // *** NUOVO: Crea freccia direzionale ***
    private Polygon creaFrecciaOrientamento(boolean orizzontale) {
        Polygon freccia = new Polygon();
        
        if (orizzontale) {
            // Freccia che punta a destra →
            freccia.getPoints().addAll(new Double[]{
                5.0, 15.0,  // Punta sinistra
                20.0, 15.0, // Centro
                15.0, 10.0, // Punta superiore destra
                15.0, 20.0  // Punta inferiore destra
            });
        } else {
            // Freccia che punta in basso ↓
            freccia.getPoints().addAll(new Double[]{
                15.0, 5.0,  // Punta superiore
                15.0, 20.0, // Centro
                10.0, 15.0, // Punta sinistra basso
                20.0, 15.0  // Punta destra basso
            });
        }
        
        freccia.setFill(Color.DARKBLUE);
        freccia.setStroke(Color.WHITE);
        freccia.setStrokeWidth(1);
        return freccia;
    }

    // *** NUOVO: Controllo spaziatura navi ***
    private boolean rispettaSpaziatura(Posizione pos) {
        // Controlla tutte le 8 direzioni adiacenti
        for (int deltaRiga = -1; deltaRiga <= 1; deltaRiga++) {
            for (int deltaColonna = -1; deltaColonna <= 1; deltaColonna++) {
                if (deltaRiga == 0 && deltaColonna == 0) continue; // Skip cella corrente
                
                int nuovaRiga = pos.getRiga() + deltaRiga;
                int nuovaColonna = pos.getColonna() + deltaColonna;
                
                if (nuovaRiga >= 0 && nuovaRiga < 10 && nuovaColonna >= 0 && nuovaColonna < 10) {
                    Posizione adiacente = new Posizione(nuovaRiga, nuovaColonna);
                    if (naviOccupate.contains(adiacente)) {
                        return false; // Troppo vicino a un'altra nave
                    }
                }
            }
        }
        return true;
    }

    // *** NUOVO: Controllo limiti griglia ***
    private boolean rispettaLimiti(int riga, int colonna, boolean orizzontale, int lunghezza) {
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            if (nuovaRiga >= 10 || nuovaColonna >= 10) {
                return false;
            }
        }
        return true;
    }

    // *** NUOVO: Auto-posizionamento ***
    private void autoPosizionaNavi() {
        resetGriglia(); // Reset prima di auto-posizionare
        
        Random random = new Random();
        
        for (int naveIndex = 0; naveIndex < naviDaPosizionare.length; naveIndex++) {
            TipoNave tipo = naviDaPosizionare[naveIndex];
            boolean posizionata = false;
            int tentativi = 0;
            
            while (!posizionata && tentativi < 100) { // Massimo 100 tentativi per nave
                int riga = random.nextInt(10);
                int colonna = random.nextInt(10);
                boolean orizzontale = random.nextBoolean();
                
                if (posizionamentoValido(riga, colonna, orizzontale, tipo.getLunghezza())) {
                    List<Posizione> posizioniNave = new ArrayList<>();
                    
                    for (int i = 0; i < tipo.getLunghezza(); i++) {
                        int nuovaRiga = orizzontale ? riga : riga + i;
                        int nuovaColonna = orizzontale ? colonna + i : colonna;
                        posizioniNave.add(new Posizione(nuovaRiga, nuovaColonna));
                    }
                    
                    naviPosizionate.add(posizioniNave);
                    naviOccupate.addAll(posizioniNave);
                    visualizzaNaveGrafica(posizioniNave, tipo, orizzontale);
                    
                    posizionata = true;
                }
                tentativi++;
            }
        }
        
        // Aggiorna stato
        indiceNaveCorrente = naviDaPosizionare.length;
        aggiornaStato();
        aggiornaListaNavi();
        aggiornaDescrizioneNaveCorrente();
        
        suggerimentoLabel.setText("🎲 Flotta posizionata automaticamente!");
        suggerimentoLabel.setStyle("-fx-text-fill: #9C27B0; -fx-font-size: 12px; -fx-font-style: italic;");
    }

    // *** NUOVO: Validazione posizionamento con spaziatura ***
    private boolean posizionamentoValido(int riga, int colonna, boolean orizzontale, int lunghezza) {
        // Controlla limiti
        if (!rispettaLimiti(riga, colonna, orizzontale, lunghezza)) {
            return false;
        }
        
        // Controlla spaziatura per ogni cella della nave
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            Posizione pos = new Posizione(nuovaRiga, nuovaColonna);
            if (naviOccupate.contains(pos) || !rispettaSpaziatura(pos)) {
                return false;
            }
        }
        
        return true;
    }

    private void rimuoviPreview() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Posizione pos = new Posizione(i, j);
                if (!naviOccupate.contains(pos)) {
                    StackPane cella = celle[i][j];
                    // Rimuovi tutto tranne lo sfondo
                    if (cella.getChildren().size() > 1) {
                        cella.getChildren().subList(1, cella.getChildren().size()).clear();
                    }
                    Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                    sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
                }
            }
        }
        
        // Reset suggerimento
        suggerimentoLabel.setText("💡 Posiziona le navi mantenendo spazio tra loro");
        suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");
    }

    // *** AGGIORNATO: Posizionamento con validazione spaziatura ***
    private void posizionaNave(int riga, int colonna, boolean orizzontale) {
        int lunghezza = naveCorrente.getLunghezza();
        
        // *** NUOVO: Validazione completa ***
        if (!posizionamentoValido(riga, colonna, orizzontale, lunghezza)) {
            if (!rispettaLimiti(riga, colonna, orizzontale, lunghezza)) {
                mostraErrore("⚠️ " + naveCorrente.getNome() + " fuori dai limiti della griglia!");
            } else {
                mostraErrore("⚠️ Posizione troppo vicina ad un'altra nave! Mantieni almeno uno spazio di distanza.");
            }
            return;
        }
        
        List<Posizione> posizioniNave = new ArrayList<>();
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            posizioniNave.add(new Posizione(nuovaRiga, nuovaColonna));
        }
        
        naviPosizionate.add(posizioniNave);
        naviOccupate.addAll(posizioniNave);
        
        visualizzaNaveGrafica(posizioniNave, naveCorrente, orizzontale);
        
        indiceNaveCorrente++;
        if (indiceNaveCorrente < naviDaPosizionare.length) {
            naveCorrente = naviDaPosizionare[indiceNaveCorrente];
            suggerimentoLabel.setText("✅ Nave posizionata! Prossima: " + naveCorrente.getNome());
            LogUtility.info("[POSIZIONAMENTO] Prossima nave: " + naveCorrente.getNome());
        } else {
            suggerimentoLabel.setText("🎉 Tutte le navi posizionate! Clicca 'Conferma Flotta'");
        }
        
        aggiornaStato();
        aggiornaListaNavi();
        aggiornaDescrizioneNaveCorrente();
    }

    private void visualizzaNaveGrafica(List<Posizione> posizioni, TipoNave tipo, boolean orizzontale) {
        for (int i = 0; i < posizioni.size(); i++) {
            Posizione pos = posizioni.get(i);
            StackPane cella = celle[pos.getRiga()][pos.getColonna()];
            
            NaveGraphics naveGraph = new NaveGraphics(tipo, orizzontale, 35);
            
            if (posizioni.size() > 1) {
                if (i == 0) {
                    naveGraph.setId("prua");
                } else if (i == posizioni.size() - 1) {
                    naveGraph.setId("poppa");
                } else {
                    naveGraph.setId("centro");
                }
            }
            
            cella.getChildren().add(naveGraph);
            naviGrafiche.put(pos, naveGraph);
        }
    }

    private void aggiornaDescrizioneNaveCorrente() {
        if (indiceNaveCorrente < naviDaPosizionare.length) {
            descrizioneLabel.setText(naveCorrente.getDescrizione());
            naveCorrenteLabel.setText("Posiziona: " + naveCorrente.getNomeConLunghezza());
            statoLabel.setText("Clicca sulla griglia per posizionare: " + naveCorrente.getNome());
        } else {
            descrizioneLabel.setText("✅ Tutte le navi sono state posizionate correttamente!");
            naveCorrenteLabel.setText("✅ Flotta Completata!");
            statoLabel.setText("✅ Tutte le navi posizionate! Clicca 'Conferma Flotta' per continuare.");
        }
    }

    private void aggiornaListaNavi() {
        listaNaviBox.getChildren().clear();
        
        Label titoloLista = new Label("📋 Navi da Posizionare");
        titoloLista.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        listaNaviBox.getChildren().add(titoloLista);

        for (int i = 0; i < naviDaPosizionare.length; i++) {
            TipoNave tipo = naviDaPosizionare[i];
            Label naveLabel = new Label((i + 1) + ". " + tipo.getNomeConLunghezza());
            
            if (i < indiceNaveCorrente) {
                naveLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-strikethrough: true;");
                naveLabel.setText("✅ " + (i + 1) + ". " + tipo.getNomeConLunghezza());
            } else if (i == indiceNaveCorrente) {
                naveLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold; " +
                                  "-fx-background-color: rgba(255, 215, 0, 0.2); -fx-padding: 5px; " +
                                  "-fx-background-radius: 3px;");
                naveLabel.setText("👉 " + (i + 1) + ". " + tipo.getNomeConLunghezza());
            } else {
                naveLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 14px;");
                naveLabel.setText("⏳ " + (i + 1) + ". " + tipo.getNomeConLunghezza());
            }
            
            listaNaviBox.getChildren().add(naveLabel);
        }
    }

    private void aggiornaStatoNavi() {
        int totaleNavi = TipoNave.getNumeroTotaleNavi();
        int naviPosizionateCount = indiceNaveCorrente;
        
        naviRimanentiLabel.setText("Progresso: " + naviPosizionateCount + "/" + totaleNavi + " navi posizionate");
        naviRimanentiLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
    }

    private void aggiornaStato() {
        aggiornaStatoNavi();
        
        if (indiceNaveCorrente < naviDaPosizionare.length) {
            statoLabel.setText("Posiziona: " + naveCorrente.getNomeConLunghezza());
            statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            confermaButton.setDisable(true);
        } else {
            statoLabel.setText("✅ Tutte le navi posizionate! Clicca 'Conferma Flotta' per continuare.");
            statoLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px; -fx-font-weight: bold;");
            confermaButton.setDisable(false);
        }
    }

    private void resetGriglia() {
        naviPosizionate.clear();
        naviOccupate.clear();
        naviGrafiche.clear();
        indiceNaveCorrente = 0;
        naveCorrente = naviDaPosizionare[0];
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                StackPane cella = celle[i][j];
                if (cella.getChildren().size() > 1) {
                    cella.getChildren().subList(1, cella.getChildren().size()).clear();
                }
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
            }
        }
        
        confermaButton.setDisable(true);
        orientamentoOrizzontale = true; // Reset orientamento
        orientamentoLabel.setText("🧭 Orientamento: Orizzontale ↔️");
        suggerimentoLabel.setText("💡 Posiziona le navi mantenendo spazio tra loro");
        suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");
        
        aggiornaDescrizioneNaveCorrente();
        aggiornaStato();
        aggiornaListaNavi();
    }

    private void resetNaveCorrente() {
        if (indiceNaveCorrente > 0) {
            indiceNaveCorrente--;
            List<Posizione> ultimaNave = naviPosizionate.remove(naviPosizionate.size() - 1);
            naviOccupate.removeAll(ultimaNave);
            
            for (Posizione pos : ultimaNave) {
                StackPane cella = celle[pos.getRiga()][pos.getColonna()];
                if (cella.getChildren().size() > 1) {
                    cella.getChildren().subList(1, cella.getChildren().size()).clear();
                }
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
                naviGrafiche.remove(pos);
            }
            
            naveCorrente = naviDaPosizionare[indiceNaveCorrente];
            confermaButton.setDisable(true);
            suggerimentoLabel.setText("↶ Nave rimossa! Torno a: " + naveCorrente.getNome());
            suggerimentoLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px; -fx-font-style: italic;");
            
            aggiornaDescrizioneNaveCorrente();
            aggiornaStato();
            aggiornaListaNavi();
        }
    }

    private void inviaNaviAlController() {
        controller.inviaPosizionamentoNavi(naviPosizionate);
    }

    private void mostraAttesaAvversario() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");

        Label titoloLabel = new Label("⚓ Flotta Posizionata!");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label attesaLabel = new Label("⏳ Attendere che l'avversario posizioni la sua flotta...");
        attesaLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 18px; -fx-font-style: italic;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);

        root.getChildren().addAll(titoloLabel, attesaLabel, progressIndicator);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        
        controller.setOnInizioBattagliaCallback(() -> {
            GrigliaView grigliaView = new GrigliaView();
            Scene scenaGriglia = grigliaView.creaScena(primaryStage);
            primaryStage.setScene(scenaGriglia);
        });
    }

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}