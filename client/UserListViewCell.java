package whiteboard.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import sun.misc.BASE64Decoder;


/**
 * UserListViewCell defines the appearance of the user data in the user listview. 
 * @author TP
 */
public class UserListViewCell extends ListCell<User> {
    
    private final int CHAR_LIMIT = 8;
    
    
    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);
        byte[] decodedBytes = null;    
        Image image = null;
        
        if (empty || user == null) 
        {
            setText(null);
            setGraphic(null);
        }         
        else 
        {
            //decode base64 string to an image
            //create a new file for the received image
            if(user.getEncodedString() != null && user.getIconIndex() == -1)  
            {
                BASE64Decoder base64Decoder = new BASE64Decoder();
                ByteArrayInputStream rocketInputStream;
                try {
                    rocketInputStream = new ByteArrayInputStream(base64Decoder.decodeBuffer(user.getEncodedString()));
                    image = new Image(rocketInputStream);
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
            else //user has chosen one of the default icons
            {
                image = DefaultIcons.getImageByIndex(user.getIconIndex());
            }

            //Check if the user is current client user, to distinguish this user from others.
            int id = user.getId();
            String userName = user.toString();
            String clippedUserName = clipString(userName);
            
            if (id == Controller.id) {
                clippedUserName = clippedUserName + "\n(you)";
            }

            //make a new image and create a new imageview for the user
            setGraphic(createImageView(image));
            setContentDisplay(ContentDisplay.TOP);
            setText(clippedUserName);
            setTextFill(Paint.valueOf(user.getColor()));
        }
        
    }
    
       
    /**
     * Creates an ImageView with correct settings. 
     * @param image input image
     * @return imageView
     */
    private ImageView createImageView(Image image) {
        ImageView iv = new ImageView(image);
        iv.setFitWidth(40);
        iv.setPreserveRatio(true);
        return iv;
    }
        
    
    /**
     * Clips a long string to multiple lines according to constant CHAR_LIMIT. 
     * @param input input string to be clipped
     * @return clippedString
     */
    private String clipString(String input) {
        
        if (input.length() <= CHAR_LIMIT)
            return input;
        
        return input.substring(0, CHAR_LIMIT) + "\n" + clipString(input.substring(CHAR_LIMIT));
        
    }
    
}
