package client.main;

import client.view.SchermataInizialeView;
import utility.AudioFileCreator;
import javafx.application.Application;

public class ClientMain {
    public static void main(String[] args) {
        AudioFileCreator.inizializza();
        Application.launch(SchermataInizialeView.class, args);   
    }
}