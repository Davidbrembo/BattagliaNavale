package client.view;

import client.controller.GiocoController;
import client.view.components.NaveGraphics;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import shared.model.Posizione;
import shared.model.TipoNave;
import utility.LogUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

/**
 * View per il posizionamento delle navi con descrizioni aggiornate
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

        // Istruzioni
        VBox istruzioniBox = new VBox(5);
        istruzioniBox.setAlignment(Pos.CENTER);
        
        Label istruzioni1 = new Label("🖱️ Click sinistro: nave orizzontale");
        istruzioni1.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
        
        Label istruzioni2 = new Label("🖱️ Click destro: nave verticale");
        istruzioni2.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
        
        istruzioniBox.getChildren().addAll(istruzioni1, istruzioni2);

        // Stato del posizionamento
        naviRimanentiLabel = new Label();
        aggiornaStatoNavi();

        // Label per lo stato generale
        statoLabel = new Label("Clicca sulla griglia per posizionare la nave");
        statoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        // Crea la griglia
        GridPane grid = creaGrigliaPosizionamento();

        // Lista navi da posizionare (sidebar)
        listaNaviBox = creaListaNavi();

        // Layout principale con griglia e lista navi
        HBox mainLayout = new HBox(30);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(grid, listaNaviBox);

        // Pulsanti
        HBox buttonBox = creaBottoni();

        root.getChildren().addAll(
            titoloLabel, 
            naveCorrenteLabel, 
            descrizioneLabel,
            istruzioniBox, 
            naviRimanentiLabel,
            statoLabel, 
            mainLayout, 
            buttonBox
        );

        Scene scena = new Scene(root, 1100, 900);
        
        // Gestione chiusura finestra
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            Platform.exit();
            System.exit(0);
        });
        
        return scena;
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
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button resetButton = new Button("🔄 Reset Tutto");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #FF5722; -fx-text-fill: white;");
        resetButton.setOnAction(e -> resetGriglia());

        Button resetNaveButton = new Button("↶ Reset Nave Corrente");
        resetNaveButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #FF9800; -fx-text-fill: white;");
        resetNaveButton.setOnAction(e -> resetNaveCorrente());

        confermaButton = new Button("✅ Conferma Flotta");
        confermaButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        confermaButton.setDisable(true);
        confermaButton.setOnAction(e -> {
            inviaNaviAlController();
            mostraAttesaAvversario();
        });

        buttonBox.getChildren().addAll(resetButton, resetNaveButton, confermaButton);
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

                cella.setOnMouseClicked(event -> {
                    if (indiceNaveCorrente < naviDaPosizionare.length) {
                        if (event.getButton() == MouseButton.PRIMARY) {
                            posizionaNave(riga, colonna, true);
                        } else if (event.getButton() == MouseButton.SECONDARY) {
                            posizionaNave(riga, colonna, false);
                        }
                    }
                });

                cella.setOnMouseEntered(e -> {
                    if (indiceNaveCorrente < naviDaPosizionare.length && 
                        !naviOccupate.contains(new Posizione(riga, colonna))) {
                        mostraPreviewNave(riga, colonna, true);
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

    private void mostraPreviewNave(int riga, int colonna, boolean orizzontale) {
        rimuoviPreview();
        
        int lunghezza = naveCorrente.getLunghezza();
        
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            if (nuovaRiga < 10 && nuovaColonna < 10) {
                StackPane cella = celle[nuovaRiga][nuovaColonna];
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                
                Posizione pos = new Posizione(nuovaRiga, nuovaColonna);
                if (!naviOccupate.contains(pos)) {
                    sfondo.setFill(Color.LIGHTYELLOW);
                } else {
                    sfondo.setFill(Color.LIGHTCORAL);
                }
            }
        }
    }

    private void rimuoviPreview() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Posizione pos = new Posizione(i, j);
                if (!naviOccupate.contains(pos)) {
                    StackPane cella = celle[i][j];
                    Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                    sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
                }
            }
        }
    }

    private void posizionaNave(int riga, int colonna, boolean orizzontale) {
        int lunghezza = naveCorrente.getLunghezza();
        List<Posizione> posizioniNave = new ArrayList<>();
        
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            if (nuovaRiga >= 10 || nuovaColonna >= 10) {
                mostraErrore("⚠️ " + naveCorrente.getNome() + " fuori dai limiti della griglia!");
                return;
            }
            
            Posizione pos = new Posizione(nuovaRiga, nuovaColonna);
            
            if (naviOccupate.contains(pos)) {
                mostraErrore("⚠️ Posizione già occupata da un'altra nave!");
                return;
            }
            
            posizioniNave.add(pos);
        }
        
        naviPosizionate.add(posizioniNave);
        naviOccupate.addAll(posizioniNave);
        
        visualizzaNaveGrafica(posizioniNave, naveCorrente, orizzontale);
        
        indiceNaveCorrente++;
        if (indiceNaveCorrente < naviDaPosizionare.length) {
            naveCorrente = naviDaPosizionare[indiceNaveCorrente];
            LogUtility.info("[POSIZIONAMENTO] Prossima nave: " + naveCorrente.getNome());
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