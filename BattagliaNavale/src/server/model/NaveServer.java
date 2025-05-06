package server.model;

import shared.model.Posizione;
import java.util.ArrayList;
import java.util.List;

public class NaveServer {
    private List<Posizione> posizioni;
    private List<Posizione> colpite;

    public NaveServer(List<Posizione> posizioni) {
        this.posizioni = posizioni;
        this.colpite = new ArrayList<>();
    }

    public boolean contiene(Posizione p) {
        return posizioni.contains(p);
    }

    public void colpisci(Posizione p) {
        if (contiene(p)) colpite.add(p);
    }

    public boolean affondata() {
        return colpite.containsAll(posizioni);
    }
    
    public List<Posizione> getPosizioni() {
		return posizioni;
	}
}