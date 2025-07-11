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
import shared.model.TipoNave;
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
    private Label naviRimanentiLabel;
    private Button confermaButton;
    private List<List<Posizione>> naviPosizionate = new ArrayList<>();
    private List<Posizione> naviOccupate = new ArrayList<>();
    
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
                        
                        // Mostra preview della nave
                        mostraPreviewNave(riga, colonna, true); // Default orizzontale
                    }
                });

                cella.setOnMouseExited(e -> {
                    rimuoviPreview();
                });

                celle[i][j] = cella;
                grid.add(cella, j, i);
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
                Posizione pos = new Posizione(nuovaRiga, nuovaColonna);
                if (!naviOccupate.contains(pos)) {
                    celle[nuovaRiga][nuovaColonna].setFill(Color.LIGHTYELLOW);
                } else {
                    celle[nuovaRiga][nuovaColonna].setFill(Color.LIGHTCORAL); // Conflitto
                }
            }
        }
    }

    private void rimuoviPreview() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Posizione pos = new Posizione(i, j);
                if (!naviOccupate.contains(pos)) {
                    celle[i][j].setFill(Color.LIGHTBLUE);
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
        
        // Colora le celle della nave con colori diversi per tipo
        Color coloreNave = getColoreNave(naveCorrente);
        for (Posizione pos : posizioniNave) {
            Rectangle cella = celle[pos.getRiga()][pos.getColonna()];
            cella.setFill(coloreNave);
            System.out.println("[DEBUG] Colorando cella (" + pos.getRiga() + "," + pos.getColonna() + ") con colore: " + coloreNave);
        }
        
        // Passa alla nave successiva
        indiceNaveCorrente++;
        if (indiceNaveCorrente < naviDaPosizionare.length) {
            naveCorrente = naviDaPosizionare[indiceNaveCorrente];
            System.out.println("[DEBUG] Prossima nave: " + naveCorrente.getNome());
        } else {
            System.out.println("[DEBUG] Tutte le navi posizionate! Attivando bottone conferma.");
        }
        
        // Aggiorna lo stato
        aggiornaStato();
        
        // Non ricaricare la scena per non perdere i colori delle celle
        aggiornaListaNavi();
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
        
        System.out.println("[DEBUG] Lista navi aggiornata - Indice corrente: " + indiceNaveCorrente);
    }

    private Color getColoreNave(TipoNave tipo) {
        return switch (tipo) {
            case PORTAEREI -> Color.DARKRED;
            case INCROCIATORE -> Color.DARKBLUE; 
            case CACCIATORPEDINIERE -> Color.DARKGREEN;
            case SOTTOMARINO -> Color.DARKORANGE;
        };
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
            System.out.println("[DEBUG] Ancora navi da posizionare: " + (naviDaPosizionare.length - indiceNaveCorrente));
        } else {
            statoLabel.setText("✅ Tutte le navi posizionate! Clicca 'Conferma Flotta' per continuare.");
            statoLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px; -fx-font-weight: bold;");
            confermaButton.setDisable(false);
            System.out.println("[DEBUG] TUTTE LE NAVI POSIZIONATE! Bottone conferma abilitato.");
        }
    }

    private void resetGriglia() {
        // Reset completo
        naviPosizionate.clear();
        naviOccupate.clear();
        indiceNaveCorrente = 0;
        naveCorrente = naviDaPosizionare[0];
        
        // Reset delle celle
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                celle[i][j].setFill(Color.LIGHTBLUE);
            }
        }
        
        // Reset dei controlli
        confermaButton.setDisable(true);
        aggiornaStato();
        System.out.println("[DEBUG] Reset completo effettuato");
    }

    private void resetNaveCorrente() {
        if (indiceNaveCorrente > 0) {
            // Rimuovi l'ultima nave posizionata
            indiceNaveCorrente--;
            List<Posizione> ultimaNave = naviPosizionate.remove(naviPosizionate.size() - 1);
            naviOccupate.removeAll(ultimaNave);
            
            // Ricolora le celle
            for (Posizione pos : ultimaNave) {
                celle[pos.getRiga()][pos.getColonna()].setFill(Color.LIGHTBLUE);
            }
            
            naveCorrente = naviDaPosizionare[indiceNaveCorrente];
            confermaButton.setDisable(true);
            aggiornaStato();
            // Non ricaricare la scena
            System.out.println("[DEBUG] Reset nave corrente. Torno a: " + naveCorrente.getNome());
        }
    }

    private void ricaricaScena() {
        // RIMOSSO - Non ricarichiamo più la scena per evitare di perdere i colori
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

    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Errore Posizionamento");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}