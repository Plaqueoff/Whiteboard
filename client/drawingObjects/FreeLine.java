package whiteboard.client.drawingObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * public class FreeLine
 * extends DrawingObject
 * 
 * An implementation of the DrawingObject.
 * Creates a smooth line between the mouse click and release by the path the mouse was dragged.
 * 
 * @author jukka
 */
public class FreeLine extends DrawingObject {

    String userId = "";
    double[] xValues;
    double[] yValues;
    int polyLineIndex = 0;
    
    Color color;
    
    /**
     * Constructs the object with a default id of -1
     */
    public FreeLine ()
    {
        super(-1);
        this.xValues = new double[10];
        this.yValues = new double[10];
    }
    
    /**
     * Constructs the object with the given size
     * @param Size
     */
    public FreeLine(int Size){
        super(-1);
        this.color = Color.BLUE;
        this.size = Size;
        this.xValues = new double[10];
        this.yValues = new double[10];
    }

    /**
     * Returns the user id string
     * @return user id string
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user id string
     * @param userId user id string
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the color of the object
     * @return the color of the object
     */
    public Color getColor(){
        return color;
    }

    /**
     * Returns the line id
     * @return the line id
     */
    public int getLineId(){
        return this.getId();
    }

    /**
     * Sets the color of the object
     * @param c the color of the object
     */
    public void setColor(Color c){
        color=c;
    }

    /**
     * Sets the id of the line
     * @param x the id of the line
     */
    public void setLineId(int x){
        this.setId(x);
    }    

    @Override
    public String toString() {
        return "Line{" + " X-Coordinates: " + xValues + " Y-Coordinates: " + yValues + " LineID: " + this.getId() +'}';
    }    

    /**
     * Sets a coordinate pair the line goes through
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the point array is full
     */
    @Override
    public boolean setStartCoords(double x, double y) {
        this.xValues[this.polyLineIndex] = x;
        this.yValues[this.polyLineIndex] = y;
        this.polyLineIndex++;
        return polyLineIndex < 10;
    }

    /**
     * Sets a coordinate pair the line goes through
     * @param x the x coordinate
     * @param y the y coordinate
     */
    @Override
    public void setEndCoords(double x, double y) {
        this.xValues[this.polyLineIndex] = x;
        this.yValues[this.polyLineIndex] = y;
        this.polyLineIndex++;
        if (this.polyLineIndex == 1) {
            this.xValues[this.polyLineIndex] = x + 0.001;//if polyline has only 1 point at the end
            this.yValues[this.polyLineIndex] = y + 0.001;//this will give another point for line to form
            this.polyLineIndex++;
        }
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
        gc.strokePolyline(this.xValues, this.yValues, this.polyLineIndex);
    }

    /**
     * Returns an empty freeline object
     * @return freeline object
     */
    @Override
    public FreeLine renew() {//Creates a new object to replace the one sent to socket
        return new FreeLine();
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
     * Returns the x coordinate of the object
     * @return the x coordinate of the object
     */
    @Override
    public double getEndX() {
        return this.xValues[this.polyLineIndex - 1];
    }

    /**
     * Returns the y coordinate of the object
     * @return the y coordinate of the object
     */
    @Override
    public double getEndY() {
        return this.yValues[this.polyLineIndex - 1];
    }
    
}
