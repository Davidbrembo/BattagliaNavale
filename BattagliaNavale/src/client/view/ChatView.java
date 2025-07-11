package client.view;

import client.controller.GiocoController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.LogUtility;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatView {
    
    private VBox chatContainer;
    private ScrollPane chatScrollPane;
    private VBox chatMessages;
    private TextField chatInput;
    private Button sendButton;
    private GiocoController giocoController;
    private String nomeGiocatore;
    
    public ChatView() {
        this.giocoController = GiocoController.getInstance();
        this.nomeGiocatore = giocoController.getNomeGiocatore();
        inizializzaChat();
    }
    
    private void inizializzaChat() {
        chatContainer = new VBox(10);
        chatContainer.setPrefWidth(300);
        chatContainer.setMaxWidth(300);
        chatContainer.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444444; " +
                              "-fx-border-width: 1px; -fx-padding: 10px; -fx-background-radius: 5px; " +
                              "-fx-border-radius: 5px;");
        
        // Titolo della chat
        Label chatTitle = new Label("💬 Chat");
        chatTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Area messaggi
        chatMessages = new VBox(5);
        chatMessages.setPadding(new Insets(5));
        
        chatScrollPane = new ScrollPane(chatMessages);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setPrefHeight(200);
        chatScrollPane.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Input della chat
        HBox chatInputContainer = new HBox(5);
        chatInputContainer.setAlignment(Pos.CENTER);
        
        chatInput = new TextField();
        chatInput.setPromptText("Scrivi un messaggio...");
        chatInput.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; " +
                          "-fx-border-color: #555555; -fx-border-radius: 3px; " +
                          "-fx-background-radius: 3px;");
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        
        sendButton = new Button("📤");
        sendButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                           "-fx-border-radius: 3px; -fx-background-radius: 3px; " +
                           "-fx-font-size: 12px;");
        sendButton.setPrefWidth(40);
        
        // Eventi
        sendButton.setOnAction(e -> inviaMessaggio());
        chatInput.setOnAction(e -> inviaMessaggio());
        
        chatInputContainer.getChildren().addAll(chatInput, sendButton);
        
        chatContainer.getChildren().addAll(chatTitle, chatScrollPane, chatInputContainer);
        
        // Messaggio di benvenuto
        aggiungiMessaggioSistema("Chat attivata! Puoi comunicare con l'avversario.");
    }
    
    public VBox getChatContainer() {
        return chatContainer;
    }
    
    private void inviaMessaggio() {
        String testo = chatInput.getText().trim();
        if (testo.isEmpty()) {
            return;
        }
        
        // Crea il messaggio di chat
        MessaggioChat messaggioChat = new MessaggioChat(nomeGiocatore, testo);
        Messaggio messaggio = new Messaggio(Comando.MESSAGGIO_CHAT, messaggioChat);
        
        // Invia al server
        giocoController.inviaMessaggio(messaggio);
        
        // Mostra il messaggio localmente (come "inviato")
        Platform.runLater(() -> {
            aggiungiMessaggioPropio(testo);
            chatInput.clear();
        });
        
        LogUtility.info("[CHAT] Messaggio inviato: " + testo);
    }
    
    public void riceviMessaggio(MessaggioChat messaggio) {
        Platform.runLater(() -> {
            aggiungiMessaggioRicevuto(messaggio.getMittente(), messaggio.getTesto());
        });
    }
    
    private void aggiungiMessaggioPropio(String testo) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        Label messaggioLabel = new Label(testo);
        messaggioLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                               "-fx-padding: 8px 12px; -fx-background-radius: 15px; " +
                               "-fx-max-width: 200px; -fx-wrap-text: true;");
        messaggioLabel.setWrapText(true);
        
        Label timeLabel = new Label(timestamp);
        timeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
        
        VBox messaggioContainer = new VBox(2);
        messaggioContainer.setAlignment(Pos.CENTER_RIGHT);
        messaggioContainer.getChildren().addAll(messaggioLabel, timeLabel);
        messaggioContainer.setMaxWidth(Double.MAX_VALUE);
        
        chatMessages.getChildren().add(messaggioContainer);
        scrollToBottom();
    }
    
    private void aggiungiMessaggioRicevuto(String mittente, String testo) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        Label mittenteLabel = new Label(mittente);
        mittenteLabel.setStyle("-fx-text-fill: #87CEEB; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        Label messaggioLabel = new Label(testo);
        messaggioLabel.setStyle("-fx-background-color: #444444; -fx-text-fill: white; " +
                               "-fx-padding: 8px 12px; -fx-background-radius: 15px; " +
                               "-fx-max-width: 200px; -fx-wrap-text: true;");
        messaggioLabel.setWrapText(true);
        
        Label timeLabel = new Label(timestamp);
        timeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
        
        VBox messaggioContainer = new VBox(2);
        messaggioContainer.setAlignment(Pos.CENTER_LEFT);
        messaggioContainer.getChildren().addAll(mittenteLabel, messaggioLabel, timeLabel);
        messaggioContainer.setMaxWidth(Double.MAX_VALUE);
        
        chatMessages.getChildren().add(messaggioContainer);
        scrollToBottom();
    }
    
    private void aggiungiMessaggioSistema(String testo) {
        Label messaggioLabel = new Label("🔔 " + testo);
        messaggioLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 11px; " +
                               "-fx-padding: 5px; -fx-font-style: italic; " +
                               "-fx-wrap-text: true; -fx-max-width: 280px;");
        messaggioLabel.setWrapText(true);
        
        VBox messaggioContainer = new VBox();
        messaggioContainer.setAlignment(Pos.CENTER);
        messaggioContainer.getChildren().add(messaggioLabel);
        messaggioContainer.setMaxWidth(Double.MAX_VALUE);
        
        chatMessages.getChildren().add(messaggioContainer);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }
    
    public void mostraNotificaGiocatoreConnesso(String nomeGiocatore) {
        aggiungiMessaggioSistema(nomeGiocatore + " si è unito alla partita!");
    }
    
    public void mostraNotificaGiocatoreDisconnesso(String nomeGiocatore) {
        aggiungiMessaggioSistema(nomeGiocatore + " si è disconnesso.");
    }
    
    public void mostraNotificaTurno(boolean mioTurno) {
        if (mioTurno) {
            aggiungiMessaggioSistema("È il tuo turno!");
        } else {
            aggiungiMessaggioSistema("Turno dell'avversario.");
        }
    }
    
    // Classe interna per rappresentare un messaggio di chat
    public static class MessaggioChat implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String mittente;
        private String testo;
        private long timestamp;
        
        public MessaggioChat(String mittente, String testo) {
            this.mittente = mittente;
            this.testo = testo;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMittente() { return mittente; }
        public String getTesto() { return testo; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return "MessaggioChat{mittente='" + mittente + "', testo='" + testo + "'}";
        }
    }
}