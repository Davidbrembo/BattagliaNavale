package utility;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Utility per creare file audio sintetici semplici per gli effetti di gioco
 */
public class AudioFileCreator {
    
    private static final int SAMPLE_RATE = 44100;
    private static final String AUDIO_PATH = "resources/";
    
    public static void inizializza() {
        File audioDir = new File(AUDIO_PATH);
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        
        LogUtility.info("[AUDIO_CREATOR] Creando file audio mancanti...");
        
        // Crea i file audio se non esistono
        creaSeNonEsiste("hit.wav", () -> creaSuonoColpo());
        creaSeNonEsiste("miss.wav", () -> creaSuonoMancato());
        creaSeNonEsiste("sunk.wav", () -> creaSuonoNaveAffondata());
        creaSeNonEsiste("victory.wav", () -> creaSuonoVittoria());
        creaSeNonEsiste("defeat.wav", () -> creaSuonoSconfitta());
        creaSeNonEsiste("place_ship.wav", () -> creaSuonoPosizionamento());
        creaSeNonEsiste("notification.wav", () -> creaSuonoNotifica());
        creaSeNonEsiste("button_click.wav", () -> creaSuonoClick());
        creaSeNonEsiste("turn_start.wav", () -> creaSuonoTurno());
        creaSeNonEsiste("chat_message.wav", () -> creaSuonoChat());
        
        LogUtility.info("[AUDIO_CREATOR] File audio creati!");
    }
    
    private static void creaSeNonEsiste(String filename, Runnable creator) {
        File file = new File(AUDIO_PATH + filename);
        if (!file.exists()) {
            try {
                creator.run();
                LogUtility.info("[AUDIO_CREATOR] Creato: " + filename);
            } catch (Exception e) {
                LogUtility.warning("[AUDIO_CREATOR] Errore nella creazione di " + filename + ": " + e.getMessage());
            }
        }
    }
    
    // Suoni sintetici più dolci e meno acuti
    private static void creaSuonoColpo() {
        byte[] audioData = generaTono(300, 0.15, 0.3); // Tono medio-grave, breve, volume basso
        salvaAudio("hit.wav", audioData);
    }
    
    private static void creaSuonoMancato() {
        byte[] audioData = generaTono(150, 0.2, 0.2); // Tono grave, più lungo, volume molto basso
        salvaAudio("miss.wav", audioData);
    }
    
    private static void creaSuonoNaveAffondata() {
        // Sequenza di toni discendenti più dolci
        byte[] parte1 = generaTono(250, 0.15, 0.3);
        byte[] parte2 = generaTono(200, 0.15, 0.25);
        byte[] parte3 = generaTono(150, 0.3, 0.2);
        byte[] audioData = combina(combina(parte1, parte2), parte3);
        salvaAudio("sunk.wav", audioData);
    }
    
    private static void creaSuonoVittoria() {
        // Sequenza ascendente più dolce per vittoria
        byte[] parte1 = generaTono(262, 0.2, 0.25); // Do più grave
        byte[] parte2 = generaTono(330, 0.2, 0.3);  // Mi più grave
        byte[] parte3 = generaTono(392, 0.4, 0.35); // Sol più grave
        byte[] audioData = combina(combina(parte1, parte2), parte3);
        salvaAudio("victory.wav", audioData);
    }
    
    private static void creaSuonoSconfitta() {
        // Sequenza discendente più dolce per sconfitta
        byte[] parte1 = generaTono(220, 0.3, 0.25);
        byte[] parte2 = generaTono(180, 0.3, 0.2);
        byte[] parte3 = generaTono(150, 0.5, 0.15);
        byte[] audioData = combina(combina(parte1, parte2), parte3);
        salvaAudio("defeat.wav", audioData);
    }
    
    private static void creaSuonoPosizionamento() {
        byte[] audioData = generaTono(220, 0.08, 0.2); // La più grave e più breve
        salvaAudio("place_ship.wav", audioData);
    }
    
    private static void creaSuonoNotifica() {
        // Due toni rapidi più dolci
        byte[] parte1 = generaTono(350, 0.08, 0.25);
        byte[] pausa = new byte[SAMPLE_RATE / 25]; // Pausa di 40ms
        byte[] parte2 = generaTono(350, 0.08, 0.25);
        byte[] audioData = combina(combina(parte1, pausa), parte2);
        salvaAudio("notification.wav", audioData);
    }
    
    private static void creaSuonoClick() {
        byte[] audioData = generaTono(400, 0.03, 0.15); // Click molto breve e dolce
        salvaAudio("button_click.wav", audioData);
    }
    
    private static void creaSuonoTurno() {
        byte[] audioData = generaTono(280, 0.12, 0.25); // Tono medio-grave
        salvaAudio("turn_start.wav", audioData);
    }
    
    private static void creaSuonoChat() {
        byte[] audioData = generaTono(320, 0.06, 0.2); // Tono più delicato
        salvaAudio("chat_message.wav", audioData);
    }
    
    // Utility per generare toni
    private static byte[] generaTono(double frequenza, double durata, double volume) {
        int numSamples = (int) (SAMPLE_RATE * durata);
        byte[] audioData = new byte[numSamples * 2]; // 16-bit samples
        
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) SAMPLE_RATE;
            double amplitude = volume * 32767; // Massima ampiezza per 16-bit
            
            // Genera onda sinusoidale con envelope per evitare click
            double envelope = 1.0;
            if (i < numSamples * 0.1) { // Fade in
                envelope = i / (numSamples * 0.1);
            } else if (i > numSamples * 0.9) { // Fade out
                envelope = (numSamples - i) / (numSamples * 0.1);
            }
            
            short sample = (short) (amplitude * envelope * Math.sin(2 * Math.PI * frequenza * time));
            
            // Little-endian 16-bit
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return audioData;
    }
    
    private static byte[] combina(byte[] audio1, byte[] audio2) {
        byte[] combined = new byte[audio1.length + audio2.length];
        System.arraycopy(audio1, 0, combined, 0, audio1.length);
        System.arraycopy(audio2, 0, combined, audio1.length, audio2.length);
        return combined;
    }
    
    private static void salvaAudio(String filename, byte[] audioData) {
        try {
            File file = new File(AUDIO_PATH + filename);
            
            AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                SAMPLE_RATE,
                16, // 16-bit
                1,  // Mono
                2,  // Frame size
                SAMPLE_RATE,
                false // Little-endian
            );
            
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());
            
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, file);
            audioStream.close();
            
        } catch (IOException e) {
            LogUtility.error("[AUDIO_CREATOR] Errore nel salvare " + filename + ": " + e.getMessage());
        }
    }
}