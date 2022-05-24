/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.drawingObjects;

import java.io.Serializable;
import javafx.scene.canvas.GraphicsContext;

/**
 * public abstract class DrawingObject
 * implements Serializable, Drawable
 * 
 * This class provides structure for the objects which can be drawn in the program.
 * It holds the method to draw the object onto a javafx canvas with the use of the canvas' graphics context and the graphics context's settings
 * 
 * @author mylle
 */
public abstract class DrawingObject implements Serializable, Drawable {
    private int id = -1;
    private String lineColor = "0x000000ff";
    double opacity = 0.0;
    int size = 1;
    private int undoID = -1;
    
    /**
     * Constructs a drawing object with the given id.
     * @param id the id of the creating users
     */
    public DrawingObject(int id){
        this.id = id;
    }
    
    /**
     * Constructs a drawing object with the given size and id
     * @param id the creating users id
     * @param size the width of the lines the object is made from
     */
    public DrawingObject(int id, int size){
        this.id = id;
        this.size = size;
    }
    
    /**
     * Returns the id used to differentiate between drawing actions for the use of an undo-method
     * @return the object's undo id
     */
    public int getUndoId(){
        return this.undoID;
    }
    
    /**
     * Sets the number used to differentiate between drawing actions for the use of an undo-method
     * @param undoId the object's undo id
     */
    public void setUndoId(int undoId){
        this.undoID = undoId;
    }
    
    /**
     * Returns the number used to differentiate between the users which create these objects.
     * @return the user's id
     */
    public int getId(){
        return this.id;
    }
    
    /**
     * Sets the number used to differentiate between the users which create these objects.
     * @param id the user's id
     */
    public void setId(int id){
        this.id = id;
    }
    
    /**
     * Returns the hexadecimal representation of the color of the object
     * @return hexadecimal string representation of the color
     */
    public String getLineColor() {
        return this.lineColor;
    }
    
    /**
     * Sets the color of the object by a hexadecimal
     * @param lineColor hexadecimal string representation of the color
     */
    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }
    
    /**
     * Returns the opacity of the object
     * @return opacity of the lines the object is made from
     */
    public double getOpacity(){
        return this.opacity;
    }
    
    /**
     * Sets the opacity of the object
     * @param opacity opacity of the lines the object is made from
     */
    public void setOpacity(double opacity){
        this.opacity = opacity;
    }
    
    /**
     * Returns the size of the object
     * @return the width of the lines the object is made from
     */
    public int getSize(){
        return this.size;
    }
    
    /**
     * Sets the size of the object
     * @param size the width of the lines the object is made from
     */
    public void setSize(int size){
        this.size = size;
    }
    
    @Override
    public String toString(){
        return "Drawing object: id: " + this.id + "undoId: " + this.undoID;
    }
}

interface Drawable { //interface to clean controller mouseeventhandlers a bit 
    public boolean setStartCoords(double x, double y);
    public void setEndCoords(double x, double y);
    public void draw(GraphicsContext gc);
    public Object renew();
    public boolean isDragAffected();
    public double getEndX();
    public double getEndY();
}
