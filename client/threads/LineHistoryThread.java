/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.threads;

import whiteboard.client.drawingObjects.DrawingObject;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import whiteboard.client.ServerListener;

/**
 * public class LineHistoryThread
 * extends myThread
 * implements Runnable
 * 
 * An implementation of myThread.
 * Collects commands to a buffer and executes them. Handles the concurrent linehistory modifications which come from the server.
 * 
 * @author mylle
 */
public class LineHistoryThread extends myThread implements Runnable 
{
    private LinkedBlockingQueue<Pair> objectsToUpdate;
    private boolean running; // used to make the thread go to sleep
    private ServerListener serverListener;

    /**
     * Constructs the object with the given serverlistener
     * @param serverListener object used for listening the server 
     */
    public LineHistoryThread(ServerListener serverListener)
    {
        super(true);
        objectsToUpdate = new LinkedBlockingQueue<>();
        running = false;
        this.serverListener = serverListener;
        //serverListener.setLineHistoryThread(this);
    }
    
    @Override    
    synchronized public void run()
    {
        Pair<String, Object> obj;
        try 
        {
            while(alive)
            {
                obj = objectsToUpdate.poll();
                if (obj != null)
                {
                    Iterator<Object> it;
                    DrawingObject lineObject;
                    //add single drawable object to linehistory
                    switch (obj.getKey()) 
                    {
                    //Clear lines by id from linehistory
                        case "AddDrawable":
                            addToLineHistory(obj.getValue());
                            break;
                        case "ClearOwnLines":
                            synchronized(ServerListener.lineHistory)
                            {
                                int userId = (int) obj.getValue();
                                it = ServerListener.lineHistory.iterator();
                                while (it.hasNext())
                                {
                                    lineObject = (DrawingObject) it.next();
                                    if (lineObject.getId() == userId)
                                    {
                                        it.remove();
                                    } 
                                }
                            }
                            break;
                            //Clear line history
                        case "CLEAR":
                            synchronized (serverListener.getServerListenerLineHistory())
                            {
                                serverListener.clearLineHistory();
                            }
                            break;
                            //handle undo -action
                        case "LineHistory":
                            serverListener.setServerListenerLineHistory((LinkedList<Object>) obj.getValue());
                            break;
                        case "UNDO":
                            int id = (int) ((Pair)((Pair) obj).getValue()).getKey();
                            int undoId = (int) ((Pair)((Pair) obj).getValue()).getValue();

                            synchronized (ServerListener.lineHistory)
                            {
                                it = ServerListener.lineHistory.iterator();
                            
                                //check if the user id and undo id match -> remove from the linehistory
                                while (it.hasNext())
                                {          
                                    lineObject = (DrawingObject) it.next();                         
                                    if (lineObject.getId() == id && lineObject.getUndoId() == undoId)
                                    {
                                        it.remove();
                                    }
                                }
                            }       
                            break;
                    }
                } else 
                {
                    this.running = false;
                    this.wait(); // if buffer is empty thread waits
                }
            }
        } catch (InterruptedException ex)
        {
            Logger.getLogger(ObjectSenderThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LineHistoryThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("LineHistoryThreadClosed");
    }
    
    /**
     * Adds an object to linehistory
     * @param obj a drawingobject to be added
     * @throws IOException if something goes wrong
     */
    private void addToLineHistory(Object obj) throws IOException
    {
        synchronized(ServerListener.lineHistory)
        {
            ServerListener.lineHistory.add(obj);
        }
    }
    
    /**
     * Adds a command to the buffer
     * @param object a command to be executed
     */
    synchronized public void addToBuffer(Pair object)
    {
        this.objectsToUpdate.add(object);
        
        //if thread not running 
        if (!running)
        {
            running = true;
            this.notify();// if the thread is waiting, wakes up the thread
        }
    }
    
}
