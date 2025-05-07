package client.controller;

import client.network.ClientSocket;
import shared.model.RisultatoAttacco;
import shared.protocol.Messaggio;

import java.io.IOException;
import java.io.ObjectInputStream;

public class GiocoController {

    private static GiocoController instance;
    public final ClientSocket clientSocket;
    private String nomeGiocatore;

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
            System.out.println("[SERVER] " + clientSocket.riceviMessaggio());
            return true;
        } catch (IOException e) {
            System.out.println("[CLIENT] Errore di connessione: " + e.getMessage());
            return false;
        }
    }

    public void inviaMessaggio(Messaggio messaggio) {
        clientSocket.inviaMessaggio(messaggio); // IOException non serve più catcharlo
    }

    /*public RisultatoAttacco getRisultatoAttacco() {
        System.out.println(clientSocket.riceviMessaggio());
		return clientSocket.riceviMessaggio().getContenuto() instanceof RisultatoAttacco ? (RisultatoAttacco) clientSocket.riceviMessaggio().getContenuto() : null;
        
    }*/

    public ClientSocket getClientSocket() {
        return clientSocket;
    }

    public String getNomeGiocatore() {
        return nomeGiocatore;
    }

    public void setNomeGiocatore(String nomeGiocatore) {
        this.nomeGiocatore = nomeGiocatore;
    }
}
