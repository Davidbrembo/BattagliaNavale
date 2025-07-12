package client.view.components;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import shared.model.TipoNave;

/**
 * Componente grafico per rappresentare le navi come forme realistiche
 * VERSIONE ORIGINALE FUNZIONANTE
 */
public class NaveGraphics extends Group {
    
    private final TipoNave tipo;
    private final boolean orizzontale;
    private final double cellSize;
    private final int lunghezza;
    
    public NaveGraphics(TipoNave tipo, boolean orizzontale, double cellSize) {
        this.tipo = tipo;
        this.orizzontale = orizzontale;
        this.cellSize = cellSize;
        this.lunghezza = tipo.getLunghezza();
        
        creaNave();
    }
    
    private void creaNave() {
        // Dimensioni conservative per rimanere entro i confini
        double margine = cellSize * 0.1;
        double larghezza = orizzontale ? (cellSize - margine * 2) : (cellSize * 0.7);
        double altezza = orizzontale ? (cellSize * 0.7) : (cellSize - margine * 2);
        
        // Centra la nave nella cella
        double offsetX = orizzontale ? margine : (cellSize - larghezza) / 2;
        double offsetY = orizzontale ? (cellSize - altezza) / 2 : margine;
        
        // Colori base per tipo di nave
        Color coloreBase = getColoreBase();
        Color coloreScuro = coloreBase.deriveColor(0, 1, 0.7, 1);
        Color coloreChiaro = coloreBase.deriveColor(0, 1, 1.3, 1);
        
        // Applica l'offset per centrare la nave
        setTranslateX(-cellSize/2 + offsetX);
        setTranslateY(-cellSize/2 + offsetY);
        
        switch (tipo) {
            case PORTAEREI -> creaPortaerei(larghezza, altezza, coloreBase, coloreScuro, coloreChiaro);
            case INCROCIATORE -> creaIncrociatore(larghezza, altezza, coloreBase, coloreScuro, coloreChiaro);
            case CACCIATORPEDINIERE -> creaCacciatorpediniere(larghezza, altezza, coloreBase, coloreScuro, coloreChiaro);
            case SOTTOMARINO -> creaSottomarino(larghezza, altezza, coloreBase, coloreScuro, coloreChiaro);
        }
    }
    
    private void creaPortaerei(double larghezza, double altezza, Color base, Color scuro, Color chiaro) {
        // Corpo principale della portaerei
        Rectangle corpo = new Rectangle(larghezza, altezza);
        corpo.setFill(base);
        corpo.setStroke(scuro);
        corpo.setStrokeWidth(1);
        corpo.setArcWidth(4);
        corpo.setArcHeight(4);
        
        // Ponte di comando (struttura centrale)
        double ponteLarg = orizzontale ? larghezza * 0.3 : larghezza * 0.8;
        double ponteAlt = orizzontale ? altezza * 0.8 : altezza * 0.3;
        Rectangle ponte = new Rectangle(ponteLarg, ponteAlt);
        ponte.setFill(chiaro);
        ponte.setStroke(scuro);
        ponte.setStrokeWidth(0.5);
        ponte.setX(orizzontale ? larghezza * 0.35 : larghezza * 0.1);
        ponte.setY(orizzontale ? altezza * 0.1 : altezza * 0.35);
        
        // Antenne (piccoli cerchi)
        Circle antenna = new Circle(1.5);
        antenna.setFill(Color.GRAY);
        antenna.setCenterX(larghezza * 0.5);
        antenna.setCenterY(altezza * 0.5);
        
        getChildren().addAll(corpo, ponte, antenna);
    }
    
    private void creaIncrociatore(double larghezza, double altezza, Color base, Color scuro, Color chiaro) {
        // Corpo principale
        Rectangle corpo = new Rectangle(larghezza, altezza);
        corpo.setFill(base);
        corpo.setStroke(scuro);
        corpo.setStrokeWidth(1);
        corpo.setArcWidth(3);
        corpo.setArcHeight(3);
        
        // Torretta centrale
        Circle torretta = new Circle(Math.min(cellSize * 0.12, 4));
        torretta.setFill(scuro);
        torretta.setStroke(Color.BLACK);
        torretta.setStrokeWidth(0.5);
        torretta.setCenterX(larghezza * 0.5);
        torretta.setCenterY(altezza * 0.5);
        
        // Cannone
        Line cannone = new Line();
        if (orizzontale) {
            cannone.setStartX(torretta.getCenterX());
            cannone.setEndX(torretta.getCenterX() + 4);
            cannone.setStartY(torretta.getCenterY());
            cannone.setEndY(torretta.getCenterY());
        } else {
            cannone.setStartX(torretta.getCenterX());
            cannone.setEndX(torretta.getCenterX());
            cannone.setStartY(torretta.getCenterY());
            cannone.setEndY(torretta.getCenterY() + 4);
        }
        cannone.setStroke(Color.BLACK);
        cannone.setStrokeWidth(1.5);
        
        // Ponte di comando
        Rectangle ponte = new Rectangle(
            orizzontale ? larghezza * 0.2 : larghezza * 0.6,
            orizzontale ? altezza * 0.6 : altezza * 0.2
        );
        ponte.setFill(chiaro);
        ponte.setStroke(scuro);
        ponte.setX(orizzontale ? larghezza * 0.4 : larghezza * 0.2);
        ponte.setY(orizzontale ? altezza * 0.2 : altezza * 0.4);
        
        getChildren().addAll(corpo, ponte, torretta, cannone);
    }
    
