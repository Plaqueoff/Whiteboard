package whiteboard.server;
import java.net.*;
import java.io.*;
import java.sql.SQLException;

/**
 *
 * @author jukka
 */
public class Main {
    
    static ServerActionThread sat;
    
    /**
     *
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        int port = 4999;
        ServerSocket server = null;
        //Have an array to store connected clients
        //Have an array to store received objects
        //Accept multiple clients and keep track of connected clients
        //Send array to freshly connected clients
        //Receive data from client, cast it to object and save to an array
        //Send received data to other connected clients

        System.out.println("Starting server with port: " + port);
        SQLHandler.StartConnection(); //Establish SQL connection
        SQLHandler.ConnectToDB(); //Connect to database
        SQLHandler.checkIp(myIp()); //Checking ip from database

        ClientHandler.messageHistory = SQLHandler.getMessages(); //Load last 30 messages to messagehistory clienthandler
        //ClientHandler.lineHistory = SQLHandler.getLineHistory(); //Load draw lines to linehistory in clienthandler

        SQLHandler sqlThread = new SQLHandler(); //Create an instance of the sql class
        new Thread(sqlThread).start(); //Start the runnable within the sql class
        ClientHandler.sqlThread = sqlThread;

        
        try { //Establish server socket
            server = new ServerSocket(port);
            server.setReuseAddress(true);
            //Accept connections
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected: " +
                        client.getInetAddress().getHostAddress());
                //Start thread for server concurrency handling
                sat = new ServerActionThread();
                new Thread(sat).start();
                
                ClientHandler clientSock = new ClientHandler(client, sat);
                // Background thread will handle each client separately
                new Thread(clientSock).start();

                System.out.println("Currently connected clients: " + ClientHandler.clientHandlers);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    //SQLHandler.saveLineHistory(ClientHandler.lineHistory); //Save linehistory to database before closing everything
                    server.close();
                    SQLHandler.CloseConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sat.killThread();
        }


    }
    /**
     * Find my public ip address
     * @return String
     */
    private static String myIp(){
        String ip = "";
        try {
            URL url = new URL("http://bot.whatismyipaddress.com");
            BufferedReader bd = new BufferedReader(new InputStreamReader(url.openStream()));
            ip = bd.readLine().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(ip);
        return ip;
    }
}
