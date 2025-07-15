package server.model;

import shared.model.Posizione;
import shared.model.RisultatoAttacco;

public class ServerGameManager {
    private GrigliaServer[] griglie = new GrigliaServer[2];
    private int turno = 0;
    
    public ServerGameManager() {
    }

    public ServerGameManager(int righe, int colonne) {
        griglie[0] = new GrigliaServer(righe, colonne);
        griglie[1] = new GrigliaServer(righe, colonne);
    }

    public RisultatoAttacco attacca(Posizione p) {
        int difensore = 1 - turno;
        boolean colpito = griglie[difensore].riceviAttacco(p);
        boolean affondata = griglie[difensore].naveAffondata(p);
        return new RisultatoAttacco(p, colpito, affondata);
    }

    public void passaTurno() {
        turno = 1 - turno;
    }

    public int getTurno() {
        return turno;
    }

    public GrigliaServer[] getGriglie() {
        return griglie;
    }
}
