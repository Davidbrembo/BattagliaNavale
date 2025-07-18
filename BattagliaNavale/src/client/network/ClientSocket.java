package client.network;

import shared.protocol.Comando;
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
        out = new ObjectOutputStream(socket.getOutputStream()); //Prima output
        out.flush();
        in = new ObjectInputStream(socket.getInputStream()); //Poi input
        LogUtility.info("[CLIENT] Connesso al server su " + host + ":" + port);
    }


    // Invia un messaggio al server
    public void inviaMessaggio(Messaggio messaggio) {
        try {
            if (out == null) {
                LogUtility.error("[CLIENT] Errore: stream di output è null. Forse connect() non è stato chiamato?");
                return;
            }
            out.writeObject(messaggio);
            out.flush();
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
            // Invia messaggio di disconnessione prima di chiudere tutto
            if (out != null) {
                Messaggio disconnessione = new Messaggio(Comando.DISCONNESSIONE, "Client si disconnette");
                out.writeObject(disconnessione);
                out.flush();
            }

            //Chiusura stream e socket
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            LogUtility.info("[CLIENT] Connessione chiusa.");
        } catch (IOException e) {
            LogUtility.error("[CLIENT] Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }

}
