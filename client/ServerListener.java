package whiteboard.client;
import whiteboard.client.threads.ObjectSenderThread;
import whiteboard.client.threads.LineHistoryThread;
import whiteboard.client.threads.myThread;
import whiteboard.client.drawingObjects.DrawingObject;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.util.Pair;

/**
 * This class will create a socket for the client and connect and communicate
 * with the server
 */
public class ServerListener extends myThread implements Runnable{
    //Attributes
    private Socket socket;
    private ObjectInputStream in = null;
    static public LinkedList<Object> lineHistory = new LinkedList<>();
    private Controller controller = null;
    private ObjectSenderThread objectSender;

    public static String userIpAddress = "";
    private int clientId;
    private int countOfClients;
    private long pingTimer;
    private LineHistoryThread lineHistoryThread;
    private CanvasImage currentCanvasImage = null;

    /**
     *
     * @param host host ip address
     * @param port host port number
     * @throws UnknownHostException if host or port is not found
     */
    public ServerListener(String host, int port) throws UnknownHostException {
        super(true);
        
        try {
            this.socket = new Socket(host, port); //establish socket
            this.socket.setTcpNoDelay(true);
            
            OutputStream outputStream = socket.getOutputStream(); // get the output stream    // create an object output stream from the output stream so we can send an object through it
            this.objectSender = new ObjectSenderThread(outputStream);   // create a DataInputStream so we can read data from it.
            new Thread(this.objectSender).start();
            Main.addThread(objectSender);
            
            InputStream inputStream = socket.getInputStream();  // get the input stream
            BufferedInputStream bf = new BufferedInputStream(inputStream);
            this.in = new ObjectInputStream(bf);
            this.userIpAddress = this.socket.getInetAddress().getHostAddress();
            
            //construct a linehistorythread
            lineHistoryThread = new LineHistoryThread(this);
            new Thread(lineHistoryThread).start();
            Main.addThread(lineHistoryThread);  

        } catch (IOException e) {
            //e.printStackTrace();
            throw new UnknownHostException();
        }
    }

