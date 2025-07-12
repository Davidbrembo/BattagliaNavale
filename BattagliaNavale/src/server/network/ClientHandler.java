package server.network;

import shared.model.Posizione;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.LogUtility;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int idGiocatore;
    private final ServerSocketManager serverManager;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nomeGiocatore; // Nome predefinito
    private boolean connessioneAttiva = true; // Nuovo flag per tracciare lo stato della connessione

    public ClientHandler(Socket socket, int idGiocatore, ServerSocketManager serverManager) {
        this.clientSocket = socket;
        this.idGiocatore = idGiocatore;
        this.serverManager = serverManager;
        this.nomeGiocatore = "Giocatore " + (idGiocatore + 1); // Inizializza il nome predefinito
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            // Invia solo l'ID al client
            inviaMessaggio(new Messaggio(Comando.ASSEGNA_ID, idGiocatore));
            LogUtility.info("[SERVER] Assegnato ID " + idGiocatore + " al client.");

            // Attendi comandi dal client
            while (connessioneAttiva && !clientSocket.isClosed()) {
                try {
                    Object obj = in.readObject();
                    if (obj == null) {
                        LogUtility.warning("[SERVER] Ricevuto oggetto null da client " + idGiocatore + " - possibile disconnessione");
                        break;
                    }
                    
                    if (obj instanceof Messaggio messaggio) {
                        LogUtility.info("[SERVER] Ricevuto dal client " + idGiocatore + ": " + messaggio);

                        switch (messaggio.getComando()) {
                            case START -> {
                                LogUtility.info("[SERVER] Inizio partita per il client " + idGiocatore);
                            }
                            case ASSEGNA_ID -> {
                                LogUtility.info("[SERVER] Assegnato ID " + idGiocatore + " al client.");
                            }
                            case INVIA_NOME -> {
                                nomeGiocatore = (String) messaggio.getContenuto();
                                LogUtility.info("[SERVER] Nome giocatore ricevuto: " + nomeGiocatore);
                            }
                            case POSIZIONA_NAVI -> {
                                // Gestisci il posizionamento delle navi
                                LogUtility.info("[SERVER] Ricevuto posizionamento navi dal client " + idGiocatore);
                                serverManager.gestisciPosizionamentoNavi(idGiocatore, messaggio.getContenuto());
                            }
                            case ATTACCA -> {
                                // Gestisci l'attacco tramite il ServerSocketManager
                                if (messaggio.getContenuto() instanceof Posizione posizione) {
                                    serverManager.gestisciAttacco(idGiocatore, posizione);
                                } else {
                                    LogUtility.error("[SERVER] Attacco ricevuto senza posizione valida");
                                }
                            }
                            case MESSAGGIO_CHAT -> {
                                // Gestisci i messaggi di chat
                                LogUtility.info("[SERVER] Messaggio chat ricevuto dal client " + idGiocatore);
                                serverManager.gestisciMessaggioChat(idGiocatore, messaggio.getContenuto());
                            }
                            case DISCONNESSIONE -> {
                                LogUtility.info("[SERVER] Client " + idGiocatore + " ha richiesto disconnessione.");
                                // Gestisci la disconnessione tramite il ServerSocketManager
                                serverManager.gestisciDisconnessione(idGiocatore);
                                connessioneAttiva = false;
                                return;
                            }
                            default -> LogUtility.info("[SERVER] Comando non gestito: " + messaggio.getComando());
                        }
                    }
                } catch (java.net.SocketException | java.io.EOFException e) {
                    // Questi errori indicano disconnessione del client
                    LogUtility.warning("[SERVER] Client " + idGiocatore + " disconnesso improvvisamente: " + e.getClass().getSimpleName());
                    break;
                } catch (java.io.IOException e) {
                    // Altri errori di I/O
                    LogUtility.error("[SERVER] Errore I/O con client " + idGiocatore + ": " + e.getMessage());
                    break;
                }
            }

        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore generale con il client " + idGiocatore + ": " + e.getMessage());
        } finally {
            // Se arriviamo qui e la connessione era ancora attiva, significa disconnessione improvvisa
            if (connessioneAttiva) {
                LogUtility.warning("[SERVER] Client " + idGiocatore + " disconnesso improvvisamente - notificando server");
                serverManager.gestisciDisconnessione(idGiocatore);
                connessioneAttiva = false;
            }
            closeConnection();
        }
    }

    private void closeConnection() {
        connessioneAttiva = false;
        
        try {
            if (in != null) {
                in.close();
                in = null;
            }
        } catch (IOException e) {
            LogUtility.info("[SERVER] InputStream già chiuso client " + idGiocatore);
        }
        try {
            if (out != null) {
                out.close();
                out = null;
            }
        } catch (IOException e) {
            LogUtility.info("[SERVER] OutputStream già chiuso client " + idGiocatore);
        }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            LogUtility.info("[SERVER] Socket già chiuso client " + idGiocatore);
        }
        
        LogUtility.info("[SERVER] Connessione client " + idGiocatore + " chiusa completamente");
    }

    public void inviaMessaggio(Messaggio messaggio) {
        if (!connessioneAttiva || clientSocket == null || clientSocket.isClosed()) {
            LogUtility.info("[SERVER] Tentativo di invio messaggio su connessione non attiva per client " + idGiocatore);
            return;
        }
        try {
            out.writeObject(messaggio);
            out.flush();
        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nell'invio al client " + idGiocatore + ": " + e.getMessage());
            
            // Se c'è un errore di invio, considera la connessione persa
            if (connessioneAttiva) {
                LogUtility.warning("[SERVER] Connessione persa durante invio messaggio, client " + idGiocatore);
                serverManager.gestisciDisconnessione(idGiocatore);
                connessioneAttiva = false;
            }
            closeConnection();
        }
    }

    public String getNomeGiocatore() {
        return nomeGiocatore;
    }

    public int getIdGiocatore() {
        return idGiocatore;
    }
    
    public boolean isConnessioneAttiva() {
        return connessioneAttiva && clientSocket != null && !clientSocket.isClosed();
    }
}