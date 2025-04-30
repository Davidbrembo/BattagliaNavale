package shared.protocol;

import java.io.Serializable;

public class Messaggio implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8349979309953580592L;
	private Comando comando;
    private Object payload;    // es. Coordinate
    private String extra;      // es. "Colpito", "Mancato", etc.

    public Messaggio(Comando comando, Object payload, String extra) {
        this.comando = comando;
        this.payload = payload;
        this.extra = extra;
    }

    public Comando getComando() {
        return comando;
    }

    public Object getPayload() {
        return payload;
    }

    public String getExtra() {
        return extra;
    }
}