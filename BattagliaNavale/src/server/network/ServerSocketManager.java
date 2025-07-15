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
    private boolean[] naviPosizionate = new boolean[2];
    private int giocatoriPronti = 0;
    private boolean partitaFinita = false;
    @SuppressWarnings("unchecked")
	private List<NaveServer>[] naviPerGiocatore = new List[2];

    public ServerSocketManager(int porta) throws IOException {
        this.serverSocket = new ServerSocket(porta);
        this.gameManager = new ServerGameManager(10, 10);
        this.naviPerGiocatore[0] = new ArrayList<>();
        this.naviPerGiocatore[1] = new ArrayList<>();
        LogUtility.info("[SERVER] In ascolto sulla porta " + porta + ". Max connessioni: 2");
    }

    public void start() {
        LogUtility.info("[SERVER] Server avviato. Accettando connessioni...");
        
        while (clientHandlers.size() < 2) {
            try {
                Socket client = serverSocket.accept();
                int playerID = clientHandlers.size();

                LogUtility.info("[SERVER] Nuova connessione da: " + client.getInetAddress());

                ClientHandler handler = new ClientHandler(client, playerID, this);
                clientHandlers.add(handler);
                new Thread(handler).start();

                LogUtility.info("[SERVER] Connessi: " + clientHandlers.size() + "/2");

                if (clientHandlers.size() == 2) {
                    LogUtility.info("[SERVER] Entrambi i giocatori connessi. Aspettando inizializzazione...");
                    
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            
                            LogUtility.info("[SERVER] Inizio fase posizionamento navi.");
                            Messaggio startMsg = new Messaggio(Comando.START, "Inizia il posizionamento delle navi!");
                            
                            // Controlla che i client siano ancora connessi prima di inviare
                            if (clientHandlers.get(0) != null && clientHandlers.get(0).isConnessioneAttiva()) {
                                LogUtility.info("[SERVER] Inviando START al giocatore 0");
                                clientHandlers.get(0).inviaMessaggio(startMsg);
                            }
                            
                            if (clientHandlers.get(1) != null && clientHandlers.get(1).isConnessioneAttiva()) {
                                LogUtility.info("[SERVER] Inviando START al giocatore 1");  
                                clientHandlers.get(1).inviaMessaggio(startMsg);
                            }
                            
                            LogUtility.info("[SERVER] Messaggi START inviati a entrambi i giocatori");
                            
                        } catch (InterruptedException e) {
                            LogUtility.error("[SERVER] Errore durante l'attesa: " + e.getMessage());
                        }
                    }).start();
                }

            } catch (IOException e) {
                LogUtility.error("[SERVER] Errore I/O: " + e.getMessage());
            }
        }
        
        LogUtility.info("[SERVER] Partita completa (2/2 giocatori). Rifiutando nuove connessioni...");
        rifiutaNuoveConnessioni();
    }
    
    @SuppressWarnings("unused")
	public synchronized void gestisciDisconnessione(int giocatoreID) {
        LogUtility.info("[SERVER] Gestendo disconnessione del giocatore " + giocatoreID);
        
        if (partitaFinita) {
            LogUtility.info("[SERVER] Partita già finita, ignorando disconnessione");
            return;
        }
        
        if (giocatoriPronti == 2) {
            int vincitore = 1 - giocatoreID;
            
            LogUtility.info("[SERVER] Giocatore " + vincitore + " vince per disconnessione dell'avversario");
            
            if (vincitore < clientHandlers.size() && clientHandlers.get(vincitore) != null) {
				String nomeVincitore = clientHandlers.get(vincitore).getNomeGiocatore();
                clientHandlers.get(vincitore).inviaMessaggio(
                    new Messaggio(Comando.VITTORIA, "Hai vinto! L'avversario si è disconnesso.")
                );
                
                inviaNotificaChatSingola(vincitore, "Hai vinto la partita per disconnessione dell'avversario!");
            }
            
            partitaFinita = true;
        } else {
            LogUtility.info("[SERVER] Disconnessione durante fase di setup, partita non ancora iniziata");
            
            int altroGiocatore = 1 - giocatoreID;
            if (altroGiocatore < clientHandlers.size() && clientHandlers.get(altroGiocatore) != null) {
                clientHandlers.get(altroGiocatore).inviaMessaggio(
                    new Messaggio(Comando.ERRORE, "L'altro giocatore si è disconnesso. Tornando al menu...")
                );
            }
        }
        
        if (giocatoreID < clientHandlers.size()) {
            clientHandlers.set(giocatoreID, null);
        }
    }
    
    private boolean tutteNaviAffondate(int giocatoreID) {
        List<NaveServer> naviGiocatore = naviPerGiocatore[giocatoreID];
        
        for (NaveServer nave : naviGiocatore) {
            if (!nave.affondata()) {
                return false;
            }
        }
        
        LogUtility.info("[SERVER] 🏆 Tutte le navi del giocatore " + giocatoreID + " sono state affondate!");
        return true;
    }
    
    /**
     * Gestisce la fine della partita quando tutte le navi sono distrutte
     */
    private synchronized void gestisciFinePartita(int giocatoreSconfitto) {
        if (partitaFinita) {
            LogUtility.info("[SERVER] Partita già finita, ignorando ulteriore fine partita");
            return;
        }
        
        partitaFinita = true;
        int vincitore = 1 - giocatoreSconfitto;
        
        LogUtility.info("[SERVER] FINE PARTITA! Vincitore: Giocatore " + vincitore + 
                       " (" + clientHandlers.get(vincitore).getNomeGiocatore() + ")");
        
        String nomeVincitore = clientHandlers.get(vincitore).getNomeGiocatore();
        String nomeSconfitto = clientHandlers.get(giocatoreSconfitto).getNomeGiocatore();
        
        clientHandlers.get(vincitore).inviaMessaggio(
            new Messaggio(Comando.VITTORIA, "Complimenti " + nomeVincitore + "! Hai distrutto tutta la flotta nemica!")
        );
        
        clientHandlers.get(giocatoreSconfitto).inviaMessaggio(
            new Messaggio(Comando.SCONFITTA, "💀 " + nomeSconfitto + ", la tua flotta è stata completamente distrutta!")
        );
        
        // Invia notifiche finali nella chat
        inviaNotificaChatATutti("FINE PARTITA! 🏆 " + nomeVincitore + " ha vinto la battaglia navale!");
        inviaNotificaChatATutti("Risultato finale: " + nomeVincitore + " vs " + nomeSconfitto);
        
        LogUtility.info("[SERVER] Messaggi di fine partita inviati a entrambi i giocatori");
    }
    
    private void rifiutaNuoveConnessioni() {
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket clientInEccesso = serverSocket.accept();
                    LogUtility.warning("[SERVER] Connessione rifiutata da: " + clientInEccesso.getInetAddress() + 
                                     " (partita già completa)");
                    
                    try {
                        java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(clientInEccesso.getOutputStream());
                        out.flush();
                        
                        Messaggio rifiuto = new Messaggio(Comando.ERRORE, 
                            "Partita completa! Massimo 2 giocatori consentiti. Riprova più tardi.");
                        out.writeObject(rifiuto);
                        out.flush();
                        
                        Thread.sleep(100);
                        clientInEccesso.close();
                        
                        LogUtility.info("[SERVER] Client in eccesso disconnesso correttamente");
                    } catch (Exception e) {
                        LogUtility.error("[SERVER] Errore nel rifiutare client: " + e.getMessage());
                        try {
                            clientInEccesso.close();
                        } catch (IOException closeError) {
                        }
                    }
                }
            } catch (IOException e) {
                LogUtility.info("[SERVER] Thread di rifiuto connessioni terminato: " + e.getMessage());
            }
        }).start();
    }
    
    @SuppressWarnings("unchecked")
    public synchronized void gestisciPosizionamentoNavi(int giocatoreID, Object naviData) {
        if (naviPosizionate[giocatoreID]) {
            LogUtility.warning("[SERVER] Giocatore " + giocatoreID + " ha già posizionato le navi!");
            return;
        }
        
        try {
            List<List<Posizione>> naviGiocatore = (List<List<Posizione>>) naviData;
            LogUtility.info("[SERVER] Ricevute " + naviGiocatore.size() + " navi dal giocatore " + giocatoreID);
            
            if (!validaNumeroNavi(naviGiocatore)) {
                clientHandlers.get(giocatoreID).inviaMessaggio(
                    new Messaggio(Comando.ERRORE, "Numero di navi non valido! Riposiziona la flotta.")
                );
                return;
            }
            
            if (!validaDimensioniNavi(naviGiocatore)) {
                clientHandlers.get(giocatoreID).inviaMessaggio(
                    new Messaggio(Comando.ERRORE, "Dimensioni delle navi non valide! Riposiziona la flotta.")
                );
                return;
            }
            
            naviPerGiocatore[giocatoreID].clear();
            for (List<Posizione> posizioni : naviGiocatore) {
                NaveServer nave = new NaveServer(posizioni);
                gameManager.getGriglie()[giocatoreID].aggiungiNave(nave);
                naviPerGiocatore[giocatoreID].add(nave);
                LogUtility.info("[SERVER] Aggiunta nave per giocatore " + giocatoreID + 
                               " (lunghezza " + posizioni.size() + "): " + posizioni);
            }
            
            naviPosizionate[giocatoreID] = true;
            giocatoriPronti++;
            
            LogUtility.info("[SERVER] Giocatore " + giocatoreID + " ha posizionato le navi. Giocatori pronti: " + giocatoriPronti + "/2");
            
            if (giocatoriPronti == 2) {
                LogUtility.info("[SERVER] Entrambi i giocatori hanno posizionato le navi. Inizio battaglia!");
                
                Messaggio inizioBattaglia = new Messaggio(Comando.INIZIO_BATTAGLIA, "Battaglia iniziata!");
                for (ClientHandler handler : clientHandlers) {
                    if (handler != null) {
                        handler.inviaMessaggio(inizioBattaglia);
                    }
                }
                
                clientHandlers.get(0).inviaMessaggio(new Messaggio(Comando.TURNO, "È il tuo turno!"));
                clientHandlers.get(1).inviaMessaggio(new Messaggio(Comando.STATO, "Aspetta il tuo turno"));
                
                inviaNotificaChatATutti("La battaglia navale è iniziata! Buona fortuna!");
            }
            
        } catch (ClassCastException e) {
            LogUtility.error("[SERVER] Errore nel parsing delle navi dal giocatore " + giocatoreID + ": " + e.getMessage());
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.ERRORE, "Errore nel posizionamento delle navi")
            );
        }
    }
    
    private boolean validaNumeroNavi(List<List<Posizione>> navi) {
        return navi.size() == 5;
    }
    
    private boolean validaDimensioniNavi(List<List<Posizione>> navi) {
        int[] conteggioDimensioni = new int[6];
        
        for (List<Posizione> nave : navi) {
            int lunghezza = nave.size();
            if (lunghezza < 2 || lunghezza > 5) {
                LogUtility.warning("[SERVER] Nave con lunghezza non valida: " + lunghezza);
                return false;
            }
            conteggioDimensioni[lunghezza]++;
        }
        
        boolean valido = conteggioDimensioni[5] == 1 &&
                         conteggioDimensioni[4] == 1 &&
                         conteggioDimensioni[3] == 2 &&
                         conteggioDimensioni[2] == 1;
        
        if (!valido) {
            LogUtility.warning("[SERVER] Composizione flotta non valida: " +
                             "Portaerei(" + conteggioDimensioni[5] + "), " +
                             "Incrociatore(" + conteggioDimensioni[4] + "), " +
                             "Cacciatorpediniere(" + conteggioDimensioni[3] + "), " +
                             "Sottomarino(" + conteggioDimensioni[2] + ")");
        }
        
        return valido;
    }
    
    public synchronized void gestisciAttacco(int giocatoreID, Posizione posizione) {
        if (partitaFinita) {
            LogUtility.warning("[SERVER] Tentativo di attacco dopo la fine della partita");
            return;
        }
        
        if (giocatoriPronti < 2) {
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.ERRORE, "Aspetta che entrambi i giocatori posizionino le navi!")
            );
            return;
        }
        
        if (gameManager.getTurno() != giocatoreID) {
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.ERRORE, "Non è il tuo turno!")
            );
            return;
        }
        
        LogUtility.info("[SERVER] Giocatore " + giocatoreID + " attacca in " + posizione);
        
        RisultatoAttacco risultato = gameManager.attacca(posizione);
        
        int attaccante = giocatoreID;
        int difensore = 1 - giocatoreID;
        
        Messaggio msgRisultatoAttacco = new Messaggio(Comando.RISULTATO_ATTACCO, risultato);
        clientHandlers.get(attaccante).inviaMessaggio(msgRisultatoAttacco);
        
        Messaggio msgAttaccoRicevuto = new Messaggio(Comando.ATTACCO_RICEVUTO, risultato);
        clientHandlers.get(difensore).inviaMessaggio(msgAttaccoRicevuto);
        
        LogUtility.info("[SERVER] Risultato attacco: " + (risultato.isColpito() ? "COLPITO" : "MANCATO") + 
                       " in posizione " + posizione);
        
        if (risultato.isColpito() && risultato.isNaveAffondata()) {
            LogUtility.info("[SERVER] 🚢 Nave affondata! Controllando se tutte le navi sono distrutte...");
            
            if (tutteNaviAffondate(difensore)) {
                gestisciFinePartita(difensore);
                return;
            }
        }
        
        if (!risultato.isColpito()) {
            gameManager.passaTurno();
            int nuovoTurno = gameManager.getTurno();
            
            clientHandlers.get(nuovoTurno).inviaMessaggio(
                new Messaggio(Comando.TURNO, "È il tuo turno!")
            );
            clientHandlers.get(1 - nuovoTurno).inviaMessaggio(
                new Messaggio(Comando.STATO, "Aspetta il tuo turno")
            );
            
            LogUtility.info("[SERVER] Turno passato al giocatore " + nuovoTurno);
        } else {
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.TURNO, "Hai colpito! Continua il tuo turno.")
            );
            LogUtility.info("[SERVER] Giocatore " + giocatoreID + " ha colpito, continua il turno");
        }
    }
    
    public synchronized void gestisciMessaggioChat(int giocatoreID, Object messaggioData) {
        if (clientHandlers.size() < 2) {
            LogUtility.warning("[SERVER] Tentativo di invio messaggio chat con meno di 2 giocatori connessi");
            return;
        }
        
        try {
            LogUtility.info("[SERVER] Inoltrando messaggio chat dal giocatore " + giocatoreID);
            
            int destinatario = 1 - giocatoreID;
            
            if (destinatario < clientHandlers.size() && clientHandlers.get(destinatario) != null) {
                Messaggio messaggioChat = new Messaggio(Comando.MESSAGGIO_CHAT, messaggioData);
                clientHandlers.get(destinatario).inviaMessaggio(messaggioChat);
                
                LogUtility.info("[SERVER] Messaggio chat inoltrato dal giocatore " + giocatoreID + 
                               " al giocatore " + destinatario);
            }
                           
        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore nell'gestione messaggio chat: " + e.getMessage());
        }
    }
    
    private void inviaNotificaChatATutti(String testo) {
        if (clientHandlers.size() < 2) return;
        
        try {
            Class<?> messaggioChatClass = Class.forName("client.view.ChatView$MessaggioChat");
            Object messaggioSistema = messaggioChatClass
                .getDeclaredConstructor(String.class, String.class)
                .newInstance("🤖 Sistema", testo);
            
            Messaggio msg = new Messaggio(Comando.MESSAGGIO_CHAT, messaggioSistema);
            
            for (ClientHandler handler : clientHandlers) {
                if (handler != null) {
                    handler.inviaMessaggio(msg);
                }
            }
        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore nell'invio notifica chat: " + e.getMessage());
        }
    }
    
    private void inviaNotificaChatSingola(int giocatoreID, String testo) {
        if (giocatoreID >= clientHandlers.size() || clientHandlers.get(giocatoreID) == null) return;
        
        try {
            Class<?> messaggioChatClass = Class.forName("client.view.ChatView$MessaggioChat");
            Object messaggioSistema = messaggioChatClass
                .getDeclaredConstructor(String.class, String.class)
                .newInstance("Sistema", testo);
            
            Messaggio msg = new Messaggio(Comando.MESSAGGIO_CHAT, messaggioSistema);
            clientHandlers.get(giocatoreID).inviaMessaggio(msg);
        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore nell'invio notifica chat singola: " + e.getMessage());
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