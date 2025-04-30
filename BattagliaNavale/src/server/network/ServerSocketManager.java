package server.network;

import utility.LogUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketManager {

    private ServerSocket serverSocket;

    public void start(int porta) {
        try {
            serverSocket = new ServerSocket(porta);
            LogUtility.info("[SERVER] In ascolto sulla porta " + porta);

            while (true) {
                Socket client = serverSocket.accept();
                LogUtility.info("[SERVER] Nuovo client connesso da " + client.getInetAddress());
                ClientHandler handler = new ClientHandler(client);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nel server socket: " + e.getMessage());
        }
    }
}
