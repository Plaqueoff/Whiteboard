package whiteboard.client;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * public class ChangeImageController
 * implements Initializable
 * 
 * Controller for the window for changing the user icon
 *
 * @author mylle, TP
 */
public class ChangeImageController implements Initializable {
    
    @FXML
    HBox iconSelectionList;
    @FXML
    ImageView selectedImageView;
    @FXML
    Button ownFileButton;
    
    private ConnectionAndProfileController capc;
    private int selectedIndex; 
    private File selectedFile;
    private ArrayList<ImageView> icons;
    
    
    /**
     * Initializes the controller class.
     * @param i selected icon index
     * @param capc ConnectionAndProfileController object
     */
    public void setup(int i, ConnectionAndProfileController capc)
    {
        this.capc = capc;
        this.selectedIndex = i;
        setIconStyle();
        
        if (i >= 0) {
            selectedImageView.setImage(icons.get(i).getImage());
            selectedFile = null;
        }
        else {
            selectedImageView.setImage(capc.getImage());
            selectedFile = capc.getImageFile();
        }
    }
    /**
     * Sets the opacity of the imageview and the style of the icon
     */
    
    private void setIconStyle(){
        
        for (ImageView iv : icons)
            iv.setStyle("-fx-opacity: 0.5");
        
        if (selectedIndex >= 0)
            icons.get(selectedIndex).setStyle("");
    }  
    
    
    EventHandler<MouseEvent> iconClicked = new EventHandler<MouseEvent>()
    {
        @Override
        public void handle(MouseEvent event) 
        {
            ImageView clickedIcon = (ImageView)event.getSource();
            selectedImageView.setImage(clickedIcon.getImage());
            selectedIndex = icons.indexOf(clickedIcon);
            selectedFile = null;
            setIconStyle();                 
        }           
    };
    
    
    EventHandler<ActionEvent> ownIcon = new EventHandler<ActionEvent>(){

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pick an image");
            fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
            Window ownerWindow = null;
            selectedFile = fileChooser.showOpenDialog(ownerWindow);
            if (selectedFile != null) {
                try {
                    selectedImageView.setImage(new Image(selectedFile.toURI().toURL().toExternalForm()));
                    selectedIndex = -1; // not a default icon
                    setIconStyle();
                } catch (MalformedURLException ex) {
                    Logger.getLogger(ChangeImageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }        
    };
    
    
    @FXML
    private void handleOkButton(ActionEvent event) {
        capc.setImage(selectedImageView.getImage(), selectedIndex);
        capc.setIconIndex(selectedIndex);
        capc.setImageFile(selectedFile);
        
        Stage stage = (Stage)iconSelectionList.getScene().getWindow();
        stage.close();
    }
    
    
    @FXML
    private void handleCancelButton(ActionEvent event) {
        Stage stage = (Stage)iconSelectionList.getScene().getWindow();
        stage.close();
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        icons = DefaultIcons.getDefaultIcons();
        iconSelectionList.setSpacing(20);
        iconSelectionList.getChildren().setAll(icons);
        
        // Setting a button handler. 
        ownFileButton.setOnAction(ownIcon);
        
        // Setting MouseEvent handler for all the default icons. 
        for (ImageView iv : icons)
            iv.setOnMouseClicked(iconClicked);
        
    }    

}
