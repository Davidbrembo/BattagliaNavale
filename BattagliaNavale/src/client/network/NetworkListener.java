package client.network;

import shared.protocol.Messaggio;
import shared.protocol.Comando;

public class NetworkListener {

    public void gestisciMessaggio(Messaggio msg) {
        if (msg.getComando() == Comando.RISPOSTA) {


        }
    }
}