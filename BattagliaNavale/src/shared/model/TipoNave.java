package shared.model;

public enum TipoNave {
    PORTAEREI("Portaerei", 5, "La nave più grande della flotta"),
    INCROCIATORE("Incrociatore", 4, "Nave da guerra pesante"),
    CACCIATORPEDINIERE("Cacciatorpediniere", 3, "Nave veloce e agile"),
    SOTTOMARINO("Sottomarino", 2, "Nave subacquea silenziosa");
    
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
    
    public static TipoNave[] getNaviDaPosizionare() {
        return new TipoNave[]{
            PORTAEREI,
            INCROCIATORE, 
            CACCIATORPEDINIERE,
            CACCIATORPEDINIERE,
            SOTTOMARINO
        };
    }
    
    public static int getNumeroTotaleNavi() {
        return getNaviDaPosizionare().length;
    }
    
    public String getNomeConLunghezza() {
        return nome + " (" + lunghezza + " caselle)";
    }
}