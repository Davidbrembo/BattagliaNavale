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
    private final int idGiocatore; // 0 o 1
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, int idGiocatore) {
        this.clientSocket = socket;
        this.idGiocatore = idGiocatore;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());

            // Notifica il client del suo ID
            inviaMessaggio(new Messaggio(Comando.ASSEGNA_ID, idGiocatore));
            LogUtility.info("[SERVER] Assegnato ID " + idGiocatore + " al client.");

            // Attendi che entrambi i client siano connessi
            if (idGiocatore == 0) {
                inviaMessaggio(new Messaggio(Comando.STATO, "In attesa del secondo giocatore..."));
            } else {
                inviaMessaggio(new Messaggio(Comando.STATO, "Partita pronta!"));
                // Notifica anche il client 0 che la partita può iniziare
                // (Se hai una lista globale di ClientHandler, puoi inviare un messaggio a entrambi)
            }

            // Resto della logica (ricezione messaggi, turni, ecc.)
            while ((in.readObject()) != null) {
                // Gestisci i comandi (es. ATTACCA, DISCONNETTI)
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

    private void inviaMessaggio(Messaggio messaggio) {
        try {
            out.writeObject(messaggio);
            out.flush();
        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nell'invio al client " + idGiocatore);
        }
    }
}