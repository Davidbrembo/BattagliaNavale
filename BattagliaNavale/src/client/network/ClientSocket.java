package client.network;

import shared.protocol.Messaggio;
import utility.LogUtility;

import java.io.*;
import java.net.Socket;

public class ClientSocket {

    private static ClientSocket instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ClientSocket() {
        // Costruttore privato per implementare il pattern Singleton
    }

    public static ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    // Connessione al server
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        LogUtility.info("[CLIENT] Connesso al server su " + host + ":" + port);
    }

    // Invia un messaggio al server
    public void inviaMessaggio(Messaggio msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
            } else {
                LogUtility.error("[CLIENT] Stream di output non inizializzato.");
            }
        } catch (IOException e) {
            LogUtility.error("[CLIENT] Errore nell'invio del messaggio: " + e.getMessage());
        }
    }

    // Ricevi un messaggio dal server
    public Messaggio riceviMessaggio() {
        try {
            if (in != null) {
                return (Messaggio) in.readObject();
            } else {
                LogUtility.error("[CLIENT] Stream di input non inizializzato.");
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            LogUtility.error("[CLIENT] Errore nella ricezione del messaggio: " + e.getMessage());
            return null;
        }
    }

    // Getter per l'input stream
    public ObjectInputStream getInputStream() {
        return in;
    }

    // Getter per l'output stream
    public ObjectOutputStream getOutputStream() {
        return out;
    }

    // Chiudi la connessione al server
    public void chiudiConnessione() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            LogUtility.info("[CLIENT] Connessione chiusa.");
        } catch (IOException e) {
            LogUtility.error("[CLIENT] Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }
}
