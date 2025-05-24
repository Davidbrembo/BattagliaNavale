package server.network;

import shared.model.Posizione;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.LogUtility;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import client.network.ClientSocket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final int idGiocatore;
    private final ServerSocketManager serverManager;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ClientSocket cs;

    public ClientHandler(Socket socket, int idGiocatore, ServerSocketManager serverManager) {
        this.clientSocket = socket;
        this.idGiocatore = idGiocatore;
        this.serverManager = serverManager;
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
            while (true) {
                Object obj = in.readObject();
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
                            LogUtility.info("[SERVER] Nome giocatore ricevuto: " + messaggio.getContenuto());
                        }
                        case ATTACCA -> {
                            // Gestisci l'attacco tramite il ServerSocketManager
                            if (messaggio.getContenuto() instanceof Posizione posizione) {
                                serverManager.gestisciAttacco(idGiocatore, posizione);
                            } else {
                                LogUtility.error("[SERVER] Attacco ricevuto senza posizione valida");
                            }
                        }
                        case DISCONNESSIONE -> {
                            LogUtility.info("[SERVER] Client " + idGiocatore + " disconnesso.");
                            inviaMessaggio(new Messaggio(Comando.DISCONNESSIONE, "Connessione chiusa."));
                            closeConnection();
                            return;
                        }
                        default -> LogUtility.info("[SERVER] Comando non gestito: " + messaggio.getComando());
                    }
                }
            }

        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore con il client " + idGiocatore + ": " + e.getMessage());
            inviaMessaggio(new Messaggio(Comando.DISCONNESSIONE, "Connessione chiusa."));
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
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
    }

    public void inviaMessaggio(Messaggio messaggio) {
        if (clientSocket == null || clientSocket.isClosed()) {
            LogUtility.info("[SERVER] Tentativo di invio messaggio su socket chiuso per client " + idGiocatore);
            return;
        }
        try {
            out.writeObject(messaggio);
            out.flush();
        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nell'invio al client " + idGiocatore + ": " + e.getMessage());
            closeConnection();
        }
    }
}