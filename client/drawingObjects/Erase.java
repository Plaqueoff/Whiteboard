/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.drawingObjects;

import javafx.scene.canvas.GraphicsContext;

/**
 * public class Erase
 * extends DrawingObject
 * 
 * An implementation of the DrawingObject.
 * Clears a rectangle with the clicked point as its center.
 * 
 * @author mylle
 */
public class Erase extends DrawingObject {
    private double x;
    private double y;
    
    /**
    * Constructs the object with a default id of -1
    */
    public Erase(){
        super(-1);
    }
    
    /**
     * Constructs the object with the given coordinates, size and id
     * @param x
     * @param y
     * @param size
     * @param id
     */
    public Erase(double x, double y, int size, int id){
        super(size, id);
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x coordinate of the object
     * @return the x coordinate of the object
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y coordinate of the object
     * @return
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the coordinates of the object
     * @param x the x coordinate of the object
     * @param y the y coordinate of the object
     * @return false
     */
    @Override
    public boolean setStartCoords(double x, double y) {
        this.x = x;
        this.y = y;
        return false;
    }

    /**
     * Does nothing.
     * @param x double
     * @param y double
     */
    @Override
    public void setEndCoords(double x, double y) {
        //Unnecessary method. Does nothing, but must be implemented
    }

    /**
     * Returns an empty erase object
     * @return erase object
     */
    @Override
    public Erase renew() {//Creates a new object to replace the one sent to socket
        return new Erase();
    }

    /**
     * Returns true if the object changes depending on the path taken to draw it
     * @return true
     */
    @Override
    public boolean isDragAffected() { //If dragging has an effect on the object returns true
        return true;
    }

    /**
     * Draws the object with the given graphics context onto its respective canvas.
     * @param gc the graphics context of a javafx canvas
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.clearRect(this.x - this.getSize()/2, this.y - this.getSize()/2, this.getSize(), this.getSize());
    }

    /**
     * Returns the x coordinate of the object
     * @return the x coordinate of the object
     */
    @Override
    public double getEndX() {
        return this.x;
    }

    /**
     * Returns the y coordinate of the object
     * @return the y coordinate of the object
     */
    @Override
    public double getEndY() {
        return this.y;
    }
}
