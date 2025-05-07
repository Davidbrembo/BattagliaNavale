package server.network;

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
    private ObjectInputStream in;
    private ObjectOutputStream out;

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
                }
            }

        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore con il client " + idGiocatore + ": " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nella chiusura del client " + idGiocatore);
        }
    }

    public void inviaMessaggio(Messaggio messaggio) {
        try {
            out.writeObject(messaggio);
            out.flush();
        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nell'invio al client " + idGiocatore);
        }
    }
}
