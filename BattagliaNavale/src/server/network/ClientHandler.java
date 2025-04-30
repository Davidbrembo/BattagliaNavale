package server.network;

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
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            LogUtility.info("[SERVER] Nuovo client connesso da " + clientSocket.getInetAddress());

            Messaggio msg;
            while ((msg = (Messaggio) in.readObject()) != null) {
                LogUtility.info("[SERVER] Ricevuto messaggio: " + msg.getComando() + " - " + msg.getContenuto());

                switch (msg.getComando()) {
                    case INVIA_NOME:
                        String nome = (String) msg.getContenuto();
                        LogUtility.info("[SERVER] Nome ricevuto: " + nome);
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
}
