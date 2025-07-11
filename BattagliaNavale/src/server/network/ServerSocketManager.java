package server.network;

import server.model.NaveServer;
import server.model.ServerGameManager;
import shared.model.Posizione;
import shared.model.RisultatoAttacco;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.LogUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerSocketManager {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers = new ArrayList<>();
    private ServerGameManager gameManager;
    private boolean[] naviPosizionate = new boolean[2]; // Traccia se i giocatori hanno posizionato le navi
    private int giocatoriPronti = 0;

    public ServerSocketManager(int porta) throws IOException {
        this.serverSocket = new ServerSocket(porta);
        this.gameManager = new ServerGameManager(10, 10); // Griglia 10x10
        LogUtility.info("[SERVER] In ascolto sulla porta " + porta + ". Max connessioni: 2");
    }

    public void start() {
        LogUtility.info("[SERVER] Server avviato. Accettando connessioni...");
        
        // Accetta fino a 2 client per la partita
        while (clientHandlers.size() < 2) {
            try {
                Socket client = serverSocket.accept();
                int playerID = clientHandlers.size(); // 0 o 1

                LogUtility.info("[SERVER] Nuova connessione da: " + client.getInetAddress());

                ClientHandler handler = new ClientHandler(client, playerID, this);
                clientHandlers.add(handler);
                new Thread(handler).start();

                LogUtility.info("[SERVER] Connessi: " + clientHandlers.size() + "/2");

                // Se entrambi sono connessi, invia START per iniziare il posizionamento
                if (clientHandlers.size() == 2) {
                    LogUtility.info("[SERVER] 🚀 Entrambi i giocatori connessi. Inizio fase posizionamento navi.");
                    Messaggio startMsg = new Messaggio(Comando.START, "Inizia il posizionamento delle navi!");
                    
                    LogUtility.info("[SERVER] Inviando START al giocatore 0");
                    clientHandlers.get(0).inviaMessaggio(startMsg);
                    
                    LogUtility.info("[SERVER] Inviando START al giocatore 1");  
                    clientHandlers.get(1).inviaMessaggio(startMsg);
                    
                    LogUtility.info("[SERVER] ✅ Messaggi START inviati a entrambi i giocatori");
                }

            } catch (IOException e) {
                LogUtility.error("[SERVER] Errore I/O: " + e.getMessage());
            }
        }
        
        // Ora che abbiamo 2 client, rifiuta ulteriori connessioni
        LogUtility.info("[SERVER] 🚫 Partita completa (2/2 giocatori). Rifiutando nuove connessioni...");
        rifiutaNuoveConnessioni();
    }
    
    /**
     * Rifiuta tutte le nuove connessioni dopo che la partita è iniziata
     */
    private void rifiutaNuoveConnessioni() {
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket clientInEccesso = serverSocket.accept();
                    LogUtility.warning("[SERVER] ⚠️ Connessione rifiutata da: " + clientInEccesso.getInetAddress() + 
                                     " (partita già completa)");
                    
                    // Invia messaggio di rifiuto e chiudi immediatamente
                    try {
                        java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(clientInEccesso.getOutputStream());
                        out.flush();
                        
                        Messaggio rifiuto = new Messaggio(Comando.ERRORE, 
                            "❌ Partita completa! Massimo 2 giocatori consentiti. Riprova più tardi.");
                        out.writeObject(rifiuto);
                        out.flush();
                        
                        Thread.sleep(100); // Piccola pausa per assicurarsi che il messaggio arrivi
                        clientInEccesso.close();
                        
                        LogUtility.info("[SERVER] ✅ Client in eccesso disconnesso correttamente");
                    } catch (Exception e) {
                        LogUtility.error("[SERVER] Errore nel rifiutare client: " + e.getMessage());
                        try {
                            clientInEccesso.close();
                        } catch (IOException closeError) {
                            // Ignora errori di chiusura
                        }
                    }
                }
            } catch (IOException e) {
                LogUtility.info("[SERVER] Thread di rifiuto connessioni terminato: " + e.getMessage());
            }
        }).start();
    }
    
    // Gestisce il posizionamento delle navi da parte di un giocatore
    @SuppressWarnings("unchecked")
    public synchronized void gestisciPosizionamentoNavi(int giocatoreID, Object naviData) {
        if (naviPosizionate[giocatoreID]) {
            LogUtility.warning("[SERVER] Giocatore " + giocatoreID + " ha già posizionato le navi!");
            return;
        }
        
        try {
            List<List<Posizione>> naviGiocatore = (List<List<Posizione>>) naviData;
            LogUtility.info("[SERVER] Ricevute " + naviGiocatore.size() + " navi dal giocatore " + giocatoreID);
            
            // Aggiungi le navi alla griglia del giocatore
            for (List<Posizione> posizioni : naviGiocatore) {
                NaveServer nave = new NaveServer(posizioni);
                gameManager.getGriglie()[giocatoreID].aggiungiNave(nave);
                LogUtility.info("[SERVER] Aggiunta nave per giocatore " + giocatoreID + ": " + posizioni);
            }
            
            naviPosizionate[giocatoreID] = true;
            giocatoriPronti++;
            
            LogUtility.info("[SERVER] Giocatore " + giocatoreID + " ha posizionato le navi. Giocatori pronti: " + giocatoriPronti + "/2");
            
            // Se entrambi i giocatori hanno posizionato le navi, inizia la battaglia
            if (giocatoriPronti == 2) {
                LogUtility.info("[SERVER] Entrambi i giocatori hanno posizionato le navi. Inizio battaglia!");
                
                Messaggio inizioBattaglia = new Messaggio(Comando.INIZIO_BATTAGLIA, "Battaglia iniziata!");
                for (ClientHandler handler : clientHandlers) {
                    handler.inviaMessaggio(inizioBattaglia);
                }
                
                // Informa il primo giocatore che è il suo turno
                clientHandlers.get(0).inviaMessaggio(new Messaggio(Comando.TURNO, "È il tuo turno!"));
                clientHandlers.get(1).inviaMessaggio(new Messaggio(Comando.STATO, "Aspetta il tuo turno"));
                
                // Invia notifica di connessione nella chat
                inviaNotificaChat("🌊 La battaglia navale è iniziata! Buona fortuna!");
            }
            
        } catch (ClassCastException e) {
            LogUtility.error("[SERVER] Errore nel parsing delle navi dal giocatore " + giocatoreID + ": " + e.getMessage());
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.ERRORE, "Errore nel posizionamento delle navi")
            );
        }
    }
    
    // Gestisce un attacco da parte di un giocatore
    public synchronized void gestisciAttacco(int giocatoreID, Posizione posizione) {
        // Controlla che entrambi i giocatori abbiano posizionato le navi
        if (giocatoriPronti < 2) {
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.ERRORE, "Aspetta che entrambi i giocatori posizionino le navi!")
            );
            return;
        }
        
        if (gameManager.getTurno() != giocatoreID) {
            // Non è il turno di questo giocatore
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.ERRORE, "Non è il tuo turno!")
            );
            return;
        }
        
        LogUtility.info("[SERVER] Giocatore " + giocatoreID + " attacca in " + posizione);
        
        // Esegui l'attacco
        RisultatoAttacco risultato = gameManager.attacca(posizione);
        
        // Invia messaggi differenziati:
        // 1. All'attaccante: RISULTATO_ATTACCO (per aggiornare la griglia avversario)
        // 2. Al difensore: ATTACCO_RICEVUTO (per aggiornare la propria griglia)
        int attaccante = giocatoreID;
        int difensore = 1 - giocatoreID;
        
        // Messaggio per l'attaccante (risultato del suo attacco)
        Messaggio msgRisultatoAttacco = new Messaggio(Comando.RISULTATO_ATTACCO, risultato);
        clientHandlers.get(attaccante).inviaMessaggio(msgRisultatoAttacco);
        
        // Messaggio per il difensore (attacco ricevuto sulla sua griglia)
        Messaggio msgAttaccoRicevuto = new Messaggio(Comando.ATTACCO_RICEVUTO, risultato);
        clientHandlers.get(difensore).inviaMessaggio(msgAttaccoRicevuto);
        
        LogUtility.info("[SERVER] Risultato attacco: " + (risultato.isColpito() ? "COLPITO" : "MANCATO") + 
                       " in posizione " + posizione);
        
        // Se non ha colpito, passa il turno
        if (!risultato.isColpito()) {
            gameManager.passaTurno();
            int nuovoTurno = gameManager.getTurno();
            
            // Informa i giocatori del cambio turno
            clientHandlers.get(nuovoTurno).inviaMessaggio(
                new Messaggio(Comando.TURNO, "È il tuo turno!")
            );
            clientHandlers.get(1 - nuovoTurno).inviaMessaggio(
                new Messaggio(Comando.STATO, "Aspetta il tuo turno")
            );
            
            LogUtility.info("[SERVER] Turno passato al giocatore " + nuovoTurno);
        } else {
            // Ha colpito, continua il suo turno
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.TURNO, "Hai colpito! Continua il tuo turno.")
            );
            LogUtility.info("[SERVER] Giocatore " + giocatoreID + " ha colpito, continua il turno");
        }
    }
    
    // Gestisce i messaggi di chat tra i giocatori
    public synchronized void gestisciMessaggioChat(int giocatoreID, Object messaggioData) {
        if (clientHandlers.size() < 2) {
            LogUtility.warning("[SERVER] Tentativo di invio messaggio chat con meno di 2 giocatori connessi");
            return;
        }
        
        try {
            LogUtility.info("[SERVER] Inoltrando messaggio chat dal giocatore " + giocatoreID);
            
            // Inoltra il messaggio all'altro giocatore
            int destinatario = 1 - giocatoreID; // Se è 0 diventa 1, se è 1 diventa 0
            
            Messaggio messaggioChat = new Messaggio(Comando.MESSAGGIO_CHAT, messaggioData);
            clientHandlers.get(destinatario).inviaMessaggio(messaggioChat);
            
            LogUtility.info("[SERVER] Messaggio chat inoltrato dal giocatore " + giocatoreID + 
                           " al giocatore " + destinatario);
                           
        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore nell'gestione messaggio chat: " + e.getMessage());
        }
    }
    
    // Invia una notifica di sistema nella chat a tutti i giocatori
    private void inviaNotificaChat(String testo) {
        if (clientHandlers.size() < 2) return;
        
        // Crea un messaggio di sistema (mittente = "Sistema")
        try {
            Class<?> messaggioChatClass = Class.forName("client.view.ChatView$MessaggioChat");
            Object messaggioSistema = messaggioChatClass
                .getDeclaredConstructor(String.class, String.class)
                .newInstance("🤖 Sistema", testo);
            
            Messaggio msg = new Messaggio(Comando.MESSAGGIO_CHAT, messaggioSistema);
            
            for (ClientHandler handler : clientHandlers) {
                handler.inviaMessaggio(msg);
            }
        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore nell'invio notifica chat: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nella chiusura: " + e.getMessage());
        }
    }
}