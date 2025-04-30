package client.view;

import client.controller.GiocoController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GiocoView extends Application {

	@Override
	public void start(Stage primaryStage) {
	    VBox root = new VBox(20);
	    root.setStyle("-fx-background-color: #1b1b1b; -fx-padding: 50px;");

	    String nome = GiocoController.getInstance().getNomeGiocatore();
	    Label nomeLabel = new Label("Giocatore: " + nome);
	    nomeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

	    root.getChildren().add(nomeLabel);

	    Scene scene = new Scene(root, 800, 600);
	    primaryStage.setScene(scene);
	    primaryStage.setTitle("Gioco - Battaglia Navale");
	    
	    // Risolve il problema della posizione e rende la finestra spostabile e ridimensionabile
	    primaryStage.setFullScreen(false);
	    primaryStage.setResizable(true);
	    primaryStage.centerOnScreen(); // opzionale: centra la finestra

	    primaryStage.show();
	}

}
