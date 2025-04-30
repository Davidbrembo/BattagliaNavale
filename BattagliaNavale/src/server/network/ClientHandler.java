package server.network;

import shared.protocol.Comando;
import shared.protocol.Messaggio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nomeGiocatore;
    Messaggio msg;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Nuovo client connesso: " + socket.getInetAddress());
            
			if (msg.getComando() == Comando.INVIA_NOME) {
                nomeGiocatore = (String) msg.getContenuto();
                System.out.println("Giocatore ha scelto il nome: " + nomeGiocatore);
            }

            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

            while (true) {
                Messaggio msg = (Messaggio) in.readObject();
                System.out.println("Messaggio ricevuto: " + msg.getComando());

                // ... (gestione dei messaggi)
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Errore con il client: " + e.getMessage());
        }
    }


    public void inviaMessaggio(Messaggio msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopHandler() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}