package utility;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ImpostazioniManager {

    private static final String FILE_PATH = "resources/settings.json";

    public static void salvaImpostazioni(Impostazioni impostazioni) {
        try {
            // Creiamo un oggetto JSONObject con i dati delle impostazioni
            JSONObject json = new JSONObject();
            json.put("volume", impostazioni.getVolume());
            json.put("luminosita", impostazioni.getLuminosita());

            // Scriviamo il JSON nel file
            FileWriter fileWriter = new FileWriter(FILE_PATH);
            fileWriter.write(json.toString(4));  // Formatta con un'indentazione di 4 spazi
            fileWriter.close();

            System.out.println("Impostazioni salvate: " + json.toString(4)); // Debug
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠️ Errore nel salvataggio delle impostazioni.");
        }
    }

    public static Impostazioni caricaImpostazioni() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                // Leggi il file JSON e carica le impostazioni
                String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                JSONObject json = new JSONObject(content);

                double volume = json.getDouble("volume");
                double luminosita = json.getDouble("luminosita");

                return new Impostazioni(volume, luminosita);
            } else {
                // Se il file non esiste, ritorna null
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠️ Errore nel caricamento delle impostazioni.");
            return null;
        }
    }
}
