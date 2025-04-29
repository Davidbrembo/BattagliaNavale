package server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import utility.LogUtility;

public class ServerSocketManager {

    private int port;

    public ServerSocketManager(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LogUtility.info("[SERVER] In ascolto sulla porta " + port);

            while (true) {
                try {
                    // Accetta la connessione
                    Socket clientSocket = serverSocket.accept();
                    LogUtility.info("[SERVER] Nuovo client connesso da " + clientSocket.getInetAddress());

                    // Crea un thread per gestire il client
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                } catch (IOException e) {
                    LogUtility.error("[SERVER] Errore durante l'accettazione della connessione: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore durante l'avvio del server: " + e.getMessage());
        } catch (Exception e) {
            LogUtility.error("[SERVER] Errore generale: " + e.getMessage());
        }
    }
}