    private void creaCacciatorpediniere(double larghezza, double altezza, Color base, Color scuro, Color chiaro) {
        // Corpo principale (più affusolato)
        Polygon corpo = new Polygon();
        if (orizzontale) {
            corpo.getPoints().addAll(new Double[]{
                0.0, altezza * 0.3,
                larghezza * 0.1, 0.0,
                larghezza * 0.9, 0.0,
                larghezza, altezza * 0.3,
                larghezza, altezza * 0.7,
                larghezza * 0.9, altezza,
                larghezza * 0.1, altezza,
                0.0, altezza * 0.7
            });
        } else {
            corpo.getPoints().addAll(new Double[]{
                larghezza * 0.3, 0.0,
                larghezza * 0.7, 0.0,
                larghezza, altezza * 0.1,
                larghezza, altezza * 0.9,
                larghezza * 0.7, altezza,
                larghezza * 0.3, altezza,
                0.0, altezza * 0.9,
                0.0, altezza * 0.1
            });
        }
        corpo.setFill(base);
        corpo.setStroke(scuro);
        corpo.setStrokeWidth(1);
        
        // Torretta piccola
        Circle torretta = new Circle(Math.min(cellSize * 0.08, 3));
        torretta.setFill(scuro);
        torretta.setStroke(Color.BLACK);
        torretta.setCenterX(larghezza * 0.5);
        torretta.setCenterY(altezza * 0.5);
        
        // Ponte piccolo
        Rectangle ponte = new Rectangle(
            orizzontale ? larghezza * 0.15 : larghezza * 0.4,
            orizzontale ? altezza * 0.4 : altezza * 0.15
        );
        ponte.setFill(chiaro);
        ponte.setStroke(scuro);
        ponte.setX(orizzontale ? larghezza * 0.6 : larghezza * 0.3);
        ponte.setY(orizzontale ? altezza * 0.3 : altezza * 0.6);
        
        getChildren().addAll(corpo, ponte, torretta);
    }
    
    private void creaSottomarino(double larghezza, double altezza, Color base, Color scuro, Color chiaro) {
        // Corpo principale (forma ovale)
        Ellipse corpo = new Ellipse(larghezza / 2, altezza / 2);
        corpo.setCenterX(larghezza / 2);
        corpo.setCenterY(altezza / 2);
        corpo.setFill(base);
        corpo.setStroke(scuro);
        corpo.setStrokeWidth(1);
        
        // Torre di comando (piccolo rettangolo)
        Rectangle torre = new Rectangle(
            orizzontale ? larghezza * 0.2 : larghezza * 0.5,
            orizzontale ? altezza * 0.8 : altezza * 0.2
        );
        torre.setFill(chiaro);
        torre.setStroke(scuro);
        torre.setX(orizzontale ? larghezza * 0.4 : larghezza * 0.25);
        torre.setY(orizzontale ? altezza * 0.1 : altezza * 0.4);
        torre.setArcWidth(2);
        torre.setArcHeight(2);
        
        // Periscopio (linea sottile)
        Line periscopio = new Line();
        if (orizzontale) {
            periscopio.setStartX(larghezza * 0.5);
            periscopio.setEndX(larghezza * 0.5);
            periscopio.setStartY(0);
            periscopio.setEndY(altezza * 0.1);
        } else {
            periscopio.setStartX(0);
            periscopio.setEndX(larghezza * 0.25);
            periscopio.setStartY(altezza * 0.5);
            periscopio.setEndY(altezza * 0.5);
        }
        periscopio.setStroke(Color.GRAY);
        periscopio.setStrokeWidth(1);
        
        getChildren().addAll(corpo, torre, periscopio);
    }
    
    private Color getColoreBase() {
        return switch (tipo) {
            case PORTAEREI -> Color.DARKRED;
            case INCROCIATORE -> Color.DARKBLUE;
            case CACCIATORPEDINIERE -> Color.DARKGREEN;
            case SOTTOMARINO -> Color.DARKORANGE;
        };
    }
    
    /**
     * Aggiorna la nave per mostrare che è stata colpita
     */
    public void mostraColpita() {
        // Aggiungi effetto "X" rossa per indicare che è colpita
        Line x1 = new Line(2, 2, cellSize - 2, cellSize - 2);
        Line x2 = new Line(cellSize - 2, 2, 2, cellSize - 2);
        x1.setStroke(Color.RED);
        x2.setStroke(Color.RED);
        x1.setStrokeWidth(2);
        x2.setStrokeWidth(2);
        getChildren().addAll(x1, x2);
    }
    
    /**
     * Aggiorna la nave per mostrare che è affondata
     */
    public void mostraAffondata() {
        // Scurisci tutta la nave e aggiungi effetto affondamento
        setOpacity(0.7);
        
        // Aggiungi onde d'acqua intorno
        for (int i = 0; i < 2; i++) {
            Arc onda = new Arc();
            onda.setCenterX(Math.random() * cellSize * 0.8);
            onda.setCenterY(Math.random() * cellSize * 0.8);
            onda.setRadiusX(3 + Math.random() * 3);
            onda.setRadiusY(2 + Math.random() * 2);
            onda.setStartAngle(0);
            onda.setLength(180);
            onda.setType(ArcType.OPEN);
            onda.setStroke(Color.LIGHTBLUE);
            onda.setFill(null);
            onda.setStrokeWidth(1);
            getChildren().add(onda);
        }
    }
}