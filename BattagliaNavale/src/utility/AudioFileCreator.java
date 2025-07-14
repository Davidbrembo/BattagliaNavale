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
    
    // Suoni sintetici ancora più dolci
    private static void creaSuonoColpo() {
        byte[] audioData = generaTono(280, 0.12, 0.2); // Volume ridotto da 0.3 a 0.2
        salvaAudio("hit.wav", audioData);
    }
    
    private static void creaSuonoMancato() {
        byte[] audioData = generaTono(140, 0.15, 0.15); // Volume ridotto da 0.2 a 0.15
        salvaAudio("miss.wav", audioData);
    }
    
    private static void creaSuonoNaveAffondata() {
        // Sequenza più dolce con volumi ridotti
        byte[] parte1 = generaTono(220, 0.12, 0.2);  // Ridotto
        byte[] parte2 = generaTono(180, 0.12, 0.18); // Ridotto
        byte[] parte3 = generaTono(140, 0.25, 0.15); // Ridotto
        byte[] audioData = combina(combina(parte1, parte2), parte3);
        salvaAudio("sunk.wav", audioData);
    }
    
    private static void creaSuonoVittoria() {
        // Sequenza più dolce per vittoria
        byte[] parte1 = generaTono(220, 0.15, 0.18); // Ridotto
        byte[] parte2 = generaTono(277, 0.15, 0.2);  // Ridotto
        byte[] parte3 = generaTono(330, 0.3, 0.22);  // Ridotto
        byte[] audioData = combina(combina(parte1, parte2), parte3);
        salvaAudio("victory.wav", audioData);
    }
    
    private static void creaSuonoSconfitta() {
        // Sequenza ancora più dolce per sconfitta
        byte[] parte1 = generaTono(200, 0.25, 0.18);
        byte[] parte2 = generaTono(160, 0.25, 0.15);
        byte[] parte3 = generaTono(130, 0.4, 0.12);
        byte[] audioData = combina(combina(parte1, parte2), parte3);
        salvaAudio("defeat.wav", audioData);
    }
    
    private static void creaSuonoPosizionamento() {
        byte[] audioData = generaTono(200, 0.06, 0.15); // Molto ridotto
        salvaAudio("place_ship.wav", audioData);
    }
    
    private static void creaSuonoNotifica() {
        // Due toni ancora più dolci
        byte[] parte1 = generaTono(300, 0.06, 0.18);
        byte[] parte2 = generaTono(300, 0.06, 0.18);
        byte[] audioData = combina(parte1, parte2);
        salvaAudio("notification.wav", audioData);
    }
    
    private static void creaSuonoClick() {
        byte[] audioData = generaTono(350, 0.025, 0.12); // Molto breve e dolce
        salvaAudio("button_click.wav", audioData);
    }
    
    private static void creaSuonoTurno() {
        byte[] audioData = generaTono(250, 0.1, 0.18); // Ridotto
        salvaAudio("turn_start.wav", audioData);
    }
    
    private static void creaSuonoChat() {
        byte[] audioData = generaTono(280, 0.05, 0.15); // Molto delicato
        salvaAudio("chat_message.wav", audioData);
    }
    
    // Utility per generare toni con fade migliore
    private static byte[] generaTono(double frequenza, double durata, double volume) {
        int numSamples = (int) (SAMPLE_RATE * durata);
        byte[] audioData = new byte[numSamples * 2]; // 16-bit samples
        
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) SAMPLE_RATE;
            double amplitude = volume * 16383; // Ridotta ampiezza massima per evitare distorsioni
            
            // Envelope più dolce con fade più lunghi
            double envelope = 1.0;
            double fadeInSamples = numSamples * 0.3;  // 30% per fade-in (prima era 10%)
            double fadeOutSamples = numSamples * 0.3; // 30% per fade-out (prima era 10%)
            
            if (i < fadeInSamples) { 
                // Fade in con curva più dolce (coseno invece di lineare)
                envelope = 0.5 * (1 - Math.cos(Math.PI * i / fadeInSamples));
            } else if (i > numSamples - fadeOutSamples) { 
                // Fade out con curva più dolce
                double fadePosition = (numSamples - i) / fadeOutSamples;
                envelope = 0.5 * (1 - Math.cos(Math.PI * fadePosition));
            }
            
            // Genera onda sinusoidale più dolce
            short sample = (short) (amplitude * envelope * Math.sin(2 * Math.PI * frequenza * time));
            
            // Limita il segnale per evitare clipping
            sample = (short) Math.max(-16383, Math.min(16383, sample));
            
            // Little-endian 16-bit
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return audioData;
    }
    
    private static byte[] combina(byte[] audio1, byte[] audio2) {
        // Aggiungi una piccola pausa tra i suoni per evitare click
        int pauseSamples = SAMPLE_RATE / 50; // 20ms di pausa
        byte[] pausa = new byte[pauseSamples * 2]; // Silenzio
        
        byte[] combined = new byte[audio1.length + pausa.length + audio2.length];
        System.arraycopy(audio1, 0, combined, 0, audio1.length);
        System.arraycopy(pausa, 0, combined, audio1.length, pausa.length);
        System.arraycopy(audio2, 0, combined, audio1.length + pausa.length, audio2.length);
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