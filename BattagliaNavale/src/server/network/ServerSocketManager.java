package server.network;

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

    public ServerSocketManager(int porta) throws IOException {
        this.serverSocket = new ServerSocket(porta);
        LogUtility.info("[SERVER] In ascolto sulla porta " + porta + ". Max connessioni: 2");
    }

    public void start() {
        while (clientHandlers.size() < 2) {
            try {
                Socket client = serverSocket.accept();
                int playerID = clientHandlers.size(); // 0 o 1

                LogUtility.info("[SERVER] Nuova connessione da: " + client.getInetAddress());

                ClientHandler handler = new ClientHandler(client, playerID);
                clientHandlers.add(handler);
                new Thread(handler).start();

                LogUtility.info("[SERVER] Connessi: " + clientHandlers.size());

                // Se entrambi sono connessi, invia START a tutti
                if (clientHandlers.size() == 2) {
                    LogUtility.info("[SERVER] Entrambi i giocatori connessi. Inizio partita.");
                    Messaggio startMsg = new Messaggio(Comando.START, "Partita pronta!");
                    for (ClientHandler ch : clientHandlers) {
                        ch.inviaMessaggio(startMsg);
                    }
                }

            } catch (IOException e) {
                LogUtility.error("[SERVER] Errore I/O: " + e.getMessage());
            }
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
