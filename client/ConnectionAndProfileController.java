package whiteboard.client;

import whiteboard.client.threads.myThread;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;
import sun.misc.BASE64Decoder;
import whiteboard.client.threads.ObjectSenderThread;

/**
 * FXML Controller class
 *
 * @author mylle
 */
public class ConnectionAndProfileController implements Initializable {
    private Stage stage;
    private ConnectionAndProfileController ilmastointiteippi;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private User user;
    private final String fileName = "userdata.bin";
    private String dataFolder;
    private File imageFile;
    private int iconIndex = -1;
   
    @FXML
    AnchorPane anchorPane;
    @FXML
    TextField nameTextField, hostTextField, portTextField;
    @FXML
    ColorPicker nameColorPicker;
    @FXML
    ImageView profImageView;
    @FXML
    Button changeImageButton, connectButton;
    @FXML
    CheckBox rememberMeCheckBox;
    
    private static String prevUserName = null;
    private static Image prevIcon = null;
    private static int prevIconIndex; 
    private static File prevImageFile = null;
    private static String prevColor = null;
    private static String prevHost = null;
    private static String prevPort = null;
    
    /**
     *
     * @param stage
     */
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
    /**
     *
     * @param image
     * @param i
     */
    public void setImage(Image image, int i){
        profImageView.setImage(image);
        // this.iconNumber = i;
        this.iconIndex = i;
    }
    
    /**
     *
     * @return
     */
    public Image getImage() {
        return profImageView.getImage();
    }

    /**
     *
     * @return
     */
    public int getIconIndex() {
        return iconIndex;
    }

