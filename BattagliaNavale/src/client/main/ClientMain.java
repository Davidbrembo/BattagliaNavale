package client.main;

import client.controller.GiocoController;
import client.view.SchermataInizialeView;
import javafx.application.Application;

public class ClientMain {
    public static void main(String[] args) {
        GiocoController controller = new GiocoController();
        if(controller.iniziaConnessione()) {
        	Application.launch(SchermataInizialeView.class, args);
        }
        else {
			System.out.println("Connessione fallita");
		}
        
    }
}
