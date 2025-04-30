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
        	//su un pc starti server (e se vuoi anche client)
        	//su questo pc il client può tenere (in clientSocket.connect) localhost oppure l'ip che da' ipconfig dopo ipv4
        	//sugli altri pc devi mettere per forza l'ip DEL SERVER
            clientSocket.connect("192.168.1.156", 12345);
            System.out.println("[CLIENT] Connessione avvenuta.");
            return true;
        } catch (IOException e) {
            System.out.println("[CLIENT] Errore di connessione: " + e.getMessage());
            return false;
        }
    }

}