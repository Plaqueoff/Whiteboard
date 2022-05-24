/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.drawingObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * public class Square
 * extends DrawingObject
 * 
 * An implementation of the DrawingObject
 * Creates a rectangle from with a start point where the mouse was clicked and an end point where it was released
 * 
 * @author mylle
 */
public class Square extends DrawingObject {

    private double startX;
    private double startY;
    private double endX;
    private double endY;
    
    /**
     * Constructs the object with a default id of -1
     */
    public Square(){
        super(-1);
    }
    
    /**
     * Constructs the object with the given start and end coordinates and id
     * @param startX the starting x coordinate
     * @param startY the starting y coordinate
     * @param endX the ending x coordinate
     * @param endY the ending y coordinate
     * @param id the id
     */
    public Square(double startX, double startY, double endX, double endY, int id) {
        super(id);
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    /**
     * Sets the starting coordinates of the object
     * @param x the starting x coordinates
     * @param y the starting y coordinate
     * @return true
     */
    @Override
    public boolean setStartCoords(double x, double y) {
        this.startX = x;
        this.startY = y;
        return true;
    }

    /**
     * Sets the end coordinates of the object
     * @param x the ending x coordinate
     * @param y the ending y coordinate
     */
    @Override
    public void setEndCoords(double x, double y) {
        this.endX = x;
        this.endY = y;
    }

    /**
     * Draws the object with the given graphics context onto its respective canvas.
     * @param gc the graphics context of a javafx canvas
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setLineWidth(this.getSize());
        gc.setGlobalAlpha(this.getOpacity());
        gc.setStroke(Color.web(this.getLineColor()));
        gc.strokeLine(startX, startY, startX, endY);
        gc.strokeLine(startX, startY, endX, startY);
        gc.strokeLine(startX, endY, endX, endY);
        gc.strokeLine(endX, startY, endX, endY);
    }

    /**
     * Returns an empty square object
     * @return an empty square object
     */
    @Override
    public Square renew() {//Creates a new object to replace the one sent to socket
        return new Square();
    }

    /**
     * Returns true if the object changes depending on the path taken to draw it
     * @return false
     */
    @Override
    public boolean isDragAffected() { //If dragging has an effect on the object returns true
        return false;
    }

    /**
     * Returns the x coordinate of the object
     * @return the x coordinate of the object
     */
    @Override
    public double getEndX() {
        return this.endX;
    }

    /**
     * Returns the y coordinate of the object
     * @return the y coordinate of the object
     */
    @Override
    public double getEndY() {
        return this.endY;
    }
    
}
