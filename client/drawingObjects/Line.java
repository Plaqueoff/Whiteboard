/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.drawingObjects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * public class Line
 * extends DrawingObject
 * 
 * An implementation of the DrawingObject
 * Creates a line with a start point where the mouse was clicked and an end point where it was released
 * 
 * @author mylle
 */
public class Line extends DrawingObject {
    String userId = "";
    double startX = 0.0;
    double startY = 0.0;
    double endX = 0.0;
    double endY = 0.0;
    Color color;
    
    /**
     * Constructs the object with a default id of -1
     */
    public Line() {
        super(-1);
    }

    /**
     * Constructs the object with the given starting coordinates xStart, yStart and ending coordinates endX, endY and the given size
     * @param xStart the x coordinate of the start of the object
     * @param xEnd the y coordinate of the start of the object
     * @param yStart the x coordinate of the end of the object
     * @param yEnd the y coordinate of the end of the object
     * @param Size the widths of the lines which makes this object
     */
    public Line(double xStart, double xEnd, double yStart, double yEnd, int Size) {
        super(-1);
        this.startX = xStart;
        this.startY = yStart;
        this.endX = xEnd;
        this.endY = yEnd;
        this.setSize(size);
    }
    
    /**
     * Returns the color of the object
     * @return the color of the object
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Returns the user id string
     * @return user id string
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the starting x coordinate
     * @return the starting x coordinate
     */
    public double getStartX() {
        return startX;
    }

    /**
     * Returns the starting y coordinate
     * @return the starting y coordinate
     */
    public double getStartY() {
        return startY;
    }

    /**
     * Returns the x coordinate of the object
     * @return the x coordinate of the object
     */
    public double getEndX() {
        return endX;
    }

    /**
     * Returns the y coordinate of the object
     * @return the y coordinate of the object
     */
    public double getEndY() {
        return endY;
    }

    /**
     * Sets the color of the object
     * @param color the color of the object
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Sets the user id string
     * @param userId the user id string
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the starting x coordinate
     * @param startX the starting x coordinate
     */
    public void setStartX(double startX) {
        this.startX = startX;
    }

    /**
     * Sets the starting y coordinate
     * @param startY the starting y coordinate
     */
    public void setStartY(double startY) {
        this.startY = startY;
    }

    /**
     * Sets the ending x coordinate
     * @param endX the ending x coordinate
     */
    public void setEndX(double endX) {
        this.endX = endX;
    }

    /**
     * Sets the ending y coordinate
     * @param endY the ending y coordinate
     */
    public void setEndY(double endY) {
        this.endY = endY;
    }
    
    /**
     * Sets the starting coordinates of the object
     * @param x the starting x coordinate
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
     * Sets the ending coordinates of the object
     * @param x the ending x coordinates
     * @param y the ending y coordinates
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
        gc.strokeLine(startX, startY, endX, endY);
    }

    /**
     * Returns an empty line object
     * @return an empty line object
     */
    @Override
    public Line renew() { //Creates a new object to replace the one sent to socket
        return new Line();
    }

    /**
     * Returns true if the object changes depending on the path taken to draw it
     * @return false
     */
    @Override
    public boolean isDragAffected() { //If dragging has an effect on the object returns true
        return false;
    }
    
}
