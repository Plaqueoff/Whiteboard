/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import whiteboard.client.drawingObjects.DrawingObject;
import whiteboard.client.User;
import whiteboard.client.UserArrayList;
import whiteboard.client.threads.myThread;

import static whiteboard.server.ClientHandler.userArray;

/**
 * public class ServerActionThread
 * extends myThread
 * implements Runnable
 * 
 * An implementation of myThread.
 * Collects commands to a buffer and executes them. Handles the concurrent linehistory modifications which come from clients.
 * @author mylle
 */
public class ServerActionThread extends myThread implements Runnable{

    private LinkedBlockingQueue<Pair<String, Object>> thingsToDo;
    private boolean running;
    /**
     * Constructor with no given parameters
     */
    public ServerActionThread() {
        super(true);
        running = false;
        thingsToDo = new LinkedBlockingQueue<>();
    }
    
    @Override
    public synchronized void run() { //Thread main loop. Handles concurrent calls to modify clienthandler memory
        Pair<String, Object> command;
        
        try {
            while(alive)
            {
                command = thingsToDo.poll();

                if (command != null)
                {
                    if (command.getKey().equals("AddDrawable"))
                    {
                        synchronized(ClientHandler.lineHistory)
                        {
                            ClientHandler.lineHistory.add((DrawingObject)command.getValue());
                        }
                    }
                    else if (command.getKey().equals("AddUser")) 
                    {
                        synchronized(ClientHandler.userArray)
                        {
                            ClientHandler.userArray.add((User)command.getValue()); //Modify userArray

                            UserArrayList tmpUserArray = new UserArrayList();
                            tmpUserArray.addAll(userArray);

                            synchronized(ClientHandler.clientHandlers)
                            {
                                for (ClientHandler client: ClientHandler.clientHandlers) //Send array to all clients
                                {
                                    try {
                                        client.sendMessage(tmpUserArray);
                                    } catch (IOException ex) {
                                        Logger.getLogger(ServerActionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                    }
                    else if (command.getKey().equals("RemoveUser"))
                    {
                        synchronized(ClientHandler.userArray)
                        {
                            System.out.println(command.getValue());
                            ClientHandler.userArray.remove((User)command.getValue()); //Modify userArray

                            UserArrayList tmpUserArray = new UserArrayList();
                            tmpUserArray.addAll(userArray);

                            synchronized(ClientHandler.clientHandlers)
                            {
                                for (ClientHandler client: ClientHandler.clientHandlers) //Send array to all clients
                                {
                                    try {
                                        client.sendMessage(tmpUserArray);
                                    } catch (IOException ex) {
                                        Logger.getLogger(ServerActionThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                        
                    }
                    else if (command.getKey().equals("AddClienHandler"))
                    {
                        synchronized(ClientHandler.clientHandlers)
                        {
                            ClientHandler.clientHandlers.add((ClientHandler)command.getValue());
                        }
                    }
                    else if (command.getKey().equals("ClearOwnLines"))
                    {
                        synchronized(ClientHandler.lineHistory)
                        {
                            DrawingObject lineObject;
                            int id = (int) command.getValue();
                            Iterator<Object> it = ClientHandler.lineHistory.iterator();
                            //check if the user id match to the lineid -> remove from the linehistory
                            while (it.hasNext())
                            {                          
                                lineObject = (DrawingObject) it.next();
                                if (lineObject.getId() == id)
                                {
                                    //ClientHandler.sqlThread.addToDeleteLinesBuffer(lineObject); //Send to buffer to delete from DB
                                    it.remove();
                                } 
                            }
                        }
                    }
                    else if (command.getKey().equals("Clear"))
                    {
                        synchronized(ClientHandler.lineHistory)
                        {
                            ClientHandler.lineHistory.clear();
                        }
                    } 
                    else if (command.getKey().equals("RemoveClient"))
                    {
                        synchronized(ClientHandler.clientHandlers)
                        {
                            ClientHandler.clientHandlers.remove((ClientHandler)command.getValue());
                        }
                    }
                    else if (command.getKey().equals("Undo"))
                    {
                        synchronized(ClientHandler.lineHistory)
                        {
                            int id = (int)((Pair)command.getValue()).getKey();
                            int undoId = (int)((Pair)command.getValue()).getValue();
                            Iterator<Object> it = ClientHandler.lineHistory.iterator();
                            DrawingObject lineObject;
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
                    }
                } 
                else 
                {
                    this.running = false;
                    this.wait(); // if buffer is empty thread waits
                }
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerActionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    /**
     *
     * @param command pair object to be added to the buffer
     */
    synchronized public void serverDo(Pair command){
        thingsToDo.add(command);
        
        if (!running)
        {
            running = true;
            this.notify();// if the thread is waiting, wakes up the thread
        }
    }
    
}
