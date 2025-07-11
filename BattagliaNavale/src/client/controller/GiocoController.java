package client.controller;

import client.network.ClientSocket;
import client.view.ChatView;
import client.view.GrigliaView;
import shared.model.Posizione;
import shared.model.RisultatoAttacco;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.LogUtility;
import javafx.application.Platform;

import java.io.IOException;
import java.util.List;

/**
 * Controller principale che gestisce la logica di business del client
 * e coordina la comunicazione tra Model (dati di gioco) e View (interfaccia)
 */
public class GiocoController {

    private static GiocoController instance;
    
    // Network
    private final ClientSocket clientSocket;
    private boolean connesso;
    
    // Model - Stato del gioco
    private String nomeGiocatore;
    private int myPlayerID = -1;
    private boolean mioTurno = false;
    private StatoPartita statoPartita = StatoPartita.LOBBY;
    
    // View - Riferimenti alle viste per aggiornamenti
    private GrigliaView grigliaView;
    private ChatView chatView;
    
    // Navigation callbacks - per gestire le transizioni tra schermate
    private Runnable onStartCallback;
    private Runnable onInizioBattagliaCallback;
    private boolean startRicevuto = false;
    private boolean battagliaIniziata = false;
    
    // Thread di ascolto messaggi
    private Thread ascoltoThread;

    private GiocoController() {
        clientSocket = ClientSocket.getInstance();
        connesso = inizializzaConnessione();
        if (connesso) {
            avviaAscoltoServer();
        }
    }

    public static GiocoController getInstance() {
        if (instance == null) {
            instance = new GiocoController();
        }
        return instance;
    }

    // ================== NETWORK MANAGEMENT ==================

    public boolean inizializzaConnessione() {
        try {
            clientSocket.connect("localhost", 12345);
            LogUtility.info("[CLIENT] Connessione avvenuta.");
            
            // Prova a ricevere il messaggio di benvenuto o di rifiuto
            Messaggio messaggioDiBenvenuto = clientSocket.riceviMessaggio();
            if (messaggioDiBenvenuto != null) {
                LogUtility.info("[SERVER] " + messaggioDiBenvenuto);
                
                // Se il server invia un errore immediatamente, significa che ha rifiutato la connessione
                if (messaggioDiBenvenuto.getComando() == Comando.ERRORE) {
                    LogUtility.warning("[CLIENT] Server ha rifiutato la connessione: " + 
                                     messaggioDiBenvenuto.getContenuto());
                    clientSocket.chiudiConnessione();
                    return false;
                }
            }
            
            return true;
        } catch (IOException e) {
            LogUtility.error("[CLIENT] Errore di connessione: " + e.getMessage());
            return false;
        }
    }

    private void avviaAscoltoServer() {
        ascoltoThread = new Thread(() -> {
            try {
                while (connesso) {
                    Messaggio msg = clientSocket.riceviMessaggio();
                    if (msg == null) {
                        LogUtility.warning("[CLIENT] Messaggio null ricevuto, possibile disconnessione");
                        connesso = false;
                        break;
                    }
                    
                    Platform.runLater(() -> gestisciMessaggioServer(msg));
                }
            } catch (Exception e) {
                LogUtility.error("[CLIENT] Errore nell'ascolto server: " + e.getMessage());
                connesso = false;
            }
        });
        ascoltoThread.setDaemon(true);
        ascoltoThread.start();
        LogUtility.info("[CLIENT] Thread di ascolto server avviato");
    }

    // ================== MESSAGE HANDLING ==================

    private void gestisciMessaggioServer(Messaggio messaggio) {
        LogUtility.info("[CLIENT] Ricevuto: " + messaggio.getComando());
        
        switch (messaggio.getComando()) {
            case ASSEGNA_ID -> gestisciAssegnazioneID((Integer) messaggio.getContenuto());
            case START -> gestisciInizioPartita();
            case INIZIO_BATTAGLIA -> gestisciInizioBattaglia();
            case TURNO -> gestisciTurno(true);
            case STATO -> gestisciStato((String) messaggio.getContenuto());
            case RISULTATO_ATTACCO -> gestisciRisultatoAttacco((RisultatoAttacco) messaggio.getContenuto());
            case ATTACCO_RICEVUTO -> gestisciAttaccoRicevuto((RisultatoAttacco) messaggio.getContenuto());
            case MESSAGGIO_CHAT -> gestisciMessaggioChat(messaggio.getContenuto());
            case VITTORIA -> gestisciVittoria((String) messaggio.getContenuto());
            case SCONFITTA -> gestisciSconfitta((String) messaggio.getContenuto());
            case ERRORE -> gestisciErrore((String) messaggio.getContenuto());
            default -> LogUtility.warning("[CLIENT] Comando non gestito: " + messaggio.getComando());
        }
    }

