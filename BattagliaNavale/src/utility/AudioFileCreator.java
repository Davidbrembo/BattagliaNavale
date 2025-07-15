package utility;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Utility per creare file audio sintetici più puliti
 */
public class AudioFileCreator {
    
    private static final int SAMPLE_RATE = 22050; // Ridotto per file più piccoli
    private static final String AUDIO_PATH = "resources/";
    
    public static void inizializza() {
        File audioDir = new File(AUDIO_PATH);
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        
        LogUtility.info("[AUDIO_CREATOR] Creando file audio...");
        
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
    
    // Suoni molto più puliti e corti
    private static void creaSuonoColpo() {
        byte[] audioData = generaBeep(300, 0.15, 0.3); // Frequenza, durata, volume
        salvaAudio("hit.wav", audioData);
    }
    
    private static void creaSuonoMancato() {
        byte[] audioData = generaBeep(150, 0.2, 0.2);
        salvaAudio("miss.wav", audioData);
    }
    
    private static void creaSuonoNaveAffondata() {
        // Tre beep in sequenza
        byte[] beep1 = generaBeep(250, 0.1, 0.25);
        byte[] beep2 = generaBeep(200, 0.1, 0.25);
        byte[] beep3 = generaBeep(150, 0.2, 0.25);
        byte[] audioData = combina(combina(beep1, beep2), beep3);
        salvaAudio("sunk.wav", audioData);
    }
    
    private static void creaSuonoVittoria() {
        // Melodia ascendente
        byte[] nota1 = generaBeep(262, 0.15, 0.3); // Do
        byte[] nota2 = generaBeep(330, 0.15, 0.3); // Mi
        byte[] nota3 = generaBeep(392, 0.3, 0.3);  // Sol lungo
        byte[] audioData = combina(combina(nota1, nota2), nota3);
        salvaAudio("victory.wav", audioData);
    }
    
    private static void creaSuonoSconfitta() {
        // Melodia discendente
        byte[] nota1 = generaBeep(220, 0.2, 0.25);
        byte[] nota2 = generaBeep(175, 0.2, 0.25);
        byte[] nota3 = generaBeep(147, 0.4, 0.25);
        byte[] audioData = combina(combina(nota1, nota2), nota3);
        salvaAudio("defeat.wav", audioData);
    }
    
    private static void creaSuonoPosizionamento() {
        byte[] audioData = generaBeep(220, 0.08, 0.2);
        salvaAudio("place_ship.wav", audioData);
    }
    
    private static void creaSuonoNotifica() {
        byte[] beep1 = generaBeep(330, 0.06, 0.2);
        byte[] beep2 = generaBeep(330, 0.06, 0.2);
        byte[] audioData = combina(beep1, beep2);
        salvaAudio("notification.wav", audioData);
    }
    
    private static void creaSuonoClick() {
        byte[] audioData = generaBeep(400, 0.03, 0.15); // Molto breve
        salvaAudio("button_click.wav", audioData);
    }
    
    private static void creaSuonoTurno() {
        byte[] audioData = generaBeep(280, 0.12, 0.2);
        salvaAudio("turn_start.wav", audioData);
    }
    
    private static void creaSuonoChat() {
        byte[] audioData = generaBeep(350, 0.05, 0.15);
        salvaAudio("chat_message.wav", audioData);
    }
    
    // Generatore di beep semplice e pulito
    private static byte[] generaBeep(double frequenza, double durata, double volume) {
        int numSamples = (int) (SAMPLE_RATE * durata);
        byte[] audioData = new byte[numSamples * 2]; // 16-bit samples
        
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) SAMPLE_RATE;
            double amplitude = volume * 8000; // Ampiezza ridotta per evitare distorsioni
            
            // Envelope semplice per evitare click
            double envelope = 1.0;
            int fadeLength = numSamples / 10; // 10% fade in/out
            
            if (i < fadeLength) {
                envelope = (double) i / fadeLength; // Fade in lineare
            } else if (i > numSamples - fadeLength) {
                envelope = (double) (numSamples - i) / fadeLength; // Fade out lineare
            }
            
            // Genera onda sinusoidale pulita
            short sample = (short) (amplitude * envelope * Math.sin(2 * Math.PI * frequenza * time));
            
            // Converti in bytes (little-endian)
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return audioData;
    }
    
    private static byte[] combina(byte[] audio1, byte[] audio2) {
        // Pausa piccola tra i suoni
        int pauseSamples = SAMPLE_RATE / 100; // 10ms di pausa
        byte[] pausa = new byte[pauseSamples * 2];
        
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