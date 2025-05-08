package client.controller;

import client.network.ClientSocket;
import shared.protocol.Messaggio;
import utility.LogUtility;

import java.io.IOException;

public class GiocoController {

    private static GiocoController instance;
    public final ClientSocket clientSocket;
    private String nomeGiocatore;
    private boolean connesso;

    private GiocoController() {
        clientSocket = ClientSocket.getInstance();
        connesso = inizializzaConnessione(); // tentativo automatico alla creazione
    }

    public static GiocoController getInstance() {
        if (instance == null) {
            instance = new GiocoController();
        }
        return instance;
    }

    public boolean inizializzaConnessione() {
        try {
            clientSocket.connect("localhost", 12345);
            LogUtility.info("[CLIENT] Connessione avvenuta.");
            Messaggio messaggioDiBenvenuto = clientSocket.riceviMessaggio();
            LogUtility.info("[SERVER] " + messaggioDiBenvenuto);
            return true;
        } catch (IOException e) {
            LogUtility.error("[CLIENT] Errore di connessione: " + e.getMessage());
            return false;
        }
    }

    public void inviaMessaggio(Messaggio messaggio) {
        if (!connesso || clientSocket.getOutputStream() == null) {
            LogUtility.error("[CLIENT] Tentativo di invio messaggio senza connessione!");
            return;
        }
        clientSocket.inviaMessaggio(messaggio);
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

    public boolean isConnesso() {
        return connesso;
    }
}
