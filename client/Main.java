package whiteboard.client;

import whiteboard.client.threads.myThread;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayList;

/**
 * public class Main 
 * extends Application
 * Main application thread
 * @author jukka
 */
public class Main extends Application {

    /**
     * @param threads list of threads in use
     * @param stage primary stage
     */
    public static ArrayList<myThread> threads = new ArrayList<>();
    private static Stage stage;

    /**
     * 
     * @param primaryStage application primary stage
     * @throws Exception if something goes wrong
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        
        stage = primaryStage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ConnectionAndProfile.fxml"));
        loader.load();
        Parent root = loader.getRoot();
        Scene scene = new Scene(root, 1210, 800);
        primaryStage.setTitle("Whiteboard");
        primaryStage.setScene(scene);
        primaryStage.show();
        ((ConnectionAndProfileController)loader.getController()).setStage(primaryStage);
        
    }
    
    //kill all threads running

    /**
     * Method for closing all threads
     */
     
    public static void closeThreads()
    {        
        for (myThread m : threads)
        {
            m.killThread();
        }
    }
    
    /**
     *
     * @param thread thread to be stored
     */
    public static void addThread(myThread thread){
        Main.threads.add(thread);
    }

    /**
     *
     * @param args launch arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
