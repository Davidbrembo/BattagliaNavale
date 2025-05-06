package server.model;

import java.util.ArrayList;
import java.util.List;

import shared.model.Posizione;

public class GrigliaServer {
    private CellaServer[][] griglia;
    private List<NaveServer> navi;

    public GrigliaServer(int righe, int colonne) {
        griglia = new CellaServer[righe][colonne];
        navi = new ArrayList<>();
        for (int i = 0; i < righe; i++) {
            for (int j = 0; j < colonne; j++) {
                griglia[i][j] = new CellaServer();
            }
        }
    }

    public CellaServer[][] getGriglia() {
        return griglia;
    }

    public void aggiungiNave(NaveServer nave) {
        for (Posizione p : nave.getPosizioni()) {
            griglia[p.getRiga()][p.getColonna()].posizionaNave();
        }
        navi.add(nave);
    }

    public boolean riceviAttacco(Posizione p) {
        CellaServer cella = griglia[p.getRiga()][p.getColonna()];
        cella.colpisci();
        return cella.haNave();
    }

    public boolean naveAffondata(Posizione p) {
        for (NaveServer nave : navi) {
            if (nave.contiene(p)) {
                nave.colpisci(p);
                return nave.affondata();
            }
        }
        return false;
    }
}
