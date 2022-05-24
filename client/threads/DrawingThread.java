/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.threads;

import whiteboard.client.drawingObjects.DrawingObject;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import whiteboard.client.Controller;

/**
 * public class DrawingThread
 * extends myThread
 * implements Runnable
 * 
 * An implementation of myThread.
 * Collects DrawingObjects to a buffer and draws them onto a Javafx canvas.
 * 
 * @author mylle
 */
public class DrawingThread extends myThread implements Runnable {
    private GraphicsContext graphicsContext;
    private LinkedBlockingQueue<DrawingObject> objectsToDraw;
    private boolean running;
    
    /**
     * Constructs the object with the given graohicsContext
     * @param graphicsContext Javafx canvas' graphics context
     */
    public DrawingThread(GraphicsContext graphicsContext){
        super(true);
        this.running = false;
        this.graphicsContext = graphicsContext;
        this.objectsToDraw = new LinkedBlockingQueue<DrawingObject>();
    }

    @Override
    public void run() {
        try 
        {
            Thread.sleep(100);
            while(alive)
            {
                if (!this.objectsToDraw.isEmpty()){
                    DrawingObject drawingObj = objectsToDraw.poll();
                    Platform.runLater(() -> {
                        drawingObj.draw(graphicsContext);
                        Controller.setForeignCursor(drawingObj.getId(), drawingObj.getEndX(), drawingObj.getEndY());
                    });
                    
                } else 
                {
                    running = false;
                    synchronized(this) 
                    {
                        this.wait(); //When buffer is empty thread waits
                    }
                }
                Thread.sleep(16); //max 60 lines per second
            }
        } catch (InterruptedException ie){
            ie.printStackTrace();
        }
        System.out.println("DrawingThread closed");
    }
    
    /**
     * Adds a DrawingObject to be drawn
     * @param l the object to be drawn
     */
    public synchronized void addToDrawer(DrawingObject l){
        objectsToDraw.add(l);
        if (!running){
            running = true;
            this.notify();//if thread is waiting, wakes up the thread
        }
    }
}
