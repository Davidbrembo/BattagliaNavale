package utility;

public class Impostazioni {

    private double volume;
    private double luminosita;

    public Impostazioni(double volume, double luminosita) {
        this.volume = volume;
        this.luminosita = luminosita;
    }

    public Impostazioni() {
        this.volume = 50.0;
        this.luminosita = 50.0;
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

    @Override
    public String toString() {
        return "Impostazioni [volume=" + volume + ", luminosita=" + luminosita + "]";
    }
}
