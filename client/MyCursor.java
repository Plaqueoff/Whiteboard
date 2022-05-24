package whiteboard.client;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;


/**
 * MyCursor contains the appearance of the canvas cursor (client's own cursor). 
 * @author TP
 */
public class MyCursor {
    
    private final ImageCursor pen = new ImageCursor(new Image(getClass().getResource("pics/cursor_pen.png").toString()), 0, 32);
    private final ImageCursor eraser = new ImageCursor(new Image(getClass().getResource("pics/cursor_eraser.png").toString()), 0, 32);
    private ImageCursor cursor;
    
    
    /**
     * Constructor. The default value for the cursor is pen. 
     */
    public MyCursor() {
        this.cursor = pen;
    }
    
    
    /**
     * Getter for the cursor. 
     * @return cursor
     */
    public Cursor getCursor() {
        return this.cursor;
    }
    
    
    /**
     * Sets the cursor to eraser and returns it. 
     * @return cursor
     */
    public Cursor eraser() {
        cursor = eraser;
        return cursor;
    }
    
    
    /**
     * Sets the cursor to pen and returns it.
     * @return
     */
    public Cursor pen() {
        cursor = pen;
        return cursor;
    }
    
}
