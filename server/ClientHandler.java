package whiteboard.server;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import whiteboard.client.CanvasImage;
import whiteboard.client.drawingObjects.DrawingObject;
import whiteboard.client.drawingObjects.Line;
import whiteboard.client.threads.ObjectSenderThread;
import whiteboard.client.User;
import whiteboard.client.UserArrayList;

/**
 *
 * @author jukka
 */
public class ClientHandler implements Runnable {
    //Attributes
    static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    static LinkedList<Object> lineHistory = new LinkedList<>(); //TODO switch to custom line object
    static ArrayList<Object> messageHistory = new ArrayList<>();
    static UserArrayList userArray = new UserArrayList();
    private Socket clientSocket;
    private ObjectInputStream in = null;
    private ObjectSenderThread objectSender;
    private Integer clientId; 
    private static AtomicInteger newId = new AtomicInteger((int)(Math.random()*1000000));
    Pair<Integer, String> countOfClients = null;
    private ServerActionThread sat;
    static CanvasImage currentCanvasImage = new CanvasImage();
    static SQLHandler sqlThread;
    //Constructor

    /**
     *
     * @param socket connection between server and client
     * @param sat thread for handling actions received from clients
     */
    public ClientHandler(Socket socket, ServerActionThread sat) {
        synchronized(ClientHandler.clientHandlers)
        {
            ClientHandler.clientHandlers.add(this);
            countOfClients = new Pair(ClientHandler.clientHandlers.size(), "COUNTOFCLIENTS");
        }
        
        this.sat = sat;
        try 
        {
            this.clientSocket = socket;
            // create an object output stream from the output stream so we can send an object through it
            OutputStream outputStream = clientSocket.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(outputStream);
            this.objectSender = new ObjectSenderThread(bos);
            this.objectSender.setOnServer();
            new Thread(this.objectSender).start();

            // get the input stream from the connected socket
            InputStream inputStream = clientSocket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            // create a DataInputStream so we can read data from it.
            this.in = new ObjectInputStream(bis);

            //Send messagehistory, linehistory, canvasimage and clients to the new socket
            synchronized(ClientHandler.lineHistory)
            {
                ClientHandler.lineHistory.add(new Line(0,0,1,1,0));
            }
            int value = ClientHandler.newId.incrementAndGet();
            this.objectSender.addToBuffer(new Integer(value));
            this.clientId = value;
            System.out.println(value);
            this.objectSender.addToBuffer(messageHistory);
            this.objectSender.addToBuffer(ClientHandler.lineHistory);

            if (currentCanvasImage.getEncodedString() != null)
            {
                this.objectSender.addToBuffer(currentCanvasImage);
            }
            synchronized(ClientHandler.clientHandlers)
            {
                for (ClientHandler c : ClientHandler.clientHandlers)
                {
                    c.sendLine(countOfClients);     
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Object object = null;
            String line = "";
            
            OUTER:
            while ((object = in.readObject()) != null) {
                if (object instanceof DrawingObject) {
                    synchronized(ClientHandler.clientHandlers)
                    {
                        for(ClientHandler client: ClientHandler.clientHandlers)
                        {
                            if (client != this)
                            {    
                                client.sendLine(object);
                            }
                        }
                    }
                    //System.out.println("Received object: " + object.getClass().getName());
                    sat.serverDo(new Pair("AddDrawable", object));
                } else if (object instanceof String) {
                    line = (String) object;
                    System.out.println("String received: " + line);
                    //If CLEAR then clear linehistory and information to all clients
                    switch (line) {
                        case "CLEAR":
                            System.out.println(ClientHandler.lineHistory.size());
                            synchronized(ClientHandler.clientHandlers)
                            {
                                for(ClientHandler client: ClientHandler.clientHandlers)
                                {
                                    if (client != this)
                                    {
                                        client.sendLine(object);
                                    }
                                }
                            }   sat.serverDo(new Pair("Clear", -1));
                            break;
                        case "CloseSocket":
                            // Remove clienthandler from clienthandlers
                            sat.serverDo(new Pair("RemoveClient", this));
                            // Remove the user from the user lists.
                            sat.serverDo(new Pair("RemoveUser", findThisUser())); //Removes user and sends new userarray in another thread
                            
                            //send new count of clients to each client
                            synchronized(ClientHandler.clientHandlers)
                            {
                                countOfClients = new Pair(ClientHandler.clientHandlers.size(), "COUNTOFCLIENTS");
                                ClientHandler.clientHandlers.remove(this);
                                for (ClientHandler c : ClientHandler.clientHandlers)
                                {
                                    try {
                                        c.sendLine(countOfClients);
                                    } catch (IOException ex) {
                                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            // Tell client to close socket
                            objectSender.addToBuffer("CloseSocket");
                            break OUTER;
                        case "Ping":
                            objectSender.addToBuffer("Ping");
                            break;
                    }
                } else if (object.getClass().getName()=="whiteboard.client.Message")
                {
                    synchronized(ClientHandler.clientHandlers)
                    {
                        for(ClientHandler client: ClientHandler.clientHandlers) //Send messages forward
                        {
                            client.sendMessage(object);
                        }
                    }
                    sqlThread.addToMessageBuffer(object); //Add message to queue to be saved to DB

                    messageHistory.add(object); //Add the message to the server memory
                    if(messageHistory.size() > 30) { //If there are more than 30 objects in messagehistory
                        messageHistory.remove(1); //Remove the oldest message
                    }
                }
                else if (object instanceof CanvasImage) //CanvasImage selected by user
                {     
                    System.out.println("CanvasImage Received");
                    currentCanvasImage = (CanvasImage) object;
                    synchronized(ClientHandler.clientHandlers)
                    {
                        for(ClientHandler client: ClientHandler.clientHandlers)
                        {
                            if (client != this)
                            {    
                                client.sendLine(object);
                            }
                        }
                    }
                }
                else if (object instanceof User) 
                {  // new user
                    sat.serverDo(new Pair("AddUser", object));
                } 
                else if (object instanceof Pair)
                { // Pair: clear, clear own lines or undo
                    synchronized(ClientHandler.clientHandlers)
                    {
                        for (ClientHandler c : ClientHandler.clientHandlers)
                        {
                            c.sendLine(object);
                        }
                    }
                    //If pair value is clear -> clear lines from the linehistory by id
                    if(((Pair) object).getValue().equals("CLEAR"))
                    {
                        int id = (int) ((Pair) object).getKey();
                        sat.serverDo(new Pair("ClearOwnLines", id));
                    }
                    else if(((Pair) object).getValue().equals("UNDO")){
                        
                        sat.serverDo(new Pair("Undo", ((Pair)object).getKey()));
                    }
                }
                else 
                { //Unknown object type
                    System.out.println("Received object not recognized");
                }
            }

        } catch (IOException | ClassNotFoundException e) 
        {
            e.printStackTrace();
            // Remove clienthandler from clienthandlers
            sat.serverDo(new Pair("RemoveClient", this));
            // Remove the user from the user lists.
            sat.serverDo(new Pair("RemoveUser", findThisUser())); //Removes user and sends new userarray in another thread
            
            //send new count of clients to each client
            synchronized(ClientHandler.clientHandlers)
            {
                countOfClients = new Pair(ClientHandler.clientHandlers.size(), "COUNTOFCLIENTS");
                ClientHandler.clientHandlers.remove(this);
                for (ClientHandler c : ClientHandler.clientHandlers)
                {
                    try {
                        c.sendLine(countOfClients);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } finally 
        {
            synchronized (this)
            {
                try 
                {
                    if (in != null) 
                    {
                        this.wait(2000);
                        in.close();
                        System.out.println("Client " + clientSocket.getInetAddress().getHostAddress() + " disconnected!");
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        }
    }

    //Methods

    /**
     *
     * @param line object to be sent to clients
     * @throws IOException exception caused by stream error
     */
    public void sendLine(Object line) throws IOException 
    {
        objectSender.addToBuffer(line);
    }
    
    /**
     *
     * @param msg object to be sent to clients
     * @throws IOException exception caused by stream error
     */
    public void sendMessage(Object msg) throws IOException 
    {
        objectSender.addToBuffer(msg);
    }
    
    /**
     * linehistory to be sent when a new client connects
     */
    public void sendLineHistory() 
    {
        objectSender.addToBuffer(lineHistory);       
    }
    /**
     * Find this user from the list of users
     * @return u user 
     */
    
    private User findThisUser() {
        synchronized(ClientHandler.userArray)
        {
            for (User u : userArray) {
                if (u.getId() == this.clientId)
                    return u;
            }
        }
        return null;
    }
            
}
