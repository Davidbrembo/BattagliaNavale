package shared.protocol;

public enum Comando {
    POSIZIONA, // per il posizionamento navi
    MOSSA,     // quando un giocatore effettua una mossa
    RISPOSTA,  // per la risposta alla mossa (colpito, mancato, affondato)
    INIZIA_PARTITA,
    FINE_PARTITA,
    MESSAGGIO_CHAT,
    INVIA_NOME,
    DISCONNETTI
}