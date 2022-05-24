package whiteboard.client;

import java.io.Serializable;
import java.util.Base64;

/**
 * public class CanvasImage 
 * implements Serializable
 * 
 * Used for storing image data for sending image over socket
 * @author mylle
 */
public class CanvasImage implements Serializable {

    private static final long serialVersionUID = 2L;
    private int id;
    //private transient Image image = null; 
    private String encodedString = null;

    /**
     * Empty class constructor
     */
    public CanvasImage() {
    }

    /**
     *
     * @return encodedString base64 encoded image
     */
    public String getEncodedString() {
        return encodedString;
    }

    /**
     * 
     * @param fileContent imagedata 
     */
    public void setEncodedString(byte[] fileContent) {
        this.encodedString = Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Gets the user's id number
     * @return id 
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user's id number
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }
    
}
