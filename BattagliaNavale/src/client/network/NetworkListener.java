package client.network;

import shared.protocol.Messaggio;
import shared.protocol.Comando;
import shared.model.Coordinate;
import client.controller.GiocoController;

public class NetworkListener {

    private GiocoController giocoController;

    public NetworkListener(GiocoController giocoController) {
        this.giocoController = giocoController;
    }

    public void gestisciMessaggio(Messaggio msg) {
        if (msg.getComando() == Comando.RISPOSTA) {
            Coordinate coord = (Coordinate) msg.getPayload(); // Assumiamo payload = Coordinate + risultato
            String esito = msg.getExtra(); // Supponendo che tu abbia un getExtra() o simile

            //giocoController.gestisciEsitoMossa(coord, esito);
        }

        // Puoi aggiungere altri comandi:
        // else if (msg.getComando() == Comando.INIZIA_PARTITA) { ... }
    }
}
