package server.network;

import shared.model.Posizione;
import shared.model.RisultatoAttacco;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.LogUtility;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());

            LogUtility.info("[SERVER] Nuovo client connesso da " + clientSocket.getInetAddress());

            Messaggio msg;
            while ((msg = (Messaggio) in.readObject()) != null) {
                LogUtility.info("[SERVER] Ricevuto messaggio: " + msg.getComando() + " - " + msg.getContenuto());

                switch (msg.getComando()) {
                case INVIA_NOME:
                    String nome = (String) msg.getContenuto();
                    LogUtility.info("[SERVER] Nome ricevuto: " + nome);
                    break;

                case ATTACCA:
                    Posizione posizione = (Posizione) msg.getContenuto();
                    LogUtility.info("[SERVER] Attacco ricevuto in posizione: " + posizione);

                    // ESEGUI L'ATTACCO sulla griglia del giocatore nemico (questa parte è simulata qui)
                    RisultatoAttacco risultato = new RisultatoAttacco(true, posizione); // <-- sostituiscilo con il vero risultato

                    // INVIA RISPOSTA AL CLIENT
                    inviaMessaggio(new Messaggio(Comando.RISPOSTA_ATTACCO, risultato));
                    break;

                case DISCONNETTI:
                    LogUtility.info("[SERVER] Il client ha richiesto la disconnessione.");
                    closeConnection();
                    return;

                default:
                    LogUtility.info("[SERVER] Comando non riconosciuto.");
                    break;
            }

            }

        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore nella gestione del client: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                LogUtility.info("[SERVER] Connessione con il client chiusa.");
            }
        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore nella chiusura della connessione: " + e.getMessage());
        }
    }
    
    private void inviaMessaggio(Messaggio messaggio) {
        try {
            out.writeObject(messaggio);
            out.flush();
        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore nell'invio del messaggio al client: " + e.getMessage());
        }
    }

}
