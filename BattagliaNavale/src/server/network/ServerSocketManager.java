package server.network;

import server.model.NaveServer;
import server.model.ServerGameManager;
import shared.model.Posizione;
import shared.model.RisultatoAttacco;
import shared.protocol.Comando;
import shared.protocol.Messaggio;
import utility.LogUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerSocketManager {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers = new ArrayList<>();
    private ServerGameManager gameManager;

    public ServerSocketManager(int porta) throws IOException {
        this.serverSocket = new ServerSocket(porta);
        this.gameManager = new ServerGameManager(10, 10); // Griglia 10x10
        inizializzaNavi(); // Aggiungi navi di esempio
        LogUtility.info("[SERVER] In ascolto sulla porta " + porta + ". Max connessioni: 2");
    }
    
    private void inizializzaNavi() {
        // Aggiungi alcune navi di esempio per entrambi i giocatori
        // Giocatore 0 (prima griglia)
        gameManager.getGriglie()[0].aggiungiNave(new NaveServer(Arrays.asList(
            new Posizione(1, 1), new Posizione(1, 2), new Posizione(1, 3)
        )));
        gameManager.getGriglie()[0].aggiungiNave(new NaveServer(Arrays.asList(
            new Posizione(3, 5), new Posizione(4, 5)
        )));
        
        // Giocatore 1 (seconda griglia)
        gameManager.getGriglie()[1].aggiungiNave(new NaveServer(Arrays.asList(
            new Posizione(2, 2), new Posizione(2, 3), new Posizione(2, 4)
        )));
        gameManager.getGriglie()[1].aggiungiNave(new NaveServer(Arrays.asList(
            new Posizione(6, 1), new Posizione(7, 1)
        )));
        
        LogUtility.info("[SERVER] Navi inizializzate per entrambi i giocatori");
    }

    public void start() {
        while (clientHandlers.size() < 2) {
            try {
                Socket client = serverSocket.accept();
                int playerID = clientHandlers.size(); // 0 o 1

                LogUtility.info("[SERVER] Nuova connessione da: " + client.getInetAddress());

                ClientHandler handler = new ClientHandler(client, playerID, this);
                clientHandlers.add(handler);
                new Thread(handler).start();

                LogUtility.info("[SERVER] Connessi: " + clientHandlers.size());

                // Se entrambi sono connessi, invia START a tutti
                if (clientHandlers.size() == 2) {
                    LogUtility.info("[SERVER] Entrambi i giocatori connessi. Inizio partita.");
                    Messaggio startMsg = new Messaggio(Comando.START, "Partita pronta!");
                    for (ClientHandler ch : clientHandlers) {
                        ch.inviaMessaggio(startMsg);
                    }
                    
                    // Informa il primo giocatore che è il suo turno
                    clientHandlers.get(0).inviaMessaggio(new Messaggio(Comando.TURNO, "È il tuo turno!"));
                    clientHandlers.get(1).inviaMessaggio(new Messaggio(Comando.STATO, "Aspetta il tuo turno"));
                }

            } catch (IOException e) {
                LogUtility.error("[SERVER] Errore I/O: " + e.getMessage());
            }
        }
    }
    
    // Gestisce un attacco da parte di un giocatore
    public synchronized void gestisciAttacco(int giocatoreID, Posizione posizione) {
        if (gameManager.getTurno() != giocatoreID) {
            // Non è il turno di questo giocatore
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.ERRORE, "Non è il tuo turno!")
            );
            return;
        }
        
        LogUtility.info("[SERVER] Giocatore " + giocatoreID + " attacca in " + posizione);
        
        // Esegui l'attacco
        RisultatoAttacco risultato = gameManager.attacca(posizione);
        
        // Invia il risultato a entrambi i giocatori
        Messaggio msgRisultato = new Messaggio(Comando.RISULTATO_ATTACCO, risultato);
        for (ClientHandler handler : clientHandlers) {
            handler.inviaMessaggio(msgRisultato);
        }
        
        // Se non ha colpito, passa il turno
        if (!risultato.isColpito()) {
            gameManager.passaTurno();
            int nuovoTurno = gameManager.getTurno();
            
            // Informa i giocatori del cambio turno
            clientHandlers.get(nuovoTurno).inviaMessaggio(
                new Messaggio(Comando.TURNO, "È il tuo turno!")
            );
            clientHandlers.get(1 - nuovoTurno).inviaMessaggio(
                new Messaggio(Comando.STATO, "Aspetta il tuo turno")
            );
            
            LogUtility.info("[SERVER] Turno passato al giocatore " + nuovoTurno);
        } else {
            // Ha colpito, continua il suo turno
            clientHandlers.get(giocatoreID).inviaMessaggio(
                new Messaggio(Comando.TURNO, "Hai colpito! Continua il tuo turno.")
            );
            LogUtility.info("[SERVER] Giocatore " + giocatoreID + " ha colpito, continua il turno");
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            LogUtility.error("[SERVER] Errore nella chiusura: " + e.getMessage());
        }
    }
}