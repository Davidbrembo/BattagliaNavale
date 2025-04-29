package client.main;

import client.controller.GiocoController;
import client.view.SchermataInizialeView;
import javafx.application.Application;

public class ClientMain {
    public static void main(String[] args) {
        GiocoController controller = new GiocoController();
        controller.iniziaConnessione();  // ← Deve essere presente
    }
}
