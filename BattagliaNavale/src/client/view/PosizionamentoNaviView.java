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
 * View per il posizionamento delle navi con grafica realistica.
 * Segue il pattern MVC delegando la logica al Controller.
 */
public class PosizionamentoNaviView {

    private StackPane[][] celle; // Cambiato da Rectangle a StackPane per contenere le navi
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
    private VBox listaNaviBox; // Riferimento al menu laterale per aggiornarlo
    private Label naveCorrenteLabel; // Riferimento alla label della nave corrente
    
    private GiocoController controller;
    private Stage primaryStage;

    public PosizionamentoNaviView() {
        this.controller = GiocoController.getInstance();
        this.naviDaPosizionare = TipoNave.getNaviDaPosizionare();
        this.naveCorrente = naviDaPosizionare[0]; // Inizia con la prima nave
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
        Label descrizioneLabel = new Label(naveCorrente.getDescrizione());
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
        scena.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
        
        // Gestione chiusura finestra
        primaryStage.setOnCloseRequest(event -> {
            LogUtility.info("[POSIZIONAMENTO] Richiesta chiusura finestra - disconnettendo dal server...");
            
            event.consume();
            
            Alert confermaChiusura = new Alert(Alert.AlertType.CONFIRMATION);
            confermaChiusura.setTitle("Conferma Uscita");
            confermaChiusura.setHeaderText("Sei sicuro di voler uscire?");
            confermaChiusura.setContentText("Se esci durante il posizionamento, l'altra persona dovrà aspettare un nuovo avversario.");
            
            ButtonType esciButton = new ButtonType("Esci");
            ButtonType annullaButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            confermaChiusura.getButtonTypes().setAll(esciButton, annullaButton);
            
            Optional<ButtonType> result = confermaChiusura.showAndWait();
            
            if (result.isPresent() && result.get() == esciButton) {
                LogUtility.info("[POSIZIONAMENTO] Uscita confermata - disconnessione in corso...");
                
                try {
                    if (controller.isConnesso()) {
                        controller.disconnetti();
                        LogUtility.info("[POSIZIONAMENTO] Disconnessione completata");
                    }
                } catch (Exception e) {
                    LogUtility.error("[POSIZIONAMENTO] Errore durante disconnessione: " + e.getMessage());
                }
                
                Platform.exit();
                System.exit(0);
            } else {
                LogUtility.info("[POSIZIONAMENTO] Chiusura annullata dall'utente");
            }
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
                // Nave già posizionata
                naveLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-strikethrough: true;");
            } else if (i == indiceNaveCorrente) {
                // Nave corrente
                naveLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else {
                // Nave da posizionare
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
                // Variabili final per uso nei lambda
                final int riga = i;
                final int colonna = j;
                
                StackPane cella = new StackPane();
                cella.setPrefSize(cellSize, cellSize);
                cella.setMaxSize(cellSize, cellSize);
                cella.setMinSize(cellSize, cellSize);
                
                // Sfondo della cella (acqua)
                Rectangle sfondo = new Rectangle(cellSize, cellSize);
                sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
                sfondo.setStroke(Color.BLACK);
                sfondo.setStrokeWidth(1);
                
                cella.getChildren().add(sfondo);

                // Gestione click del mouse
                cella.setOnMouseClicked(event -> {
                    if (indiceNaveCorrente < naviDaPosizionare.length) {
                        if (event.getButton() == MouseButton.PRIMARY) {
                            // Click sinistro: nave orizzontale
                            posizionaNave(riga, colonna, true);
                        } else if (event.getButton() == MouseButton.SECONDARY) {
                            // Click destro: nave verticale
                            posizionaNave(riga, colonna, false);
                        }
                    }
                });

                // Effetto hover con preview della nave
                cella.setOnMouseEntered(e -> {
                    if (indiceNaveCorrente < naviDaPosizionare.length && 
                        !naviOccupate.contains(new Posizione(riga, colonna))) {
                        
                        // Mostra preview della nave (default orizzontale)
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
        rimuoviPreview(); // Rimuovi preview precedente
        
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
                    sfondo.setFill(Color.LIGHTCORAL); // Conflitto
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
        
        // Calcola le posizioni della nave
        for (int i = 0; i < lunghezza; i++) {
            int nuovaRiga = orizzontale ? riga : riga + i;
            int nuovaColonna = orizzontale ? colonna + i : colonna;
            
            // Controlla se la posizione è valida
            if (nuovaRiga >= 10 || nuovaColonna >= 10) {
                mostraErrore("⚠️ " + naveCorrente.getNome() + " fuori dai limiti della griglia!");
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
        
        // Visualizza la nave grafica invece dei rettangoli colorati
        visualizzaNaveGrafica(posizioniNave, naveCorrente, orizzontale);
        
        // Passa alla nave successiva
        indiceNaveCorrente++;
        if (indiceNaveCorrente < naviDaPosizionare.length) {
            naveCorrente = naviDaPosizionare[indiceNaveCorrente];
            LogUtility.info("[POSIZIONAMENTO] Prossima nave: " + naveCorrente.getNome());
        } else {
            LogUtility.info("[POSIZIONAMENTO] Tutte le navi posizionate! Attivando bottone conferma.");
        }
        
        // Aggiorna lo stato
        aggiornaStato();
        
        // Aggiorna la lista navi
        aggiornaListaNavi();
    }

    private void visualizzaNaveGrafica(List<Posizione> posizioni, TipoNave tipo, boolean orizzontale) {
        // Per ogni posizione della nave, aggiungi la grafica
        for (int i = 0; i < posizioni.size(); i++) {
            Posizione pos = posizioni.get(i);
            StackPane cella = celle[pos.getRiga()][pos.getColonna()];
            
            // Crea la nave grafica
            NaveGraphics naveGraph = new NaveGraphics(tipo, orizzontale, 35);
            
            // Aggiungi identificatore per il segmento
            if (posizioni.size() > 1) {
                if (i == 0) {
                    naveGraph.setId("prua");
                } else if (i == posizioni.size() - 1) {
                    naveGraph.setId("poppa");
                } else {
                    naveGraph.setId("centro");
                }
            }
            
            // Aggiungi la nave alla cella (sopra lo sfondo)
            cella.getChildren().add(naveGraph);
            
            // Salva il riferimento per eventuali reset
            naviGrafiche.put(pos, naveGraph);
            
            LogUtility.info("[POSIZIONAMENTO] Visualizzata nave grafica " + tipo.getNome() + 
                           " in (" + pos.getRiga() + "," + pos.getColonna() + ")");
        }
    }

    private void aggiornaListaNavi() {
        // Aggiorna il titolo della nave corrente
        if (indiceNaveCorrente < naviDaPosizionare.length) {
            naveCorrenteLabel.setText("Posiziona: " + naveCorrente.getNomeConLunghezza());
        } else {
            naveCorrenteLabel.setText("✅ Flotta Completata!");
            naveCorrenteLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 20px; -fx-font-weight: bold;");
        }
        
        // Rimuovi tutti i figli tranne il titolo
        listaNaviBox.getChildren().clear();
        
        Label titoloLista = new Label("📋 Navi da Posizionare");
        titoloLista.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        listaNaviBox.getChildren().add(titoloLista);

        // Ricrea la lista aggiornata
        for (int i = 0; i < naviDaPosizionare.length; i++) {
            TipoNave tipo = naviDaPosizionare[i];
            Label naveLabel = new Label((i + 1) + ". " + tipo.getNomeConLunghezza());
            
            if (i < indiceNaveCorrente) {
                // Nave già posizionata
                naveLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-strikethrough: true;");
                naveLabel.setText("✅ " + (i + 1) + ". " + tipo.getNomeConLunghezza());
            } else if (i == indiceNaveCorrente) {
                // Nave corrente
                naveLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px; -fx-font-weight: bold;");
                naveLabel.setText("👉 " + (i + 1) + ". " + tipo.getNomeConLunghezza());
            } else {
                // Nave da posizionare
                naveLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 14px;");
                naveLabel.setText("⏳ " + (i + 1) + ". " + tipo.getNomeConLunghezza());
            }
            
            listaNaviBox.getChildren().add(naveLabel);
        }
        
        LogUtility.info("[POSIZIONAMENTO] Lista navi aggiornata - Indice corrente: " + indiceNaveCorrente);
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
            LogUtility.info("[POSIZIONAMENTO] Ancora navi da posizionare: " + (naviDaPosizionare.length - indiceNaveCorrente));
        } else {
            statoLabel.setText("✅ Tutte le navi posizionate! Clicca 'Conferma Flotta' per continuare.");
            statoLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px; -fx-font-weight: bold;");
            confermaButton.setDisable(false);
            LogUtility.info("[POSIZIONAMENTO] TUTTE LE NAVI POSIZIONATE! Bottone conferma abilitato.");
        }
    }

    private void resetGriglia() {
        // Reset completo
        naviPosizionate.clear();
        naviOccupate.clear();
        naviGrafiche.clear();
        indiceNaveCorrente = 0;
        naveCorrente = naviDaPosizionare[0];
        
        // Reset delle celle - rimuovi tutte le navi grafiche
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                StackPane cella = celle[i][j];
                // Mantieni solo lo sfondo (primo child)
                if (cella.getChildren().size() > 1) {
                    cella.getChildren().subList(1, cella.getChildren().size()).clear();
                }
                // Ripristina colore sfondo
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
            }
        }
        
        // Reset dei controlli
        confermaButton.setDisable(true);
        aggiornaStato();
        aggiornaListaNavi();
        LogUtility.info("[POSIZIONAMENTO] Reset completo effettuato");
    }

    private void resetNaveCorrente() {
        if (indiceNaveCorrente > 0) {
            // Rimuovi l'ultima nave posizionata
            indiceNaveCorrente--;
            List<Posizione> ultimaNave = naviPosizionate.remove(naviPosizionate.size() - 1);
            naviOccupate.removeAll(ultimaNave);
            
            // Rimuovi le navi grafiche
            for (Posizione pos : ultimaNave) {
                StackPane cella = celle[pos.getRiga()][pos.getColonna()];
                // Rimuovi la nave grafica (mantieni solo lo sfondo)
                if (cella.getChildren().size() > 1) {
                    cella.getChildren().subList(1, cella.getChildren().size()).clear();
                }
                // Ripristina colore sfondo
                Rectangle sfondo = (Rectangle) cella.getChildren().get(0);
                sfondo.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8));
                
                // Rimuovi dal tracking delle navi grafiche
                naviGrafiche.remove(pos);
            }
            
            naveCorrente = naviDaPosizionare[indiceNaveCorrente];
            confermaButton.setDisable(true);
            aggiornaStato();
            aggiornaListaNavi();
            LogUtility.info("[POSIZIONAMENTO] Reset nave corrente. Torno a: " + naveCorrente.getNome());
        }
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

        Label titoloLabel = new Label("⚓ Flotta Posizionata!");
        titoloLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        // Riassunto flotta
        VBox riassuntoBox = new VBox(5);
        riassuntoBox.setAlignment(Pos.CENTER);
        
        Label riassuntoTitolo = new Label("📋 La tua flotta:");
        riassuntoTitolo.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 18px; -fx-font-weight: bold;");
        riassuntoBox.getChildren().add(riassuntoTitolo);
        
        for (TipoNave tipo : TipoNave.getNaviDaPosizionare()) {
            Label naveLabel = new Label("✅ " + tipo.getNomeConLunghezza());
            naveLabel.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");
            riassuntoBox.getChildren().add(naveLabel);
        }

        Label attesaLabel = new Label("⏳ Attendere che l'avversario posizioni la sua flotta...");
        attesaLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 18px; -fx-font-style: italic;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);
        progressIndicator.setStyle("-fx-progress-color: #4CAF50;");

        Label statusLabel = new Label("Sincronizzazione in corso...");
        statusLabel.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 14px;");

        root.getChildren().addAll(titoloLabel, riassuntoBox, attesaLabel, progressIndicator, statusLabel);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/warstyle.css").toExternalForm());
        
        // Gestione chiusura anche nella schermata di attesa
        primaryStage.setOnCloseRequest(event -> {
            LogUtility.info("[POSIZIONAMENTO_ATTESA] Richiesta chiusura finestra - disconnettendo dal server...");
            
            event.consume();
            
            Alert confermaChiusura = new Alert(Alert.AlertType.CONFIRMATION);
            confermaChiusura.setTitle("Conferma Uscita");
            confermaChiusura.setHeaderText("Sei sicuro di voler uscire?");
            confermaChiusura.setContentText("Se esci ora, l'avversario vincerà automaticamente.");
            
            ButtonType esciButton = new ButtonType("Esci");
            ButtonType annullaButton = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            confermaChiusura.getButtonTypes().setAll(esciButton, annullaButton);
            
            Optional<ButtonType> result = confermaChiusura.showAndWait();
            
            if (result.isPresent() && result.get() == esciButton) {
                try {
                    if (controller.isConnesso()) {
                        controller.disconnetti();
                    }
                } catch (Exception e) {
                    LogUtility.error("[POSIZIONAMENTO_ATTESA] Errore durante disconnessione: " + e.getMessage());
                }
                Platform.exit();
                System.exit(0);
            }
        });
        
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
        GrigliaView grigliaView = new GrigliaView();
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

    /**
     * Mostra un errore in un popup
     */
    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Errore Posizionamento");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}