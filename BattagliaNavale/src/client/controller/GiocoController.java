package client.controller;

import client.network.ClientSocket;
import shared.model.RisultatoAttacco;
import shared.protocol.Messaggio;

import java.io.IOException;
import java.io.ObjectInputStream;

public class GiocoController {

    private static GiocoController instance;
    private final ClientSocket clientSocket;
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
            return true;
        } catch (IOException e) {
            System.out.println("[CLIENT] Errore di connessione: " + e.getMessage());
            return false;
        }
    }

    public void inviaMessaggio(Messaggio messaggio) {
        clientSocket.inviaMessaggio(messaggio); // IOException non serve più catcharlo
    }

    public RisultatoAttacco getRisultatoAttacco() {
        try {
            // Ora ClientSocket gestisce correttamente l'input stream
            ObjectInputStream inputStream = clientSocket.getInputStream();
            return (RisultatoAttacco) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

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
