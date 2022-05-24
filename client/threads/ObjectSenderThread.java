/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client.threads;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * public class ObjectSenderThread
 * extends myThread
 * implements Runnable
 * 
 * An implementation of myThread.
 * Collects objects to a buffer and sends them to the server.
 * 
 * @author mylle
 */
public class ObjectSenderThread extends myThread implements Runnable {
    private LinkedBlockingQueue<Object> objectsToSend;
    private ObjectOutputStream out; // used to kill the thread
    private boolean running; // used to make the thread go to sleep
    private boolean onServer = false;
    private static ObjectOutputStream sOut;
    
    //Constructor

    /**
     * Consctructs the object with the given bufferedOutputStream
     * @param output buffered outputstream
     */
    public ObjectSenderThread(OutputStream output)
    {
        super(true);
        objectsToSend = new LinkedBlockingQueue<>();
        running = false;
        try 
        {
            this.out = new ObjectOutputStream(output);
            ObjectSenderThread.sOut = out;
        } catch (IOException ex)
        {
            Logger.getLogger(ObjectSenderThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    synchronized public void run()
    {
        Object obj;
        try 
        {
            while(alive)
            {
                obj = objectsToSend.poll();
                if (obj != null)
                {
                    sendObjects(obj);
                } else 
                {
                    this.running = false;
                    this.wait(); // if buffer is empty thread waits
                }
            }
        } catch(IOException ex)
        {
            Logger.getLogger(whiteboard.client.threads.ObjectSenderThread.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            exceptionHandler();
        } catch (InterruptedException ex)
        {
            Logger.getLogger(ObjectSenderThread.class.getName()).log(Level.SEVERE, null, ex);
            exceptionHandler();
        } 
        System.out.println("ObjectSenderThread closed");
    }
    
    private void exceptionHandler()
    {
        if (!onServer)
        {
            try 
            {
                sendObjects("CloseSocket");
            } catch (IOException ex) 
            {
                Logger.getLogger(ObjectSenderThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    /**
     * Sends objects to server
     * @param obj the object to be sent
     * @throws IOException if something goes wrong
     */
    private void sendObjects(Object obj) throws IOException
    {
        out.writeObject(obj);
        out.flush();
    }
    
    /**
     * Adds an object to the buffer to be sent to the server
     * @param object the object to be sent
     */
    synchronized public void addToBuffer(Object object)
    {
        this.objectsToSend.add(object);
        if (!running)
        {
            running = true;
            this.notify();// if the thread is waiting, wakes up the thread
        }
    }
    /**
     * Sets the boolean onServer to true. Differentiates client and server objectsenderthreads.
     */
    public void setOnServer(){
        this.onServer = true;
    }
    
    
    public static void sendCloseRequest(){
        try {
            ObjectSenderThread.sOut.writeObject("CloseSocket");
            ObjectSenderThread.sOut.flush();
        } catch (IOException ex) {
            Logger.getLogger(ObjectSenderThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
