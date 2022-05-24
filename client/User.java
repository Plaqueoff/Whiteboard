package whiteboard.client;

import java.io.Serializable;
import java.util.Base64;


/**
 * User defines the object for user properties object. 
 * @author TP
 */
public final class User implements Serializable {
    
    private int id;
    private int iconIndex = 0;
    private String userName;
    private String host;
    private String port;
    private String color;
    private static final long serialVersionUID = 1L;
    private String encodedString = null;
    

    /**
     * Constructor which sets a default user name. 
     */
    public User() {
        this.userName = "John Doe";
    }

    
    /**
     * Getter for iconIndex. 
     * @return iconIndex
     */
    public int getIconIndex() {
        return iconIndex;
    }

    
    /**
     * Setter for iconIndex. 
     * @param iconIndex index number for the default icon
     */
    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }
    
    
    /**
     * Setter for user id. 
     * @param id user id number
     */
    public void setId(int id) {
        this.id = id;
    }

    
    /**
     * Getter for user color property. 
     * @return color
     */
    public String getColor() {
        return color;
    }

    
    /**
     * Setter for user color property. 
     * @param color color value as a string
     */
    public void setColor(String color) {
        this.color = color;
    }
    
    
    /**
     * Getter for host property. 
     * @return host
     */
    public String getHost() {
        return host;
    }

    
    /**
     * Setter for host property. 
     * @param host host IP address as a string
     */
    public void setHost(String host) {
        this.host = host;
    }

    
    /**
     * Getter for port number. 
     * @return port
     */
    public String getPort() {
        return port;
    }

    
    /**
     * Setter for port number. 
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }
    
    
    /**
     * Getter for user id. 
     * @return id
     */
    public int getId() {
        return id;
    }
    

    /**
     * Setter for user name. 
     * @param name user name
     */
    public void setUserName(String name) {
        if (name != null && name.length() > 0)
            this.userName = name;
    }
    

    /**
     * Getter for user name. 
     * @return userName
     */
    public String getUserName() {
        return userName;
    }

    
    @Override
    public String toString() {
        return userName;
    }
    
    
    /**
     * Getter for encodedString containing user icon. 
     * @return encodedString
     */
    public String getEncodedString() {
        return encodedString;
    }

    
    /**
     * Setter for encodedString containinf user icon. 
     * @param fileContent
     */
    public void setEncodedString(byte[] fileContent) {
        this.encodedString = Base64.getEncoder().encodeToString(fileContent);
    }
    
    
    @Override
    public boolean equals(Object object) {
        
        if (object == null)
            return false;
        
        if (object instanceof User) {
            if (this.id == ((User) object).getId())  // matching id
                if (this.userName.equals(((User) object).getUserName()))  // matching username
                    return true;
        }
        
        return false;
    }

}