    /**
     *
     * @param iconIndex
     */
    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }
    
    /**
     *
     */
    public void close(){
        
        // Before closing the window, let's write down the settings for the future. 
        prevIcon = this.profImageView.getImage();
        prevIconIndex = this.iconIndex;
        prevUserName = this.nameTextField.getText();
        prevImageFile = this.imageFile;
        prevColor = nameColorPicker.getValue().toString();
        prevHost = this.hostTextField.getText();
        prevPort = this.portTextField.getText();
        
        this.stage.close();
    }
    
    
    EventHandler<ActionEvent> changeProfPic = new EventHandler<ActionEvent>()
    {

        @Override
        public void handle(ActionEvent event) {
            try 
            {
                Stage changeImageStage = new Stage();
                changeImageStage.initModality(Modality.APPLICATION_MODAL);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("ChangeImage.fxml"));
                loader.load();
                ((ChangeImageController)loader.getController()).setup(iconIndex, ilmastointiteippi);
                Parent root = loader.getRoot();
                Scene scene = new Scene(root, 1000, 600);
                changeImageStage.setTitle("Whiteboard");
                changeImageStage.setScene(scene);
                changeImageStage.show();
                
            } catch (IOException ex) 
            {
                Logger.getLogger(ConnectionAndProfileController.class.getName()).log(Level.SEVERE, null, ex);
            }           
        }       
    };
    
    EventHandler<ActionEvent> connect = new EventHandler<ActionEvent>()
    {

        @Override
        public void handle(ActionEvent event) {
            ServerListener sl = null;
            boolean connection = false;
            try
            {
                sl = new ServerListener(hostTextField.getText(), Integer.valueOf(portTextField.getText()));
                new Thread(sl).start();
                connection = true;
                
            }catch (UnknownHostException ue)
            {
                // No connection to the server. Alert to the user. 
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("No connection to the server!");
                alert.setContentText("Please, check the server settings and try again.");
                alert.showAndWait();
                return;
            }
            
            if (connection)
            {
                Stage primaryStage = new Stage();
                try 
                {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("view.fxml"));
                    loader.load();
                    Parent root = loader.getRoot();
                    Scene scene = new Scene(root, 1210, 800);
                    primaryStage.setTitle("Whiteboard");
                    primaryStage.setScene(scene);
                    primaryStage.show();
                    Controller controller = loader.getController();
                                       
                    sl.setController(controller);
                    sl.sendToController();
                    
                    //set user information
                    user.setUserName(nameTextField.getText());
                    user.setHost(hostTextField.getText());
                    user.setPort(portTextField.getText());
                    user.setColor(nameColorPicker.getValue().toString());
                    user.setIconIndex(iconIndex);
                    
                    byte[] fileContent = null;
                    //convert image file to base64 string
                    if(imageFile != null)
                    {
                        try 
                        {
                            fileContent = FileUtils.readFileToByteArray(imageFile);
                            user.setEncodedString(fileContent);
                            System.out.println("added");
                            
                        } 
                        catch (IOException ex) 
                        {
                            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                        }                        
                    }

                    
                    //if user choose logging information to be saved -> write user's logging information to a file                               
                    if(rememberMeCheckBox.isSelected())
                    {
                        try 
                        {                                            
                            File file = new File (dataFolder + fileName);
                            file.createNewFile();
                            oos = new ObjectOutputStream(new FileOutputStream(file));
                            oos.writeObject(user);  
                            
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(ConnectionAndProfileController.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(ConnectionAndProfileController.class.getName()).log(Level.SEVERE, null, ex);
                        }                
                    }
                    
                    try {
                        controller.setup(user);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConnectionAndProfileController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    close();
                    
                } 
                catch (IOException ex)
                {
                    Logger.getLogger(ConnectionAndProfileController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //Close sockets
                primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
                {
                    @Override
                    public void handle(WindowEvent we) 
                    {
                        System.out.println("Closing Whiteboard.");
                        ObjectSenderThread.sendCloseRequest();
                        for (myThread thread : Main.threads)
                        {
                            thread.killThread();
                        }
                        Platform.exit();
                    }
                });
            }
        }
        
    };
    /**
     *
     * @param selectedFile
     */
    protected void setImageFile(File selectedFile) 
    {
        this.imageFile = selectedFile;
    }
    
    /**
     *
     * @return
     */
    protected File getImageFile() 
    {
        return imageFile;
    }

    //decode base64 string to image
    private Image decodeBase64ToImage(User user) {
        
        Image image = null;
        BASE64Decoder base64Decoder = new BASE64Decoder();
        ByteArrayInputStream bais;
        try {
            bais = new ByteArrayInputStream(base64Decoder.decodeBuffer(user.getEncodedString()));
            image = new Image(bais);
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
        return image;        
    }            
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        user = new User();      
        dataFolder = System.getProperty("user.home") + "\\Local Settings\\ApplicationData";
        File userDataFile = new File(dataFolder + fileName);
        //userDataFile.delete();
        
        // If the user has returned to the settings window, we choose the previous settings as default.  
        if (prevIcon != null && prevUserName != null) 
        {
            this.profImageView.setImage(prevIcon);
            this.iconIndex = prevIconIndex;
            this.nameTextField.setText(prevUserName);
            this.imageFile = prevImageFile;
            this.nameColorPicker.setValue(Color.web(prevColor));
            this.hostTextField.setText(prevHost);
            this.portTextField.setText(prevPort);
        }
        //if userdatafile exists -> read user's logging information from the file
        else if (userDataFile.exists())
        {
            try 
            {
                ois = new ObjectInputStream(new FileInputStream(userDataFile));                
                user = (User) ois.readObject();                
                ois.close();
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ConnectionAndProfileController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ConnectionAndProfileController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ConnectionAndProfileController.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Set fields of this view
            nameTextField.setText(user.getUserName());   
            hostTextField.setText(user.getHost());
            portTextField.setText(user.getPort());
            nameColorPicker.setValue(Color.web(user.getColor()));
            
            //check if usericon is one of the defaults or own
            if (user.getEncodedString() != null && user.getIconIndex() == -1)
            {
                System.out.println("encoded string found!");
                profImageView.setImage(decodeBase64ToImage(user));                
            } 
            else 
            {
                System.out.println("encoded string null");
                profImageView.setImage(DefaultIcons.getImageByIndex(user.getIconIndex()));
                this.iconIndex = user.getIconIndex();
            }
            
        } 
        else 
        {
            System.out.println("File not found, create a new one");
          
            iconIndex = 0;
            profImageView.setImage(DefaultIcons.getImageByIndex(iconIndex));
            nameColorPicker.setValue(Color.BLACK); // default color picker value
        }

        ilmastointiteippi = this;
        BackgroundImage backgroundImage = new BackgroundImage( new Image( getClass().getResource("pics/change.png").toExternalForm()), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        connectButton.setOnAction(connect);
        changeImageButton.setOnAction(changeProfPic);
        changeImageButton.setBackground(background);
    }


}
