package utility;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    
    private static AudioManager instance;
    private Map<String, MediaPlayer> players = new HashMap<>();
    private MediaPlayer ambientPlayer;
    private MediaPlayer musicPlayer;
    private boolean audioEnabled = true;
    private double masterVolume = 0.5;
    private double effectsVolume = 0.7;
    private double musicVolume = 0.3;
    
    private static final String AUDIO_PATH = "resources/";
    
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
            File audioDir = new File("resources");
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }
            
            inizializzaSuoni();
            
            LogUtility.info("[AUDIO] AudioManager inizializzato con " + players.size() + " suoni");
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Errore inizializzazione audio: " + e.getMessage());
            audioEnabled = false;
        }
    }
    
    private void inizializzaSuoni() {
        caricaMusica("audio_battaglia.mp3");
        
        caricaEffetto("hit", "Suono colpo riuscito");
        caricaEffetto("miss", "Suono colpo mancato"); 
        caricaEffetto("sunk", "Suono nave affondata");
        caricaEffetto("victory", "Suono vittoria");
        caricaEffetto("defeat", "Suono sconfitta");
        caricaEffetto("place_ship", "Suono posizionamento nave");
        caricaEffetto("notification", "Suono notifica");
        caricaEffetto("button_click", "Suono click bottone");
        caricaEffetto("turn_start", "Suono inizio turno");
        caricaEffetto("chat_message", "Suono messaggio chat");
        
        caricaAmbiente("waves", "Suono onde del mare");
    }
    
    private void caricaEffetto(String nome, String descrizione) {
        try {
            File audioFileWav = new File(AUDIO_PATH + nome + ".wav");
            File audioFileMp3 = new File(AUDIO_PATH + nome + ".mp3");
            
            File audioFile = null;
            if (audioFileWav.exists()) {
                audioFile = audioFileWav;
            } else if (audioFileMp3.exists()) {
                audioFile = audioFileMp3;
            }
            
            if (audioFile != null) {
                Media media = new Media(audioFile.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(effectsVolume);
                players.put(nome, player);
                LogUtility.info("[AUDIO] Effetto caricato: " + audioFile.getName());
            } else {
                LogUtility.warning("[AUDIO] File audio non trovato per: " + nome);
            }
            
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Errore caricamento effetto '" + nome + "': " + e.getMessage());
        }
    }
    
    private void caricaAmbiente(String nome, String descrizione) {
        try {
            File audioFileWav = new File(AUDIO_PATH + nome + ".wav");
            File audioFileMp3 = new File(AUDIO_PATH + nome + ".mp3");
            
            File audioFile = null;
            if (audioFileWav.exists()) {
                audioFile = audioFileWav;
            } else if (audioFileMp3.exists()) {
                audioFile = audioFileMp3;
            }
            
            if (audioFile != null) {
                Media media = new Media(audioFile.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setVolume(musicVolume * 0.6);
                player.setCycleCount(MediaPlayer.INDEFINITE);
                ambientPlayer = player;
                players.put(nome, player);
                LogUtility.info("[AUDIO] Ambiente caricato: " + audioFile.getName());
            }
            
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Errore caricamento ambiente: " + e.getMessage());
        }
    }
    
    private void caricaMusica(String filename) {
        try {
            File audioFile = new File(AUDIO_PATH + filename);
            
            if (audioFile.exists()) {
                Media media = new Media(audioFile.toURI().toString());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setVolume(musicVolume);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                LogUtility.info("[AUDIO] Musica caricata: " + filename);
            } else {
                LogUtility.info("[AUDIO] File musica non trovato: " + filename);
            }
            
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Errore caricamento musica: " + e.getMessage());
        }
    }
    
    public void riproduciEffetto(String nome) {
        if (!audioEnabled) return;
        
        MediaPlayer player = players.get(nome);
        if (player != null) {
            try {
                player.stop();
                player.seek(Duration.ZERO);
                player.play();
                LogUtility.info("[AUDIO] Riprodotto effetto: " + nome);
            } catch (Exception e) {
                LogUtility.warning("[AUDIO] Errore riproduzione effetto '" + nome + "': " + e.getMessage());
            }
        } else {
            LogUtility.warning("[AUDIO] Effetto non trovato: " + nome);
        }
    }
    
    public void riproduciColpo() {
        riproduciEffetto("hit");
    }
    
    public void riproduciMancato() {
        riproduciEffetto("miss");
    }
    
    public void riproduciNaveAffondata() {
        riproduciEffetto("sunk");
    }
    
    public void riproduciVittoria() {
        riproduciEffetto("victory");
    }
    
    public void riproduciSconfitta() {
        riproduciEffetto("defeat");
    }
    
    public void riproduciPosizionamentoNave() {
        riproduciEffetto("place_ship");
    }
    
    public void riproduciNotifica() {
        riproduciEffetto("notification");
    }
    
    public void riproduciClickBottone() {
        riproduciEffetto("button_click");
    }
    
    public void riproduciInizioTurno() {
        riproduciEffetto("turn_start");
    }
    
    public void riproduciMessaggioChat() {
        riproduciEffetto("chat_message");
    }
    
    public void avviaMusica() {
        if (!audioEnabled || musicPlayer == null) return;
        
        try {
            musicPlayer.play();
            LogUtility.info("[AUDIO] Musica di background avviata");
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Errore avvio musica: " + e.getMessage());
        }
    }
    
    public void fermaMusica() {
        if (musicPlayer != null) {
            try {
                musicPlayer.stop();
                LogUtility.info("[AUDIO] Musica di background fermata");
            } catch (Exception e) {
                LogUtility.warning("[AUDIO] Errore stop musica: " + e.getMessage());
            }
        }
    }
    
    public void pausaMusica() {
        if (musicPlayer != null) {
            try {
                musicPlayer.pause();
                LogUtility.info("[AUDIO] Musica di background in pausa");
            } catch (Exception e) {
                LogUtility.warning("[AUDIO] Errore pausa musica: " + e.getMessage());
            }
        }
    }
    
    public void avviaAmbiente() {
        if (!audioEnabled || ambientPlayer == null) return;
        
        try {
            ambientPlayer.play();
            LogUtility.info("[AUDIO] Suoni ambiente avviati");
        } catch (Exception e) {
            LogUtility.warning("[AUDIO] Errore avvio ambiente: " + e.getMessage());
        }
    }
    
    public void fermaAmbiente() {
        if (ambientPlayer != null) {
            try {
                ambientPlayer.stop();
                LogUtility.info("[AUDIO] Suoni ambiente fermati");
            } catch (Exception e) {
                LogUtility.warning("[AUDIO] Errore stop ambiente: " + e.getMessage());
            }
        }
    }
    
    public void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));
        
        if (musicPlayer != null) {
            musicPlayer.setVolume(this.masterVolume * musicVolume);
        }
        
        if (ambientPlayer != null) {
            ambientPlayer.setVolume(this.masterVolume * musicVolume * 0.6);
        }
        
        for (MediaPlayer player : players.values()) {
            if (player != musicPlayer && player != ambientPlayer) {
                player.setVolume(this.masterVolume * effectsVolume);
            }
        }
        
        LogUtility.info("[AUDIO] Volume master impostato a: " + (this.masterVolume * 100) + "%");
    }
    
    public void setEffectsVolume(double volume) {
        this.effectsVolume = Math.max(0, Math.min(1, volume));
        
        for (MediaPlayer player : players.values()) {
            if (player != musicPlayer && player != ambientPlayer) {
                player.setVolume(masterVolume * this.effectsVolume);
            }
        }
    }
    
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        
        if (musicPlayer != null) {
            musicPlayer.setVolume(masterVolume * this.musicVolume);
        }
        
        if (ambientPlayer != null) {
            ambientPlayer.setVolume(masterVolume * this.musicVolume * 0.6);
        }
    }
    
    public void abilitaAudio(boolean enabled) {
        this.audioEnabled = enabled;
        
        if (!enabled) {
            fermaMusica();
            fermaAmbiente();
            for (MediaPlayer player : players.values()) {
                try {
                    player.stop();
                } catch (Exception e) {
                	LogUtility.warning("[AUDIO] Errore stop player '" + player.getMedia().getSource() + "': " + e.getMessage());
                }
            }
        }
        
        LogUtility.info("[AUDIO] Audio " + (enabled ? "abilitato" : "disabilitato"));
    }
    
    public boolean isAudioEnabled() {
        return audioEnabled;
    }
    
    public double getMasterVolume() {
        return masterVolume;
    }
    
    public void cleanup() {
        LogUtility.info("[AUDIO] Cleanup AudioManager...");
        
        fermaMusica();
        fermaAmbiente();
        
        for (MediaPlayer player : players.values()) {
            try {
                player.stop();
                player.dispose();
            } catch (Exception e) {
            	LogUtility.warning("[AUDIO] Errore durante lo stop/disposal del player '" + player.getMedia().getSource() + "': " + e.getMessage());
            }
        }
        
        players.clear();
        musicPlayer = null;
        ambientPlayer = null;
        
        LogUtility.info("[AUDIO] Cleanup completato");
    }
}