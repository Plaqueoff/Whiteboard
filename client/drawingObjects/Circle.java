/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.drawingObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * public class Circle
 * extends DrawingObject
 * 
 * An implementation of the DrawingObject 
 * Creates a circle, where the starting coordinates are at the center of the circle and the end coordinates give its radius
 * 
 * @author mylle
 */
public class Circle extends DrawingObject {
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    
   /**
     * Constructs the object with a default id of -1
     */
    public Circle() {
        super(-1);
    }

    /**
     * Sets the starting coordinates of the object
     * @param x the x coordinate of the start of the object
     * @param y the y coordinate of the start of the object
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
     * @param x the x coordinate of the end of the object
     * @param y the y coordinate of the end of the object
     */
    @Override
    public void setEndCoords(double x, double y) {
        this.endX = x;
        this.endY = y;
    }

    /**
     * Draws the object with the given graphics context onto its respective canvas.
     * @param gc the graphics context of a Javafx canvas
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setLineWidth(this.getSize());
        gc.setGlobalAlpha(this.getOpacity());
        gc.setStroke(Color.web(this.getLineColor()));
        
        double dx = this.endX - this.startX;
        double dy = this.endY - this.startY;
        
        double r = Math.sqrt(dx*dx + dy*dy);
        gc.strokeOval(this.startX - r, this.startY - r, 2*r, 2*r);
    }

    /**
     * Returns an empty circle object
     * @return circle object
     */
    @Override
    public Object renew() {
        return new Circle();
    }

    /**
     * Returns true if the object changes depending on the path taken to draw it
     * @return false
     */
    @Override
    public boolean isDragAffected() {
        return false;
    }

    /**
     * Returns the x coordinate of the end of the object
     * @return the x coordinate of the end of the object
     */
    @Override
    public double getEndX() {
        return this.endX;
    }

    /**
     * Returns the y coordinate of the end of the object
     * @return the y coordinate of the end of the object
     */
    @Override
    public double getEndY() {
        return this.endY;
    }
    
}
