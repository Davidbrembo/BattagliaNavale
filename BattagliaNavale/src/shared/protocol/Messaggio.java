package shared.protocol;

import java.io.Serializable;

public class Messaggio implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Comando comando;
    private Object contenuto;

    public Messaggio(Comando comando, Object contenuto) {
        this.comando = comando;
        this.contenuto = contenuto;
    }

    public Comando getComando() {
        return comando;
    }

    public Object getContenuto() {
        return contenuto;
    }
    
    @Override
    public String toString() {
		return "Messaggio{" +
				"comando=" + comando +
				", contenuto=" + contenuto +
				'}';
	}
}
