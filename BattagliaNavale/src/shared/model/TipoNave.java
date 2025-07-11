package shared.model;

/**
 * Enum che rappresenta i diversi tipi di navi nella Battaglia Navale
 */
public enum TipoNave {
    PORTAEREI("🚢 Portaerei", 5, "La nave più grande della flotta"),
    INCROCIATORE("🛳️ Incrociatore", 4, "Nave da guerra pesante"),
    CACCIATORPEDINIERE("⚓ Cacciatorpediniere", 3, "Nave veloce e agile"),
    SOTTOMARINO("🤿 Sottomarino", 2, "Nave subacquea silenziosa");
    
    private final String nome;
    private final int lunghezza;
    private final String descrizione;
    
    TipoNave(String nome, int lunghezza, String descrizione) {
        this.nome = nome;
        this.lunghezza = lunghezza;
        this.descrizione = descrizione;
    }
    
    public String getNome() { return nome; }
    public int getLunghezza() { return lunghezza; }
    public String getDescrizione() { return descrizione; }
    
    /**
     * Restituisce la lista delle navi da posizionare per ogni giocatore
     */
    public static TipoNave[] getNaviDaPosizionare() {
        return new TipoNave[]{
            PORTAEREI,           // 1x Portaerei (5)
            INCROCIATORE,        // 1x Incrociatore (4)  
            CACCIATORPEDINIERE,  // 1x Cacciatorpediniere (3)
            CACCIATORPEDINIERE,  // 2x Cacciatorpediniere (3)
            SOTTOMARINO          // 1x Sottomarino (2)
        };
    }
    
    /**
     * Restituisce il numero totale di navi da posizionare
     */
    public static int getNumeroTotaleNavi() {
        return getNaviDaPosizionare().length;
    }
    
    /**
     * Restituisce il nome con lunghezza per la UI
     */
    public String getNomeConLunghezza() {
        return nome + " (" + lunghezza + " caselle)";
    }
}