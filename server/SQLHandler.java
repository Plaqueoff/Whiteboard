package whiteboard.server;

import whiteboard.client.Message;
import whiteboard.client.threads.myThread;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for handling connection to SQL database and for saving and retrieving chat messages
 * @author mika-matti
 */
public class SQLHandler extends myThread implements Runnable {
    //Tablenames
    private static String dbName = "R08WhiteboardDB";
    private static String servers = "servers";
    private static String messages = "messages";
    private static String drawlines = "drawlines";

    private static String cloudURL = "jdbc:mariadb://maria.westeurope.cloudapp.azure.com:3306?user=opiskelija&password=opiskelija1";
    private static String dbURL = "jdbc:mariadb://maria.westeurope.cloudapp.azure.com:3306/"+ dbName +"?user=opiskelija&password=opiskelija1";

    static Connection conn;
    static String serverIp = "";

    private LinkedBlockingQueue<Object> messagesToSave; //Keep track of what messages to save to DB
    private LinkedBlockingQueue<Object> linesToSave; //Keep track of what lines to save to DB
    private LinkedBlockingQueue<Object> linesToDelete; //Keep track of what lines to delete from DB
    private boolean clearAllLines = false; //If this is true, the thread will remove all lines from db and set this to false

    private boolean running; // used to make the thread go to sleep

    //Constructor

    /**
     *
     */
    public SQLHandler() {
        super(true);
        messagesToSave = new LinkedBlockingQueue<>();
        running = false;
    }

    //Use a thread to update database whenever there is something to update in queues, without interrupting server action
    @Override
    synchronized public void run() {
        Object obj;
        try
        {
            while(alive)
            {
                obj = messagesToSave.poll(); //If there are messages to save
                if (obj != null)
                {
                    saveMessage(obj); //Save that message
                }

                if (obj == null)
                {
                    this.running = false;
                    this.wait(); // if buffer is empty thread waits
                }

            }
        } catch (InterruptedException ex)
        {
            Logger.getLogger(SQLHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("SQLThread closed");
    }

    //Establish connection
    static void ConnectToDB() throws SQLException {
        System.out.println("Checking if database exists");
        Statement ini = conn.createStatement();

        /**USE THIS TO DROP DATABASE*/
//        String queryDel = "DROP DATABASE IF EXISTS " + dbName + ";";
//
//        if(ini.executeUpdate(queryDel) > 0) {
//            System.out.println("Deleted old database");
//        } else {
//            System.out.println("Database doesn't exist, cannot drop database");
//        }

        String query = "CREATE DATABASE IF NOT EXISTS "+ dbName +";";
        if(ini.executeUpdate(query) == 1) {
            conn = DriverManager.getConnection(dbURL);
            ini = conn.createStatement();

            query = "CREATE TABLE " + servers + " (server_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, server_ip varchar(16) NOT NULL);";
            ini.execute(query);

            query = "CREATE TABLE " + drawlines +
                    " (line_id INT NOT NULL AUTO_INCREMENT," +
                    " line_size INT NOT NULL," +
                    " line_color VARCHAR(16) NOT NULL," +
                    " line_startx INT NOT NULL," +
                    " line_starty INT NOT NULL," +
                    " line_endx INT NOT NULL," +
                    " line_endy INT NOT NULL," +
                    " line_iseraser INT," +
                    " server_id INT NOT NULL," +
                    " user_id VARCHAR(64)," +
                    " PRIMARY KEY (line_id)," +
                    " FOREIGN KEY (server_id) REFERENCES servers(server_id));";
            ini.execute(query);

            query = "CREATE TABLE " + messages +
                    " (message_id INT NOT NULL AUTO_INCREMENT," +
                    " message_content VARCHAR(300) NOT NULL," +
                    " message_username VARCHAR(50) NOT NULL," +
                    " message_timestamp DATETIME NOT NULL," +
                    " server_id INT NOT NULL," +
                    " PRIMARY KEY (message_id)," +
                    " FOREIGN KEY (server_id) REFERENCES servers(server_id));";
            ini.execute(query);

            System.out.println("Database created");
        } else {
            conn = DriverManager.getConnection(dbURL);
            System.out.println("Database exists");
        }
    }

    /**
     * Method to start connection to database
     * @return
     */
    protected static int StartConnection(){

        try {
            conn = DriverManager.getConnection(cloudURL);
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Method to close the connection to database
     */
    public static void CloseConnection() {
        try {
            Connection c = conn;
            if(c!=null) { //If exists
                c.close();
            }
            System.out.println("Database connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checking public ip address from database and add it if not exists
     * @param ip
     * @throws SQLException
     */
    protected static void checkIp(String ip) throws SQLException {
        serverIp = ip; //Save public ip to SQLHandler

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO servers (server_ip) ");
        sql.append("SELECT '");
        sql.append(ip);
        sql.append("' WHERE NOT EXISTS (");
        sql.append("SELECT server_ip ");
        sql.append("FROM servers ");
        sql.append("WHERE server_ip = '");
        sql.append(ip);
        sql.append("'); ");
        
        try {
            Statement s = conn.createStatement();
            s.execute(sql.toString());
        } catch (SQLException se){
            se.printStackTrace();
        }
    }

    /**
     * Offer message object to messagesqueue and wake up the thread
     * for handling messages in queue
     * @param object
     */
    synchronized public void addToMessageBuffer(Object object)
    {
        this.messagesToSave.offer(object);
        if (!running)
        {
            running = true;
            this.notify();// if the thread is waiting, wakes up the thread
        }
    }

    /**
     * Save message to the database
     * @param msg
     */
    synchronized public void saveMessage(Object msg) {
        System.out.println("Saving message to database");
        whiteboard.client.Message message = (whiteboard.client.Message) msg; //Cast object to right class

        String username = message.getUser();
        String content = message.getMessage();
        long now = System.currentTimeMillis(); //Establish timestamp for message
        Timestamp sqlTimestamp = new Timestamp(now);

        String query = "INSERT INTO " + messages +
                " (message_content, message_username, message_timestamp, server_id) " +
                "VALUES ('" + content + "', '" +
                username + "', '" +
                sqlTimestamp + "', " +
                "(SELECT server_id FROM " + servers + " WHERE server_ip = '" + serverIp + "')" +
                ");";
        try {
            Statement s = conn.createStatement();
            s.execute(query);
        } catch (SQLException se){
            se.printStackTrace();
        }
    }

    /**
     * When server starts, server loads 30 last messages into it's own memory and updates that from there on.
     * @return 
     */
    protected static ArrayList<Object> getMessages() {
        String server_id = "(SELECT server_id FROM " + servers + " WHERE server_ip = '" + serverIp + "')";
        ArrayList<Object> db_data = new ArrayList();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + messages + " WHERE server_id = "
                    + server_id +" ORDER BY message_id DESC LIMIT 30;");
            while(rs.next()) {
                String message = rs.getString("message_content");
                String user = rs.getString("message_username");
                int id = rs.getInt("message_id");
                Message msg = new Message(message, user, id);
                db_data.add(msg);
            }
            Collections.reverse(db_data); //Reverse order of list so indexes so messages are from oldest to newest
            return db_data;
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return db_data;
    }

}
