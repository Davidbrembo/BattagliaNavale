package client.view;

import client.controller.GiocoController;
import client.view.components.NaveGraphics;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import shared.model.Posizione;
import shared.model.TipoNave;
import utility.AudioManager;
import utility.LogUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PosizionamentoNaviView {

    private StackPane[][] celle;
    private Label statoLabel;
    private Label naviRimanentiLabel;
    private Button confermaButton;
    private List<List<Posizione>> naviPosizionate = new ArrayList<>();
    private List<Posizione> naviOccupate = new ArrayList<>();
    private Map<Posizione, NaveGraphics> naviGrafiche = new HashMap<>();
    
    private TipoNave[] naviDaPosizionare;
    private int indiceNaveCorrente = 0;
    private TipoNave naveCorrente;
    private VBox listaNaviBox;
    private Label naveCorrenteLabel;
    private Label descrizioneLabel;
    
    private boolean orientamentoOrizzontale = true;
    private Label orientamentoLabel;
    private Label suggerimentoLabel;
    private Button autoPosizionaButton;
    
    private GiocoController controller;
    private AudioManager audioManager;
    private Stage primaryStage;

    public PosizionamentoNaviView() {
        this.controller = GiocoController.getInstance();
        this.audioManager = AudioManager.getInstance();
        this.naviDaPosizionare = TipoNave.getNaviDaPosizionare();
        this.naveCorrente = naviDaPosizionare[0];
    }

    public Scene creaScena(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 20px;");

        Label titoloLabel = new Label("POSIZIONA LA TUA FLOTTA");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        naveCorrenteLabel = new Label("Posiziona: " + naveCorrente.getNomeConLunghezza());
        naveCorrenteLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 20px; -fx-font-weight: bold;");

        descrizioneLabel = new Label(naveCorrente.getDescrizione());
        descrizioneLabel.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px; -fx-font-style: italic;");

        orientamentoLabel = new Label("Orientamento: " + (orientamentoOrizzontale ? "Orizzontale " : "Verticale"));
        orientamentoLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox istruzioniBox = new VBox(5);
        istruzioniBox.setAlignment(Pos.CENTER);
        
        Label istruzioni1 = new Label("Click: posiziona nave");
        istruzioni1.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
        
        Label istruzioni2 = new Label("Tasto R: ruota orientamento");
        istruzioni2.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
                
        istruzioniBox.getChildren().addAll(istruzioni1, istruzioni2);

        suggerimentoLabel = new Label("Posiziona le navi mantenendo spazio tra loro");
        suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");

        naviRimanentiLabel = new Label();
        aggiornaStatoNavi();

        statoLabel = new Label("Clicca sulla griglia per posizionare la nave");
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        VBox grigliaContainer = creaGrigliaConCoordinate();

        listaNaviBox = creaListaNavi();

        HBox mainLayout = new HBox(30);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(grigliaContainer, listaNaviBox);

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
        
        // Gestione tasti
        scena.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.R) {
                ruotaOrientamento();
            }
        });
        
        scena.getRoot().setFocusTraversable(true);
        scena.getRoot().requestFocus();
        
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            Platform.exit();
            System.exit(0);
        });
        
        return scena;
    }

    private VBox creaGrigliaConCoordinate() {
        VBox container = new VBox(0);
        container.setAlignment(Pos.CENTER);
        
        HBox headerRow = new HBox(2);
        headerRow.setAlignment(Pos.CENTER);
        
        Label corner = new Label();
        corner.setPrefSize(25, 25);
        corner.setMaxSize(25, 25);
        corner.setMinSize(25, 25);
        headerRow.getChildren().add(corner);
        
        for (char c = 'A'; c <= 'J'; c++) {
            Label letter = new Label(String.valueOf(c));
            letter.setPrefSize(35, 25);
            letter.setMaxSize(35, 25);
            letter.setMinSize(35, 25);
            letter.setAlignment(Pos.CENTER);
            letter.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 14px;");
            headerRow.getChildren().add(letter);
        }
        
        //Corpo principale
        HBox mainRow = new HBox(0);
        mainRow.setAlignment(Pos.CENTER);
        
        VBox numbers = new VBox(2);
        numbers.setAlignment(Pos.CENTER);
        
        for (int i = 1; i <= 10; i++) {
            Label number = new Label(String.valueOf(i));
            number.setPrefSize(25, 35);
            number.setMaxSize(25, 35);
            number.setMinSize(25, 35);
            number.setAlignment(Pos.CENTER);
            number.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 14px;");
            numbers.getChildren().add(number);
        }
        
        GridPane grid = creaGrigliaPosizionamento();
        
        mainRow.getChildren().addAll(numbers, grid);
        container.getChildren().addAll(headerRow, mainRow);
        
        return container;
    }

    private GridPane creaGrigliaPosizionamento() {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        celle = new StackPane[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                final int riga = i;
                final int colonna = j;
                
                StackPane cella = new StackPane();
                cella.setPrefSize(35, 35);
                cella.setMinSize(35, 35);
                cella.setMaxSize(35, 35);
                
                Rectangle sfondo = new Rectangle(35, 35);
                sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
                sfondo.setStroke(Color.BLACK);
                sfondo.setStrokeWidth(1);
                
                cella.getChildren().add(sfondo);

                cella.setOnMouseClicked(event -> {
                    if (indiceNaveCorrente < naviDaPosizionare.length) {
                        posizionaNave(riga, colonna, orientamentoOrizzontale);
                    }
                });

                cella.setOnMouseEntered(e -> {
                    if (indiceNaveCorrente < naviDaPosizionare.length && 
                        !naviOccupate.contains(new Posizione(riga, colonna))) {
                        mostraPreviewNaveConOrientamento(riga, colonna, orientamentoOrizzontale);
                    }
                });

                cella.setOnMouseExited(e -> rimuoviPreview());

                celle[riga][colonna] = cella;
                grid.add(cella, colonna, riga);
            }
        }

        return grid;
    }

    private void ruotaOrientamento() {
        orientamentoOrizzontale = !orientamentoOrizzontale;
        orientamentoLabel.setText("Orientamento: " + (orientamentoOrizzontale ? "Orizzontale" : "Verticale"));
        
        suggerimentoLabel.setText("Orientamento ruotato! " + (orientamentoOrizzontale ? "Orizzontale" : "Verticale"));
        suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");
        
        audioManager.riproduciClickBottone();
        
        LogUtility.info("[POSIZIONAMENTO] Orientamento ruotato: " + (orientamentoOrizzontale ? "Orizzontale" : "Verticale"));
    }

    private VBox creaListaNavi() {
        VBox listaBox = new VBox(10);
        listaBox.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 15px; -fx-border-color: #444444; " +
                         "-fx-border-width: 1px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        listaBox.setPrefWidth(250);

        Label titoloLista = new Label("Navi da Posizionare");
        titoloLista.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        listaBox.getChildren().add(titoloLista);

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

        Button resetButton = new Button("Reset Tutto");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #FF5722; -fx-text-fill: white;");
        resetButton.setOnAction(e -> {
            audioManager.riproduciClickBottone();
            resetGriglia();
        });

        Button resetNaveButton = new Button("Reset Nave Corrente");
        resetNaveButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #FF9800; -fx-text-fill: white;");
        resetNaveButton.setOnAction(e -> {
            audioManager.riproduciClickBottone();
            resetNaveCorrente();
        });

        autoPosizionaButton = new Button("Auto-Posiziona");
        autoPosizionaButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        autoPosizionaButton.setOnAction(e -> {
            audioManager.riproduciClickBottone();
            autoPosizionaNavi();
        });

        confermaButton = new Button("Conferma Flotta");
        confermaButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        confermaButton.setDisable(true);
        confermaButton.setOnAction(e -> {
            audioManager.riproduciNotifica();
            inviaNaviAlController();
            mostraAttesaAvversario();
        });

        buttonBox.getChildren().addAll(resetButton, resetNaveButton, autoPosizionaButton, confermaButton);
        return buttonBox;
    }

    // Preview con freccia direzionale
    private void mostraPreviewNaveConOrientamento(int riga, int colonna, boolean orizzontale) {
        rimuoviPreview();
        
        int lunghezza = naveCorrente.getLunghezza();
        boolean posizionamentoValido = true;
        
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
        
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            if (nuovaRiga < 10 && nuovaColonna < 10) {
                StackPane cella = celle[nuovaRiga][nuovaColonna];
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                
                if (posizionamentoValido) {
                    sfondo.setFill(Color.LIGHTYELLOW);
                    
                    if (i == 0) {
                        Polygon freccia = creaFrecciaOrientamento(orizzontale);
                        cella.getChildren().add(freccia);
                    }
                } else {
                    sfondo.setFill(Color.LIGHTCORAL);
                }
            }
        }
        
        if (!posizionamentoValido) {
            if (!rispettaLimiti(riga, colonna, orizzontale, lunghezza)) {
                suggerimentoLabel.setText("Nave fuori dai limiti della griglia!");
            } else {
                suggerimentoLabel.setText("Posizione troppo vicina ad un'altra nave!");
            }
            suggerimentoLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px; -fx-font-style: italic;");
        } else {
            suggerimentoLabel.setText("Posizione valida - Clicca per posizionare");
            suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");
        }
    }

    private Polygon creaFrecciaOrientamento(boolean orizzontale) {
        Polygon freccia = new Polygon();
        
        double size = 12;
        
        if (orizzontale) {
            freccia.getPoints().addAll(new Double[]{
                2.0, size/2,
                size*0.8, size/2,
                size*0.6, size*0.3,
                size*0.6, size*0.7
            });
        } else {
            freccia.getPoints().addAll(new Double[]{
                size/2, 2.0,
                size/2, size*0.8,
                size*0.3, size*0.6,
                size*0.7, size*0.6
            });
        }
        
        freccia.setFill(Color.DARKBLUE);
        freccia.setStroke(Color.WHITE);
        freccia.setStrokeWidth(1);
        
        return freccia;
    }

    private boolean rispettaSpaziatura(Posizione pos) {
        for (int deltaRiga = -1; deltaRiga <= 1; deltaRiga++) {
            for (int deltaColonna = -1; deltaColonna <= 1; deltaColonna++) {
                if (deltaRiga == 0 && deltaColonna == 0) continue;
                
                int nuovaRiga = pos.getRiga() + deltaRiga;
                int nuovaColonna = pos.getColonna() + deltaColonna;
                
                if (nuovaRiga >= 0 && nuovaRiga < 10 && nuovaColonna >= 0 && nuovaColonna < 10) {
                    Posizione adiacente = new Posizione(nuovaRiga, nuovaColonna);
                    if (naviOccupate.contains(adiacente)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

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

    private void autoPosizionaNavi() {
        resetGriglia();
        
        Random random = new Random();
        
        for (int naveIndex = 0; naveIndex < naviDaPosizionare.length; naveIndex++) {
            TipoNave tipo = naviDaPosizionare[naveIndex];
            boolean posizionata = false;
            int tentativi = 0;
            
            while (!posizionata && tentativi < 100) {
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
        
        indiceNaveCorrente = naviDaPosizionare.length;
        aggiornaStato();
        aggiornaListaNavi();
        aggiornaDescrizioneNaveCorrente();
        
        audioManager.riproduciNotifica();
        
        suggerimentoLabel.setText("Flotta posizionata automaticamente!");
        suggerimentoLabel.setStyle("-fx-text-fill: #9C27B0; -fx-font-size: 12px; -fx-font-style: italic;");
    }

    private boolean posizionamentoValido(int riga, int colonna, boolean orizzontale, int lunghezza) {
        if (!rispettaLimiti(riga, colonna, orizzontale, lunghezza)) {
            return false;
        }
        
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
        
        suggerimentoLabel.setText("Posiziona le navi mantenendo spazio tra loro");
        suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");
    }

    private void posizionaNave(int riga, int colonna, boolean orizzontale) {
        int lunghezza = naveCorrente.getLunghezza();
        
        if (!posizionamentoValido(riga, colonna, orizzontale, lunghezza)) {
            if (!rispettaLimiti(riga, colonna, orizzontale, lunghezza)) {
                mostraErrore(naveCorrente.getNome() + " fuori dai limiti della griglia!");
            } else {
                mostraErrore("Posizione troppo vicina ad un'altra nave! Mantieni almeno uno spazio di distanza.");
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
        
        audioManager.riproduciPosizionamentoNave();
        
        indiceNaveCorrente++;
        if (indiceNaveCorrente < naviDaPosizionare.length) {
            naveCorrente = naviDaPosizionare[indiceNaveCorrente];
            suggerimentoLabel.setText("✅ Nave posizionata! Prossima: " + naveCorrente.getNome());
            LogUtility.info("[POSIZIONAMENTO] Prossima nave: " + naveCorrente.getNome());
        } else {
            suggerimentoLabel.setText("Tutte le navi posizionate! Clicca 'Conferma Flotta'");
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
            
            StackPane.setAlignment(naveGraph, Pos.CENTER);
            
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
            descrizioneLabel.setText("Tutte le navi sono state posizionate correttamente!");
            naveCorrenteLabel.setText("Flotta Completata!");
            statoLabel.setText("Tutte le navi posizionate! Clicca 'Conferma Flotta' per continuare.");
        }
    }

    private void aggiornaListaNavi() {
        listaNaviBox.getChildren().clear();
        
        Label titoloLista = new Label("Navi da Posizionare");
        titoloLista.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        listaNaviBox.getChildren().add(titoloLista);

        for (int i = 0; i < naviDaPosizionare.length; i++) {
            TipoNave tipo = naviDaPosizionare[i];
            Label naveLabel = new Label((i + 1) + ". " + tipo.getNomeConLunghezza());
            
            if (i < indiceNaveCorrente) {
                naveLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-strikethrough: true;");
                naveLabel.setText((i + 1) + ". " + tipo.getNomeConLunghezza());
            } else if (i == indiceNaveCorrente) {
                naveLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold; " +
                                  "-fx-background-color: rgba(255, 215, 0, 0.2); -fx-padding: 5px; " +
                                  "-fx-background-radius: 3px;");
                naveLabel.setText((i + 1) + ". " + tipo.getNomeConLunghezza());
            } else {
                naveLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 14px;");
                naveLabel.setText((i + 1) + ". " + tipo.getNomeConLunghezza());
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
        orientamentoOrizzontale = true;
        orientamentoLabel.setText("Orientamento: Orizzontale ↔️");
        suggerimentoLabel.setText("Posiziona le navi mantenendo spazio tra loro");
        suggerimentoLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-size: 12px; -fx-font-style: italic;");
        
        audioManager.riproduciClickBottone();
        
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
            suggerimentoLabel.setText("Nave rimossa! Torno a: " + naveCorrente.getNome());
            suggerimentoLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px; -fx-font-style: italic;");
            
            // Audio per reset nave
            audioManager.riproduciClickBottone();
            
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

        Label titoloLabel = new Label("Flotta Posizionata!");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label attesaLabel = new Label("Attendere che l'avversario posizioni la sua flotta...");
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