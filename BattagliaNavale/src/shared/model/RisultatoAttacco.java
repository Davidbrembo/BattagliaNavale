package shared.model;

import java.io.Serializable;

public class RisultatoAttacco implements Serializable {
    private Posizione posizione;
    private boolean colpito;
    private boolean naveAffondata;

    public RisultatoAttacco(Posizione posizione, boolean colpito, boolean naveAffondata) {
        this.posizione = posizione;
        this.colpito = colpito;
        this.naveAffondata = naveAffondata;
    }

    public RisultatoAttacco(boolean b, Posizione posizione2) {
		// TODO Auto-generated constructor stub
	}

	public Posizione getPosizione() { return posizione; }
    public boolean isColpito() { return colpito; }
    public boolean isNaveAffondata() { return naveAffondata; }
}