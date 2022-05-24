package whiteboard.client;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * UserArrayList is a specially named version of ArrayList. 
 * It is used for sending all the user data from the server to all the clients. 
 * @author TP
 */
public class UserArrayList extends ArrayList<User> implements Serializable { }
