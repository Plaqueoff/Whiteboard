/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whiteboard.client;

import java.io.Serializable;

/**
 *
 * @author ilkkaniemelainen
 * 
 * Meaning of this is to send message object to other clients. User is to be
 * defined in the main application by the user.
 * 
 */
public class Message implements Serializable {
    
    String message;
    String user;
    int id;
    
    /**
     *
     * @param msg
     * @param usr
     * @param ide
     */
    public Message(String msg, String usr, int ide){
        this.message=msg;
        this.user=usr;
        this.id = ide;
    }
    
    /**
     *
     * @param msg
     */
    public void setMessage(String msg){
        this.message=msg;
    }
    
    /**
     *
     * @param usr
     */
    public void setUser(String usr){
        this.user=usr;
    }
    
    /**
     *
     * @return
     */
    public String getMessage(){
        return this.message;
    }
    
    /**
     *
     * @return
     */
    public String getUser(){
        return this.user;
    }
    
    /**
     *
     * @return
     */
    public int getId(){
        return id;
    }
    
    /**
     *
     * @param newId
     */
    public void setId(int newId){
        this.id = newId;
    }
    
}