    @Override
    public void run() {
        try {
            //Send a test object to server
            String testString = "Hello server. This is client";
            objectSender.addToBuffer(testString);

            Object object = null;
            String str = "";
            
            // waiting for controller setting
            while (controller == null) {
                try {
                    Thread.sleep(500);
                } 
                catch (Exception e) { }
            }
            
            OUTER:
            while ((object = in.readObject()) != null) {
                if (object instanceof DrawingObject) //Detecting most send object type fisrt
                {
                    DrawingObject drawingObj = (DrawingObject) object;
                    controller.foreignDrawOnCanvas(drawingObj);
                    lineHistoryThread.addToBuffer(new Pair("AddDrawable", object));
                } 
                else if (object instanceof String) 
                {
                    str = (String) object;
                    System.out.println("String received: " + str);
                    Pair commandPair;
                    System.out.println(str);
                    //If CLEAR then information to controller
                    switch (str) 
                    {
                        case "CLEAR":
                            commandPair = new Pair("CLEAR", null);
                            controller.clearCanvas();
                            lineHistoryThread.addToBuffer(commandPair);
                            break;
                        case "CloseSocket":
                            break OUTER;
                        case "Ping":
                            System.out.println("Ping time was " + (System.currentTimeMillis()-this.pingTimer) + "ms");
                            break;
                        case "RemoveCanvasImage":
                            this.currentCanvasImage = null;
                            controller.removeCanvasImage();
                            break;
                    }
                } else if (object instanceof Message)
                {
                    
                    Message message = (Message) object;
                    controller.messageToTextArea(message);
                    
                }
                else if (object instanceof UserArrayList) 
                {                   
                    System.out.println("Userlist received from the server.");
                    UserArrayList ual = (UserArrayList)object;
                    int i = ual.size();
                    
                    Platform.runLater(() -> {
                        controller.updateUserView(ual);
                        controller.changeCountOfClients(i);
                    });
                } 
                else if (object instanceof LinkedList)
                { //Receiving line history
                    LinkedList<Object> l = (LinkedList<Object>) object;
                    while(controller == null && alive) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (Object obj : l){
                        controller.drawLineHistory((DrawingObject)obj);
                    }
                    Pair p = new Pair("LineHistory", object);
                    lineHistoryThread.addToBuffer(p);
                }
                else if (object instanceof ArrayList)
                { //Receiving message history
                    
                    Object finalObject = object;
                    
                    new Thread(() -> {
                        //Thread waits for controller to exist
                        while (controller == null && alive && Controller.id != 0) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //Then thread loads the maximum of 30 messages to the chat
                        if (controller != null) {
                            for(Object o : (ArrayList<Object>) finalObject) {
                                Message message = (Message) o;
                                System.out.println(message.getMessage());
                                controller.messageToTextArea(message);
                            }
                        }
                        alive = false;
                    }).start();
                }
                else if (object instanceof Message)
                {                    
                    Message message = (Message) object;
                    controller.messageToTextArea(message);
                } 
                else if (object instanceof Integer) 
                {
                    Integer i = (Integer) object;                   
                    this.clientId = i;
                } 
                else if (object instanceof Pair)
                {
                    //If pair value is clear -> clear lines from the linehistory by Pair key
                    if (((Pair) object).getValue().equals("CLEAR"))
                    {
                        controller.clearCanvas();
                        int id = (int) ((Pair) object).getKey();
                        synchronized(ServerListener.lineHistory)
                        {
                            for (Object o : ServerListener.lineHistory)
                            {
                                DrawingObject d = (DrawingObject) o;
                                if (d.getId() != id)
                                {
                                    controller.drawLineHistory(d);
                                }
                            }
                        }
                        Pair p = new Pair("ClearOwnLines", id);
                        lineHistoryThread.addToBuffer(p);
                    } 
                    else if (((Pair) object).getValue().equals("UNDO"))
                    {
                        int id = (int)((Pair)((Pair) object).getKey()).getKey();
                        int undoId = (int)((Pair)((Pair) object).getKey()).getValue();
                        
                        DrawingObject lineObject;
                        //check if the user clientId and undo clientId match -> remove from the linehistory
                        controller.clearCanvas();
                        synchronized (lineHistory)
                        {
                            System.out.println(lineHistory.size());
                            for (Object o : lineHistory)
                            {                          
                                lineObject = (DrawingObject) o;
                                if (!(lineObject.getId() == id && lineObject.getUndoId() == undoId))
                                {
                                    controller.drawLineHistory((DrawingObject)o);                              
                                } 
                            }
                        }
                        //send updated linehistory to the controller                       
                        lineHistoryThread.addToBuffer(new Pair("UNDO", ((Pair) object).getKey()));
                    }      
                }   
                else if (object instanceof CanvasImage) //CanvasImage selected by user
                {
                   this.currentCanvasImage = (CanvasImage) object;    
                   if (controller != null && ((CanvasImage) object).getEncodedString() != null)
                   {
                       controller.addImageToCanvas(object);
                   }
                }                 
                else {
                    //TODO
                    System.out.println("Instance of the object is not recognized");
                }
            }
            System.out.println("ServerListener closed");
        } catch (IOException | ClassNotFoundException e) 
        {
            System.out.println("Exception OVER HERE!");
            controller.noConnectionToServer();
            
        } finally 
        {
            try 
            {
                in.close();
                socket.close();
            } catch (IOException ex) 
            {
                Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
    //Methods
    //Sends a line or erase object to the server

    
    public void sendObjectToServer(Object obj) throws IOException 
    {
        //System.out.println("ServerListener picked up a line to send to server: " + line);
        //Send the line object to server
        this.objectSender.addToBuffer(obj);
        if(obj instanceof DrawingObject)
        {
            lineHistoryThread.addToBuffer(new Pair("AddDrawable", obj));
        } else if (obj instanceof String && obj.equals("CLEAR")){
            Pair commandPair = new Pair("CLEAR", null);
            lineHistoryThread.addToBuffer(commandPair);
        }
    }   
    
  
    
    public void sendToController(){
        this.controller.setServerListener(this);
    }


    /**GETTERS AND SETTER
     * @return lineHistoryThread Thread for linehistory modifications
     */
    public LineHistoryThread getLineHistoryThread() {
        return lineHistoryThread;
    }
    
    /**
     *
     * @param c sets controller to this 
     */
    public void setController(Controller c) {
        this.controller = c;
    }
    
    
    public int getId() {
        return clientId;
    }

    /**
     *
     * @param id sets client's id number
     */
    public void setId(int id) {
        this.clientId = id;
    }

    /**
     *
     * @return counofClients the current number of the clients on the server
     */
    public int getCountOfClients() {
        return countOfClients;
    }

    /**
     *
     * @param countOfClients sets the current number of the clients on the server to the field variable named counOfClients
     */
    public void setCountOfClients(int countOfClients) {
        this.countOfClients = countOfClients;
    }

    /**
     *
     * @throws IOException if something goes wrong
     */
    public void ping() throws IOException{
        this.pingTimer = System.currentTimeMillis();
        sendObjectToServer("Ping");
    }

    /**
     *
     * @param drawingObj object to be drawn later
     */
    public void addObjectToHistory(Object drawingObj){
        synchronized(this.lineHistory)
        {
            this.lineHistory.add(drawingObj);
        }
            
    }
    
    /**
     *
     * @return lineHistory list for storing all drawing objects
     */
    public LinkedList<Object> getServerListenerLineHistory()
    {
        return this.lineHistory;
    }

    /**
     * clears the linehistory owned by this
     */
    public void clearLineHistory()
    {
        synchronized(this.lineHistory)
        {
            this.lineHistory.clear();
        }
    }

    public void setServerListenerLineHistory(LinkedList<Object> setLineHistory) {
        synchronized(this.lineHistory)
        {
            this.lineHistory.addAll(setLineHistory);
        }
    }

    /**
     * 
     * @return currentCanvasImage canvas background image which is currently in use 
     */
    public CanvasImage getCanvasImage() {
        return this.currentCanvasImage;
    }
    
}
