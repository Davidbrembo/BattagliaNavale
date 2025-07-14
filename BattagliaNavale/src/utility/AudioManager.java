package utility;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestisce tutti i suoni del gioco - ambiente, notifiche ed effetti
 */
public class AudioManager {
    
    private static AudioManager instance;
    private Map<String, MediaPlayer> players = new HashMap<>();
    private MediaPlayer ambientPlayer; // Suoni di ambiente continui
    private boolean audioEnabled = true;
    private double masterVolume = 0.5;
    
    // Percorsi dei file audio (usa file generati programmaticamente se non esistono)
    private static final String AUDIO_PATH = "resources/audio/";
    
    private AudioManager() {
        inizializzaAudio();
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    private void inizializzaAudio() {
        try {
            // Crea directory audio se non esiste
            File audioDir = new File(AUDIO_PATH);
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }
            
            // Inizializza i suoni (alcuni sono generati programmaticamente)
            inizializzaSuoni();
            
            LogUtility.info("[AUDIO] AudioManager inizializzato");
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Errore inizializzazione audio: " + e.getMessage());
            audioEnabled = false;
        }
    }
    
    private void inizializzaSuoni() {
        // Prova a caricare file audio esistenti, altrimenti usa suoni generati
        try {
            // Suoni ambiente (onde del mare)
            caricaSuono("waves", "waves.mp3", true);
            
            // Effetti di gioco
            caricaSuono("hit", "hit.mp3", false);
            caricaSuono("miss", "miss.mp3", false);
            caricaSuono("sunk", "sunk.mp3", false);
            caricaSuono("victory", "victory.mp3", false);
            caricaSuono("defeat", "defeat.mp3", false);
            caricaSuono("place_ship", "place_ship.mp3", false);
            caricaSuono("notification", "notification.mp3", false);
            
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Alcuni file audio non trovati, usando suoni di fallback");
        }
    }
    
    private void caricaSuono(String nome, String filename, boolean isAmbient) {
        try {
            File audioFile = new File(AUDIO_PATH + filename);
            
            if (audioFile.exists()) {
                Media media = new Media(audioFile.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(masterVolume);
                
                if (isAmbient) {
                    player.setCycleCount(MediaPlayer.INDEFINITE);
                    ambientPlayer = player;
                }
                
                players.put(nome, player);
                LogUtility.info("[AUDIO] Caricato: " + filename);
            }
            
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Errore nel caricamento del suono '" + filename + "': " + e.getMessage());
        }
    }
}