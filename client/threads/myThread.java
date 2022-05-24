/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.threads;

/**
 * public class myThread
 * superclass for drawingthread and objectsenderthread for the purpose of killing them easily
 * @author mylle
 */
public class myThread {
    public boolean alive;
    
    /**
     * Constructs the object with the given boolean
     * @param bool
     */
    public myThread(boolean bool){
        this.alive = bool;
    }
    
    /**
     * Kills the thread
     */
    synchronized public void killThread(){
        this.alive = false;
        this.notify();
    }
}
