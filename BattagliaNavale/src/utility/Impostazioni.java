package utility;

public class Impostazioni {

    private double volume;
    private double luminosita;

    // Costruttore con parametri
    public Impostazioni(double volume, double luminosita) {
        this.volume = volume;
        this.luminosita = luminosita;
    }

    // Costruttore di default con valori predefiniti
    public Impostazioni() {
        this.volume = 50.0;  // Impostazione predefinita per il volume
        this.luminosita = 50.0;  // Impostazione predefinita per la luminosità
    }

    // Getters e setters
    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getLuminosita() {
        return luminosita;
    }

    public void setLuminosita(double luminosita) {
        this.luminosita = luminosita;
    }


    // Metodo per visualizzare le impostazioni in modo leggibile
    @Override
    public String toString() {
        return "Impostazioni [volume=" + volume + ", luminosita=" + luminosita + "]";
    }
}