    // ================== GAME LOGIC HANDLERS ==================

    private void gestisciAssegnazioneID(int id) {
        this.myPlayerID = id;
        LogUtility.info("[CLIENT] Assegnato ID: " + id + " - Stato: " + statoPartita);
        if (grigliaView != null) {
            grigliaView.aggiornaStatoGioco("Sei il giocatore " + (id + 1));
        }
    }

    private void gestisciInizioPartita() {
        statoPartita = StatoPartita.POSIZIONAMENTO_NAVI;
        startRicevuto = true;
        LogUtility.info("[CLIENT] ⭐ RICEVUTO START! ID=" + myPlayerID + " - Transizione a posizionamento navi");
        
        // Callback per la transizione di schermata
        if (onStartCallback != null) {
            LogUtility.info("[CLIENT] Eseguendo callback START");
            onStartCallback.run();
        } else {
            LogUtility.warning("[CLIENT] ⚠️ Callback START è NULL!");
        }
    }

    private void gestisciInizioBattaglia() {
        statoPartita = StatoPartita.BATTAGLIA;
        LogUtility.info("[CLIENT] Inizio battaglia!");
        if (chatView != null) {
            chatView.mostraNotificaSistema("🌊 La battaglia è iniziata!");
        }
        
        // Callback per la transizione di schermata
        if (onInizioBattagliaCallback != null) {
            onInizioBattagliaCallback.run();
        }
    }

    private void gestisciTurno(boolean turno) {
        this.mioTurno = turno;
        if (grigliaView != null) {
            grigliaView.aggiornaStatoGioco("È il tuo turno! Clicca sulla griglia avversario per attaccare.");
            grigliaView.attivaGrigliaAvversario(true);
        }
        if (chatView != null) {
            chatView.mostraNotificaTurno(true);
        }
    }

    private void gestisciStato(String stato) {
        this.mioTurno = false;
        if (grigliaView != null) {
            grigliaView.aggiornaStatoGioco(stato);
            grigliaView.attivaGrigliaAvversario(false);
        }
        if (chatView != null) {
            chatView.mostraNotificaTurno(false);
        }
    }

    private void gestisciRisultatoAttacco(RisultatoAttacco risultato) {
        if (grigliaView != null) {
            grigliaView.aggiornaCellaAvversario(risultato);
        }
        LogUtility.info("[CLIENT] Risultato attacco: " + (risultato.isColpito() ? "COLPITO" : "MANCATO"));
    }

    private void gestisciAttaccoRicevuto(RisultatoAttacco risultato) {
        if (grigliaView != null) {
            grigliaView.aggiornaCellaPropria(risultato);
        }
        LogUtility.info("[CLIENT] Attacco ricevuto in: " + risultato.getPosizione());
    }

    private void gestisciMessaggioChat(Object messaggioData) {
        if (chatView != null && messaggioData instanceof ChatView.MessaggioChat) {
            chatView.riceviMessaggio((ChatView.MessaggioChat) messaggioData);
        }
    }

    private void gestisciVittoria(String messaggio) {
        statoPartita = StatoPartita.FINITA;
        if (grigliaView != null) {
            grigliaView.gestisciVittoria(messaggio);
        }
        if (chatView != null) {
            chatView.mostraNotificaSistema("🎉 " + messaggio);
        }
    }

    private void gestisciSconfitta(String messaggio) {
        statoPartita = StatoPartita.FINITA;
        if (grigliaView != null) {
            grigliaView.gestisciSconfitta(messaggio);
        }
        if (chatView != null) {
            chatView.mostraNotificaSistema("💀 " + messaggio);
        }
    }

    private void gestisciErrore(String errore) {
        LogUtility.error("[CLIENT] Errore dal server: " + errore);
        
        // Se l'errore indica che la partita è completa, gestiscilo diversamente
        if (errore.contains("Partita completa") || errore.contains("Massimo 2 giocatori")) {
            LogUtility.warning("[CLIENT] Server ha rifiutato la connessione: partita completa");
            connesso = false;
            
            // Notifica all'utente che deve riprovare più tardi
            Platform.runLater(() -> {
                if (grigliaView != null) {
                    grigliaView.mostraErrore("Server pieno! Partita già in corso con 2 giocatori.");
                }
            });
        } else {
            // Altri errori di gioco normali
            if (grigliaView != null) {
                grigliaView.mostraErrore(errore);
            }
        }
    }

