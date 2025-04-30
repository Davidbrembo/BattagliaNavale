package client.network;

import java.io.IOException;
import java.net.Socket;

import utility.LogUtility;

public class ClientSocket {

    private Socket socket;

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        LogUtility.info("[CLIENT] Connesso al server su " + host + ":" + port);
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                LogUtility.info("[CLIENT] Connessione chiusa.");
            }
        } catch (IOException e) {
            LogUtility.error("[CLIENT] Errore durante la disconnessione: " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return socket;
    }
}