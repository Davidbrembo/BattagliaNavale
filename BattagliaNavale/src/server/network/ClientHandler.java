package server.network;

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
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ClientSocket cs;

    public ClientHandler(Socket socket, int idGiocatore) {
        this.clientSocket = socket;
        this.idGiocatore = idGiocatore;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush(); // Assicura che lo stream sia pronto
            in = new ObjectInputStream(clientSocket.getInputStream());

            // Invia solo l'ID al client
            inviaMessaggio(new Messaggio(Comando.ASSEGNA_ID, idGiocatore));
            LogUtility.info("[SERVER] Assegnato ID " + idGiocatore + " al client.");

            // Attendi comandi dal client
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof Messaggio messaggio) {
                    // TODO: gestire i messaggi ricevuti dal client (es. ATTACCA, DISCONNETTI, ecc.)
                    LogUtility.info("[SERVER] Ricevuto dal client " + idGiocatore + ": " + messaggio);

					switch (messaggio.getComando()) {
						case START -> {
							LogUtility.info("[SERVER] Inizio partita per il client " + idGiocatore);
							// Inviare messaggio di inizio partita
						}
						case ASSEGNA_ID -> {
							LogUtility.info("[SERVER] Assegnato ID " + idGiocatore + " al client.");
							// Inviare messaggio di assegnazione ID
						}
						case INVIA_NOME -> {
							LogUtility.info("[SERVER] Nome giocatore ricevuto: " + messaggio.getContenuto());
							// Gestire il nome del giocatore
						}
						case ATTACCA -> {
							LogUtility.info("[SERVER] Giocatore " + idGiocatore + " ha attaccato.");
							// Gestire l'attacco
						}
						case DISCONNESSIONE -> {
						    LogUtility.info("[SERVER] Client " + idGiocatore + " disconnesso.");
						    inviaMessaggio(new Messaggio(Comando.DISCONNESSIONE, "Connessione chiusa."));
						    closeConnection();
						    return; // esci dal thread
						}

						// Aggiungere altri casi per gestire i comandi
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
            return; // evita di inviare se socket chiuso
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