    // ================== PUBLIC INTERFACE - ACTIONS ==================

    /**
     * Invia il nome del giocatore al server
     */
    public void impostaNomeGiocatore(String nome) {
        this.nomeGiocatore = nome;
        inviaMessaggio(new Messaggio(Comando.INVIA_NOME, nome));
        LogUtility.info("[CLIENT] Nome giocatore impostato: " + nome);
    }

    /**
     * Invia un attacco alla posizione specificata
     */
    public void attacca(Posizione posizione) {
        if (!mioTurno) {
            LogUtility.warning("[CLIENT] Tentativo di attacco fuori turno");
            return;
        }
        
        if (statoPartita != StatoPartita.BATTAGLIA) {
            LogUtility.warning("[CLIENT] Tentativo di attacco fuori dalla fase di battaglia");
            return;
        }
        
        inviaMessaggio(new Messaggio(Comando.ATTACCA, posizione));
        mioTurno = false; // Disabilita temporaneamente fino alla risposta
        
        if (grigliaView != null) {
            grigliaView.aggiornaStatoGioco("Attacco inviato... attendere risultato");
            grigliaView.attivaGrigliaAvversario(false);
        }
    }

    /**
     * Invia un messaggio in chat
     */
    public void inviaMessaggioChat(String testo) {
        if (nomeGiocatore == null) {
            LogUtility.warning("[CLIENT] Nome giocatore non impostato");
            return;
        }
        
        ChatView.MessaggioChat messaggioChat = new ChatView.MessaggioChat(nomeGiocatore, testo);
        inviaMessaggio(new Messaggio(Comando.MESSAGGIO_CHAT, messaggioChat));
    }

    /**
     * Invia il posizionamento delle navi
     */
    public void inviaPosizionamentoNavi(List<List<Posizione>> navi) {
        inviaMessaggio(new Messaggio(Comando.POSIZIONA_NAVI, navi));
        statoPartita = StatoPartita.ATTESA_BATTAGLIA;
        LogUtility.info("[CLIENT] Navi inviate al server");
    }

    // ================== NAVIGATION CALLBACKS ==================

    /**
     * Registra callback per la transizione START (lobby -> posizionamento)
     */
    public void setOnStartCallback(Runnable callback) {
        this.onStartCallback = callback;
        
        // Se START è già arrivato, esegui immediatamente il callback
        if (startRicevuto && callback != null) {
            LogUtility.info("[CLIENT] START già ricevuto, eseguendo callback immediatamente");
            Platform.runLater(callback);
        }
    }

    /**
     * Registra callback per la transizione INIZIO_BATTAGLIA (posizionamento -> griglia)
     */
    public void setOnInizioBattagliaCallback(Runnable callback) {
        this.onInizioBattagliaCallback = callback;
    }

    // ================== VIEW REGISTRATION ==================

    public void registraGrigliaView(GrigliaView view) {
        this.grigliaView = view;
    }

    public void registraChatView(ChatView view) {
        this.chatView = view;
    }

    // ================== UTILITY METHODS ==================

    private void inviaMessaggio(Messaggio messaggio) {
        if (!connesso || clientSocket.getOutputStream() == null) {
            LogUtility.error("[CLIENT] Tentativo di invio messaggio senza connessione!");
            return;
        }
        clientSocket.inviaMessaggio(messaggio);
    }

    public void disconnetti() {
        connesso = false;
        if (ascoltoThread != null) {
            ascoltoThread.interrupt();
        }
        inviaMessaggio(new Messaggio(Comando.DISCONNESSIONE, "Client si disconnette"));
        clientSocket.chiudiConnessione();
        LogUtility.info("[CLIENT] Disconnessione completata");
    }

    // ================== GETTERS ==================

    public boolean isConnesso() { return connesso; }
    public String getNomeGiocatore() { return nomeGiocatore; }
    public int getMyPlayerID() { return myPlayerID; }
    public boolean isMioTurno() { return mioTurno; }
    public StatoPartita getStatoPartita() { return statoPartita; }
    public ClientSocket getClientSocket() { return clientSocket; }

    // ================== INNER ENUM ==================

    public enum StatoPartita {
        LOBBY,
        POSIZIONAMENTO_NAVI,
        ATTESA_BATTAGLIA,
        BATTAGLIA,
        FINITA
    }
}