package client.controller;

import client.network.ClientSocket;
import shared.protocol.Messaggio;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GiocoController {

    private static GiocoController instance;
    private final ClientSocket clientSocket;

    private static final Logger logger = Logger.getLogger(GiocoController.class.getName());

    public GiocoController() {
        clientSocket = ClientSocket.getInstance();
    }

    public static GiocoController getInstance() {
        if (instance == null) {
            instance = new GiocoController();
        }
        return instance;
    }

    public boolean iniziaConnessione() {
        try {
            clientSocket.connect("localhost", 12345);
            System.out.println("[CLIENT] Connessione avvenuta.");
            return true;
        } catch (IOException e) {
            System.out.println("[CLIENT] Errore di connessione: " + e.getMessage());
            return false;
        }
    }

    public void inviaMessaggio(Messaggio messaggio) {
        clientSocket.inviaMessaggio(messaggio);
    }


    public ClientSocket getClientSocket() {
        return clientSocket;
    }
    
    
}
