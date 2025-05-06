package server.model;

public class CellaServer {
    private boolean haNave;
    private boolean colpita;

    public CellaServer() {
        this.haNave = false;
        this.colpita = false;
    }

    public boolean haNave() { return haNave; }
    public void posizionaNave() { this.haNave = true; }
    public boolean isColpita() { return colpita; }
    public void colpisci() { this.colpita = true; }
}
