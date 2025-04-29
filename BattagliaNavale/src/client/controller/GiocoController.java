package client.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import client.network.ClientSocket;
import shared.model.Coordinate;

public class GiocoController {

    private ClientSocket clientSocket;

    public GiocoController() {
        clientSocket = new ClientSocket();
    }

    public void iniziaConnessione() {
        try {
            clientSocket.connect("localhost", 12345);
            System.out.println("[CLIENT] Connessione avvenuta.");
        } catch (IOException e) {
            System.out.println("[CLIENT] Errore di connessione: " + e.getMessage());
        }
    }
}
