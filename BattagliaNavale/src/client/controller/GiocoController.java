package client.controller;

import java.io.IOException;
import client.network.ClientSocket;

public class GiocoController {

    private ClientSocket clientSocket;

    public GiocoController() {
        clientSocket = new ClientSocket();
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

}