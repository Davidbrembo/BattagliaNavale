package client.view;

import client.controller.GiocoController;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import shared.model.Posizione;
import server.model.ServerGameManager;
import shared.protocol.Comando;
import shared.protocol.Messaggio;

//GrigliaView.java
public class GrigliaView {

 private ServerGameManager gameManager;
 private GiocoController giocoController;

 public GrigliaView(ServerGameManager gameManager) {
     this.gameManager = gameManager;
     this.giocoController = GiocoController.getInstance();  // Assicurati di avere accesso al controller
 }

 public Scene creaScena(Stage primaryStage) {
     // Crea un GridPane per la griglia di gioco
     GridPane grid = new GridPane();
     grid.setHgap(2);
     grid.setVgap(2);
     grid.setAlignment(Pos.CENTER);

     int righe = gameManager.getGriglie()[0].getGriglia().length;
     int colonne = gameManager.getGriglie()[0].getGriglia()[0].length;

     // Aggiungi le celle alla griglia
     for (int i = 0; i < righe; i++) {
         for (int j = 0; j < colonne; j++) {
             // Crea una cella della griglia
             Rectangle cella = new Rectangle(30, 30);
             cella.setFill(Color.LIGHTBLUE);
             cella.setStroke(Color.BLACK);

             // Aggiungi evento di clic sulla cella
             Posizione posizione = new Posizione(i, j);
             cella.setOnMouseClicked(new EventHandler<MouseEvent>() {
                 @Override
                 public void handle(MouseEvent event) {
                     // Quando l'utente clicca, invia l'attacco al server
                     inviaAttaccoAlServer(posizione);
                 }
             });

             // Aggiungi la cella alla griglia
             grid.add(cella, j, i);
         }
     }

     // Creazione della scena
     Scene scena = new Scene(grid, 800, 600);
     return scena;
 }

 private void inviaAttaccoAlServer(Posizione posizione) {
     // Crea il messaggio con la posizione dell'attacco
     Messaggio messaggio = new Messaggio(Comando.ATTACCA, posizione);

     // Invia il messaggio al server
     giocoController.inviaMessaggio(messaggio);
     
     // Gestisci la risposta del server (aggiornamento della griglia)
     /*RisultatoAttacco risultato = giocoController.getRisultatoAttacco();  // Puoi avere un metodo per ottenere il risultato
     aggiornaCella(posizione, risultato);*/
 }

}
