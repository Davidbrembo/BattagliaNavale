package client.main;

import client.view.SchermataInizialeView;
import utility.AudioFileCreator; // AGGIUNGI QUESTO
import javafx.application.Application;

public class ClientMain {
    public static void main(String[] args) {
        AudioFileCreator.inizializza(); // AGGIUNGI QUESTA RIGA
        Application.launch(SchermataInizialeView.class, args);   
    }
}