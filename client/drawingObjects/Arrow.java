/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.drawingObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * public class Arrow
 * extends DrawingObject
 * 
 * Implementation of the DrawingObject
 * Creates an arrow shape by the use of three lines.
 * The main line is drawn by the user by clicking, dragging and releasing and the arrow shape is created afterwards.
 * The arrow is proportional to the size of the main line and are 45 degrees off from the main line.
 * 
 * @author mylle
 */
public class Arrow extends DrawingObject {
    private double startX;
    private double startY;
    private double endX;
    private double endY;

    /**
     * Constructs the object with a default id of -1
     */
    public Arrow(){
        super(-1);
    }
    
    /**
     * Connstruct the object with the given start and end coordinates and the given id.
     * @param startX the x coordinate of the start of the object
     * @param startY the y coordinate of the start of the object
     * @param endX the x coordinate of the end of the object
     * @param endY the y coordinate of the end of the object
     * @param id the number which differentiates between users
     */
    public Arrow(double startX, double startY, double endX, double endY, int id) {
        super(id);
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
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
     * @param gc the graphics context of a javafx canvas
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setLineWidth(this.getSize());
        gc.setGlobalAlpha(this.getOpacity());
        gc.setStroke(Color.web(this.getLineColor()));
        
        double dx = this.endX - this.startX;
        double dy = this.endY - this.startY;
        double angle = Math.atan2(dy, dx); //Angle of arrow staff
        double len = Math.sqrt((dx*dx)+(dy*dy))/8; //Get length for arrow head
        
        gc.strokeLine(this.startX, this.startY, this.endX, this.endY);
        gc.strokeLine(this.endX, this.endY, len * Math.cos(angle + 135*Math.PI/180) + this.endX, len * Math.sin(angle + 135*Math.PI/180) + this.endY); 
        gc.strokeLine(this.endX, this.endY, len * Math.cos(angle - 135*Math.PI/180) + this.endX, len * Math.sin(angle - 135*Math.PI/180) + this.endY);
    }

    /**
     * Returns an empty arrow object
     * @return arrow object
     */
    @Override
    public Arrow renew() {//Creates a new object to replace the one sent to socket
        return new Arrow();
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
