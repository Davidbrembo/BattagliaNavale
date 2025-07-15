package shared.model;

import java.io.Serializable;

public class Posizione implements Serializable {
    
	private static final long serialVersionUID = 1L;
	private int riga;
    private int colonna;

    public Posizione(int riga, int colonna) {
        this.riga = riga;
        this.colonna = colonna;
    }

    public int getRiga() {
        return riga;
    }

    public int getColonna() {
        return colonna;
    }

    @Override
    public String toString() {
        return "(" + riga + ", " + colonna + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Posizione)) return false;
        Posizione other = (Posizione) obj;
        return riga == other.riga && colonna == other.colonna;
    }

    @Override
    public int hashCode() {
        return 31 * riga + colonna;
    }
}
