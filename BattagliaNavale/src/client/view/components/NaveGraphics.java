package client.view.components;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import shared.model.TipoNave;

/**
 * Componente grafico per rappresentare le navi come forme realistiche
 * invece dei semplici quadratini colorati
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
        // Calcola dimensioni
        double larghezza = orizzontale ? lunghezza * cellSize * 0.9 : cellSize * 0.6;
        double altezza = orizzontale ? cellSize * 0.6 : lunghezza * cellSize * 0.9;
        
        // Colori base per tipo di nave
        Color coloreBase = getColoreBase();
        Color coloreScuro = coloreBase.deriveColor(0, 1, 0.7, 1);
        Color coloreChiaro = coloreBase.deriveColor(0, 1, 1.3, 1);
        
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
        corpo.setArcWidth(8);
        corpo.setArcHeight(8);
        
        // Ponte di comando (struttura centrale)
        double ponteLarg = orizzontale ? larghezza * 0.3 : larghezza * 0.8;
        double ponteAlt = orizzontale ? altezza * 0.8 : altezza * 0.3;
        Rectangle ponte = new Rectangle(ponteLarg, ponteAlt);
        ponte.setFill(chiaro);
        ponte.setStroke(scuro);
        ponte.setStrokeWidth(0.5);
        ponte.setX(orizzontale ? larghezza * 0.35 : larghezza * 0.1);
        ponte.setY(orizzontale ? altezza * 0.1 : altezza * 0.35);
        
        // Pista di decollo (linee)
        for (int i = 1; i < lunghezza; i++) {
            Line linea = new Line();
            if (orizzontale) {
                linea.setStartX(i * cellSize * 0.9 / lunghezza);
                linea.setEndX(i * cellSize * 0.9 / lunghezza);
                linea.setStartY(2);
                linea.setEndY(altezza - 2);
            } else {
                linea.setStartX(2);
                linea.setEndX(larghezza - 2);
                linea.setStartY(i * cellSize * 0.9 / lunghezza);
                linea.setEndY(i * cellSize * 0.9 / lunghezza);
            }
            linea.setStroke(scuro);
            linea.setStrokeWidth(0.5);
            getChildren().add(linea);
        }
        
        // Antenne (piccoli cerchi)
        for (int i = 0; i < 3; i++) {
            Circle antenna = new Circle(1.5);
            antenna.setFill(Color.GRAY);
            if (orizzontale) {
                antenna.setCenterX(larghezza * 0.2 + i * larghezza * 0.3);
                antenna.setCenterY(altezza * 0.5);
            } else {
                antenna.setCenterX(larghezza * 0.5);
                antenna.setCenterY(altezza * 0.2 + i * altezza * 0.3);
            }
            getChildren().add(antenna);
        }
        
        getChildren().addAll(corpo, ponte);
    }
    
    private void creaIncrociatore(double larghezza, double altezza, Color base, Color scuro, Color chiaro) {
        // Corpo principale
        Rectangle corpo = new Rectangle(larghezza, altezza);
        corpo.setFill(base);
        corpo.setStroke(scuro);
        corpo.setStrokeWidth(1);
        corpo.setArcWidth(6);
        corpo.setArcHeight(6);
        
        // Torrette (cerchi)
        int numTorrette = 3;
        for (int i = 0; i < numTorrette; i++) {
            Circle torretta = new Circle(cellSize * 0.15);
            torretta.setFill(scuro);
            torretta.setStroke(Color.BLACK);
            torretta.setStrokeWidth(0.5);
            
            if (orizzontale) {
                torretta.setCenterX(larghezza * 0.15 + i * larghezza * 0.35);
                torretta.setCenterY(altezza * 0.5);
            } else {
                torretta.setCenterX(larghezza * 0.5);
                torretta.setCenterY(altezza * 0.15 + i * altezza * 0.35);
            }
            getChildren().add(torretta);
            
            // Cannoni (piccole linee)
            Line cannone = new Line();
            if (orizzontale) {
                cannone.setStartX(torretta.getCenterX());
                cannone.setEndX(torretta.getCenterX() + cellSize * 0.1);
                cannone.setStartY(torretta.getCenterY());
                cannone.setEndY(torretta.getCenterY());
            } else {
                cannone.setStartX(torretta.getCenterX());
                cannone.setEndX(torretta.getCenterX());
                cannone.setStartY(torretta.getCenterY());
                cannone.setEndY(torretta.getCenterY() + cellSize * 0.1);
            }
            cannone.setStroke(Color.BLACK);
            cannone.setStrokeWidth(2);
            getChildren().add(cannone);
        }
        
        // Ponte di comando
        Rectangle ponte = new Rectangle(
            orizzontale ? larghezza * 0.2 : larghezza * 0.6,
            orizzontale ? altezza * 0.6 : altezza * 0.2
        );
        ponte.setFill(chiaro);
        ponte.setStroke(scuro);
        ponte.setX(orizzontale ? larghezza * 0.4 : larghezza * 0.2);
        ponte.setY(orizzontale ? altezza * 0.2 : altezza * 0.4);
        
        getChildren().addAll(corpo, ponte);
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
        
        // Torretta singola
        Circle torretta = new Circle(cellSize * 0.12);
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
        torre.setArcWidth(3);
        torre.setArcHeight(3);
        
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
        periscopio.setStrokeWidth(1.5);
        
        // Portelli (piccoli cerchi)
        Circle portello1 = new Circle(2, Color.DARKGRAY);
        Circle portello2 = new Circle(2, Color.DARKGRAY);
        if (orizzontale) {
            portello1.setCenterX(larghezza * 0.3);
            portello1.setCenterY(altezza * 0.5);
            portello2.setCenterX(larghezza * 0.7);
            portello2.setCenterY(altezza * 0.5);
        } else {
            portello1.setCenterX(larghezza * 0.5);
            portello1.setCenterY(altezza * 0.3);
            portello2.setCenterX(larghezza * 0.5);
            portello2.setCenterY(altezza * 0.7);
        }
        
        getChildren().addAll(corpo, torre, periscopio, portello1, portello2);
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
        x1.setStrokeWidth(3);
        x2.setStrokeWidth(3);
        getChildren().addAll(x1, x2);
    }
    
    /**
     * Aggiorna la nave per mostrare che è affondata
     */
    public void mostraAffondata() {
        // Scurisci tutta la nave e aggiungi effetto affondamento
        setOpacity(0.7);
        
        // Aggiungi onde d'acqua intorno
        for (int i = 0; i < 3; i++) {
            Arc onda = new Arc();
            onda.setCenterX(Math.random() * cellSize);
            onda.setCenterY(Math.random() * cellSize);
            onda.setRadiusX(5 + Math.random() * 5);
            onda.setRadiusY(3 + Math.random() * 2);
            onda.setStartAngle(0);
            onda.setLength(180);
            onda.setType(ArcType.OPEN);
            onda.setStroke(Color.LIGHTBLUE);
            onda.setFill(null);
            onda.setStrokeWidth(1.5);
            getChildren().add(onda);
        }
    }
}