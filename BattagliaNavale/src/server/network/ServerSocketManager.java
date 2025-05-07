package server.network;

import utility.LogUtility;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ServerSocketManager {
    private final ServerSocket serverSocket;
    private final Semaphore connectionSemaphore = new Semaphore(2); // Permessi per 2 connessioni

    public ServerSocketManager(int porta) throws IOException {
        this.serverSocket = new ServerSocket(porta);
        LogUtility.info("[SERVER] In ascolto sulla porta " + porta + ". Max connessioni: 2");
    }

    public void start() {
        while (true) {
            try {
                connectionSemaphore.acquire(); // Blocca se già 2 connessioni
                
                Socket client = serverSocket.accept();
                LogUtility.info("[SERVER] Nuova connessione da: " + client.getInetAddress());
                
                new Thread(() -> {
                    try {
                        ClientHandler handler = new ClientHandler(client, connectionSemaphore.availablePermits());
                        handler.run();
                    } finally {
                        connectionSemaphore.release(); // Libera il permesso
                        LogUtility.info("[SERVER] Slot disponibili: " + connectionSemaphore.availablePermits());
                    }
                }).start();
                
            } catch (IOException e) {
                LogUtility.error("[SERVER] Errore I/O: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LogUtility.error("[SERVER] Interrotto: " + e.getMessage());
            }
        }
    }

    // Metodo per chiudere il server
    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nella chiusura: " + e.getMessage());
        }
    }
}