package whiteboard.client;

import java.util.ArrayList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * DefaultIcons class maintains the information of default user icons available. 
 * @author TP
 */
public class DefaultIcons {
    
    // Paths for the default icons. 
    private static final String PATHS[] = {
        "icons/jagger-icon.png", 
        "icons/trinity-icon.png", 
        "icons/andy-icon.png", 
        "icons/girl-icon.png", 
        "icons/trump-icon.png",
        "icons/native-icon.png", 
        "icons/lenin-icon.png"
    };
    
    private static ArrayList<ImageView> icons;
    
    
    /**
     * Initializing the list for default icons. 
     */
    private static void initIcons() {
        
        // Filling the ImageView list for the default icons. 
        icons = new ArrayList<>();
        for (String PATH : PATHS) {
            icons.add(new ImageView(DefaultIcons.class.getResource(PATH).toExternalForm()));
        }
        
    }
    
    
    /**
     * Getter for the list of default icons. 
     * Initialization is made if necessary. 
     * @return iconlist
     */
    public static ArrayList<ImageView> getDefaultIcons() {
        if (icons == null)
            initIcons();
        
        return icons;
    }
    
    
    /**
     * Getter for a certain default icon. 
     * @param iconIndex icon index of the wanted icon
     * @return iconImage
     */
    public static Image getImageByIndex(int iconIndex) {
        if (icons == null)
            initIcons();
        
        if (iconIndex < 0)
            return null;
        
        ImageView iv = icons.get(iconIndex);
        Image image = iv.getImage();
        return image;
    }
    
}
