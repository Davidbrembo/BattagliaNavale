package client.view.components;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import shared.model.TipoNave;

public class NaveGraphics extends Group {
    
    private final TipoNave tipo;
    private final double cellSize;
    
    public NaveGraphics(TipoNave tipo, boolean orizzontale, double cellSize) {
        this.tipo = tipo;
        this.cellSize = cellSize;
        
        creaNave();
    }
    
    private void creaNave() {
        double margine = 2;
        double larghezza = cellSize - margine * 2;
        double altezza = cellSize - margine * 2;
        
        Color coloreBase = getColoreBase();
        Color coloreScuro = coloreBase.deriveColor(0, 1, 0.7, 1);
        
        Rectangle corpo = new Rectangle(larghezza, altezza);
        corpo.setFill(coloreBase);
        corpo.setStroke(coloreScuro);
        corpo.setStrokeWidth(1);
        corpo.setArcWidth(4);
        corpo.setArcHeight(4);
        
        getChildren().add(corpo);
        
        aggiungiDettagli(larghezza, altezza, coloreBase, coloreScuro);
    }
    
    private void aggiungiDettagli(double larghezza, double altezza, Color base, Color scuro) {
        switch (tipo) {
            case PORTAEREI -> {
                Rectangle ponte = new Rectangle(larghezza * 0.3, altezza * 0.6);
                ponte.setFill(base.brighter());
                ponte.setStroke(scuro);
                ponte.setX(larghezza * 0.35);
                ponte.setY(altezza * 0.2);
                getChildren().add(ponte);
            }
            case INCROCIATORE -> {
                Circle torretta = new Circle(Math.min(larghezza, altezza) * 0.15);
                torretta.setFill(scuro);
                torretta.setCenterX(larghezza / 2);
                torretta.setCenterY(altezza / 2);
                getChildren().add(torretta);
            }
            case CACCIATORPEDINIERE -> {
                Rectangle ponte = new Rectangle(larghezza * 0.2, altezza * 0.4);
                ponte.setFill(base.brighter());
                ponte.setX(larghezza * 0.4);
                ponte.setY(altezza * 0.3);
                getChildren().add(ponte);
            }
            case SOTTOMARINO -> {
                Rectangle torre = new Rectangle(larghezza * 0.3, altezza * 0.5);
                torre.setFill(base.brighter());
                torre.setX(larghezza * 0.35);
                torre.setY(altezza * 0.25);
                getChildren().add(torre);
            }
        }
    }
    
    private Color getColoreBase() {
        return switch (tipo) {
            case PORTAEREI -> Color.DARKRED;
            case INCROCIATORE -> Color.DARKBLUE;
            case CACCIATORPEDINIERE -> Color.DARKGREEN;
            case SOTTOMARINO -> Color.DARKORANGE;
        };
    }
    
    public void mostraColpita() {
        Line x1 = new Line(2, 2, cellSize - 4, cellSize - 4);
        Line x2 = new Line(cellSize - 4, 2, 2, cellSize - 4);
        x1.setStroke(Color.RED);
        x2.setStroke(Color.RED);
        x1.setStrokeWidth(2);
        x2.setStrokeWidth(2);
        getChildren().addAll(x1, x2);
    }
    
    public void mostraAffondata() {
        setOpacity(0.6);
        mostraColpita();
    }
}