package whiteboard.client;

import whiteboard.client.threads.DrawingThread;
import whiteboard.client.drawingObjects.FreeLine;
import whiteboard.client.drawingObjects.Line;
import whiteboard.client.drawingObjects.DrawingObject;
import whiteboard.client.drawingObjects.Circle;
import whiteboard.client.drawingObjects.Erase;
import whiteboard.client.drawingObjects.Arrow;
import whiteboard.client.drawingObjects.Square;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import java.io.*;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import static javafx.scene.input.MouseEvent.MOUSE_MOVED;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import sun.misc.BASE64Decoder;
import whiteboard.client.threads.ObjectSenderThread;

/**
 * public class Controller
 * implements Initializable
 * 
 * Controller for main fxml
 * @author mylle
 */
public class Controller implements Initializable {
    
    private DrawingThread drawingThread;
    private GraphicsContext graphicsContext;
    private GraphicsContext previewGc;
    private Scale newScale = new Scale(1, 1, 0, 0);
    private DrawingThread drawer;
    private ToggleGroup lineToggleGroup;
    private Message message;
    public static User user;
    private static ObservableList<User> userList;
    public static int id;
    private static int lastId = -1;
    private ServerListener serverListener;
    private DecimalFormat df = new DecimalFormat("#.##");
    private MyCursor cursor = new MyCursor();
    private static Text forCursor = new Text();
    private Pair<Integer, String> clearOwnLinesPair;
    private Pair<Pair<Integer, Integer>, String> undoLastLinePair;
    private static Timer timer = new Timer(true);
    private static TimerTask cursorDelay = null;
    private transient Image bgImage = null;
    private Stage stage;
    private DrawingObject[] tools = {new Erase(), new Arrow(), new FreeLine(), new Line(), new Square(), new Circle()};
    private int currentTool = 2;
    private AtomicInteger undoIndex = new AtomicInteger(0);
    private CanvasImage canvasImage = new CanvasImage();
    private static int lastMessageUser;
    private String lastUserString;

    
    //Define FXML -objects 
    @FXML
    AnchorPane rootPane;
    
    @FXML
    StackPane canvasStackPane;
    
    @FXML
    Pane cursorPane;
    
    @FXML
    ListView clientListView;
    
    @FXML
    Canvas drawingCanvas, previewCanvas;
    
    @FXML
    ColorPicker penColorPicker;
    
    @FXML
    Text penSizeText, penOpacityValueText, clientsCountText;
    
    @FXML 
    Button growPenSizeButton, witherPenSizeButton, clearCanvasButton, clearOwnLinesButton,sendButton;
    
    @FXML
    Slider penSizeSlider, penOpacitySlider;
    
    @FXML
    ToggleButton arrowToggleButton, lineToggleButton, freeLineToggleButton,
    squareToggleButton, eraserToggleButton, circleToggleButton;
    
    @FXML
    TextArea chatTextArea;
    
    @FXML
    TextField typeTextField, coordinatesText;
    
    @FXML
    MenuButton fileMenuButton;
    
    @FXML
    MenuItem importImageMenuItem, exportImageMenuItem, connectionMenuMenuItem, exitMenuItem, undoMenuItem;
   
    @FXML
    CheckBox hideCanvasImageCheckBox;
    
    /**METHOD
     * Draws objects received from the server
     * @param drawingObj DrawingObject -object
     */
    public void foreignDrawOnCanvas(DrawingObject drawingObj)
    {
        drawingThread.addToDrawer(drawingObj);
    }
    
    public void drawLineHistory(DrawingObject drawingObj)
    {
        Platform.runLater(() ->{
            drawingObj.draw(graphicsContext);
        });
    }

    /**
     * Resets the index used for undoing drawing events
     */
    public void resetUndoIndex() {
        this.undoIndex = new AtomicInteger(0);
    }

    /**
     * Clears the canvas
     */
    public void clearCanvas()
    {
        Platform.runLater(() -> {
            graphicsContext.clearRect(0, 0, 800,730);
        });
    }
    
    /**
     * Clears the secondary canvas used for preview drawing events
     */
    public void clearPreview()
    {
        previewGc.clearRect(0, 0, 800,730);
        
    }
      
    /**
     * Changes the number of the clients connected to the server
     * @param i number of the clients
     */
    public void changeCountOfClients(int i)
    {
        clientsCountText.setText(String.valueOf(i));
    }
    

    public void noConnectionToServer(){
        Platform.runLater(() -> {
            Alert kysymys = new Alert(Alert.AlertType.INFORMATION,
                        "You have lost connection", ButtonType.OK);
            kysymys.setHeaderText("Something went wrong");
            kysymys.showAndWait();

            Main.closeThreads();
            stage = (Stage)drawingCanvas.getScene().getWindow();
            stage.close();
            //load a new connection menu
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("ConnectionAndProfile.fxml"));
            try {
                loader.load();
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            Parent root = loader.getRoot();
            Scene scene = new Scene(root, 1200, 800);
            Stage primaryStage = new Stage();
            primaryStage.setTitle("Whiteboard");
            primaryStage.setScene(scene);
            primaryStage.show();
            ((ConnectionAndProfileController)loader.getController()).setStage(primaryStage);
        });
    }
    
    /**
     * Displays received message to chat
     * @param m message
     */
    
    public void messageToTextArea(Message m)
    {
        if(m.getId()==lastMessageUser){
            if(m.getId()==Controller.id)
            {   
                String chatMessage =m.getMessage()+"\n";
                chatTextArea.appendText(chatMessage);
                typeTextField.clear();
            } else 
            {
                String chatMessage = m.getMessage() + "\n";
                chatTextArea.appendText(chatMessage);          
            }            
            
        } else {
            if(m.getId()==Controller.id)
            {   
                //If the sender are the client, "you" text appears    
                String chatMessage = "\n"+ m.getUser()+ "(you): \n"  + m.getMessage() + "\n";
                chatTextArea.appendText(chatMessage);
                typeTextField.clear();
            } else 
            {
                String chatMessage ="\n"+ m.getUser()+ ": \n"  + m.getMessage() +"\n";
                chatTextArea.appendText(chatMessage);          
            }
        }
        
        lastMessageUser=m.getId();
        lastUserString=m.getUser();
    }

    /**
     * Brings serverlistener to this class
     * @param s serverListener
     */
    public void setServerListener(ServerListener s) {
        serverListener = s;
    }

    /**
     * Sets the current tool choosed by the user
     */
    public void setTool(){ // Chooses the right tool
        if (lineToggleGroup.getSelectedToggle().equals(eraserToggleButton))
        {
            this.currentTool = 0;
        } 
        else if (lineToggleGroup.getSelectedToggle().equals(arrowToggleButton))
        {
            this.currentTool = 1;
        } 
        else if (lineToggleGroup.getSelectedToggle().equals(freeLineToggleButton))
        {
            this.currentTool = 2;
        } 
        else if (lineToggleGroup.getSelectedToggle().equals(lineToggleButton))
        {
            this.currentTool = 3;
        } 
        else if (lineToggleGroup.getSelectedToggle().equals(squareToggleButton))
        {
            this.currentTool = 4;
        }
        else if (lineToggleGroup.getSelectedToggle().equals(circleToggleButton))
        {
            this.currentTool = 5;
        }
        else 
        {
            System.out.println("Something went wrong");
        }
    }

    /**
     * Undo the previous drawing action and sends the action to the server
     */
    public void undo(){ //Undoes the previously drawn line
        if (undoIndex.get() > 0)
        {
            int value = undoIndex.decrementAndGet();
            undoLastLinePair = new Pair(new Pair(Controller.id, value), "UNDO");
            try {
                serverListener.sendObjectToServer(undoLastLinePair);
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**EVENT HANDLERS*/
    //Defines an eventhandler for clicking the mouse on the canvas
    EventHandler<MouseEvent> mouseClick = new EventHandler<MouseEvent> ()
    {

        @Override
        public void handle(MouseEvent event) 
        {
            tools[currentTool].setId(id);
            tools[currentTool].setUndoId(undoIndex.get());
            tools[currentTool].setOpacity(penOpacitySlider.getValue());
            tools[currentTool].setLineColor(penColorPicker.getValue().toString());
            tools[currentTool].setSize(Integer.parseInt(penSizeText.getText()));
            tools[currentTool].setStartCoords(event.getX(), event.getY());
            
            if (currentTool == 0) // Erase is the only object drawn at the start of clicking rather than at the end
            {
                tools[currentTool].draw(graphicsContext);
                try { // Send drawn object to server
                    serverListener.sendObjectToServer(tools[currentTool]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                //Resets tool
                tools[currentTool] = (DrawingObject)tools[currentTool].renew();
                tools[currentTool].setStartCoords(event.getX(), event.getY());
                tools[currentTool].setId(id);
                tools[currentTool].setUndoId(undoIndex.get());
                tools[currentTool].setSize(Integer.parseInt(penSizeText.getText()));
            }
        }           
    };
    
    //Defines an eventlistener for dragging the mouse on the canvas
    EventHandler<MouseEvent> mouseDragged = new EventHandler<MouseEvent>() {
        
        @Override
        public void handle(MouseEvent event) 
        {
            if (tools[currentTool].isDragAffected())
            {
                boolean shouldBeDrawn = !tools[currentTool].setStartCoords(event.getX(), event.getY());
                if (shouldBeDrawn) //Checks if polyline pointarray is full
                {
                    tools[currentTool].draw(graphicsContext);                    
                    try { // Send drawn object to server before it is reset
                        serverListener.sendObjectToServer(tools[currentTool]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    //Resets tool
                    tools[currentTool] = (DrawingObject)tools[currentTool].renew();
                    tools[currentTool].setStartCoords(event.getX(), event.getY());
                    tools[currentTool].setId(id);
                    tools[currentTool].setUndoId(undoIndex.get());
                    tools[currentTool].setOpacity(penOpacitySlider.getValue());
                    tools[currentTool].setLineColor(penColorPicker.getValue().toString());
                    tools[currentTool].setSize(Integer.parseInt(penSizeText.getText()));
                }
            }
            else 
            {
                clearPreview();
                tools[currentTool].setEndCoords(event.getX(), event.getY());
                tools[currentTool].setOpacity(0.1);
                tools[currentTool].draw(previewGc);
            }
            
            
        //shows mouse coordinates on canvas    
        double x = Math.round(event.getX());
        double y = Math.round(event.getY());
        
        
        String coordinates = Double.toString(x) + " , " + Double.toString(y);
        

        coordinatesText.setText(coordinates);                                               
        }
    };
    
    //Defines an eventhandler for releasing the mouse
    EventHandler<MouseEvent> mouseReleased = new EventHandler<MouseEvent>()
    {
        
        @Override
        public void handle(MouseEvent event)
        {
            clearPreview();
            tools[currentTool].setEndCoords(event.getX(), event.getY());
            tools[currentTool].setOpacity(penOpacitySlider.getValue());
            tools[currentTool].draw(graphicsContext);
            undoIndex.incrementAndGet();
            
            try { // Send drawn object to server before it is reset
                serverListener.sendObjectToServer(tools[currentTool]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            tools[currentTool] = (DrawingObject)tools[currentTool].renew();
        } 
    };
    
    EventHandler<ActionEvent> toolsButtonClicked = new EventHandler<ActionEvent>() 
    {
        @Override
        public void handle(ActionEvent event)
        {
            Button b = (Button) event.getSource();
                
            if(b == growPenSizeButton)
            {
                if(Integer.valueOf(penSizeText.getText())==penSizeSlider.getMax())
                {
                    return;
                }
                penSizeText.setText("" + (Integer.valueOf(penSizeText.getText()) + 1));
                
            } 
            else if(b == witherPenSizeButton)
            {
                
                if(Integer.valueOf(penSizeText.getText())==penSizeSlider.getMin())
                {
                    return;
                }
                penSizeText.setText("" + (Integer.valueOf(penSizeText.getText()) - 1));
            } 
            else if(b == clearCanvasButton)
            {
                //clear button, confirmation alert
                Alert kysymys = new Alert(Alert.AlertType.CONFIRMATION,
                        "Do you really want to clear canvas?", ButtonType.OK, ButtonType.CANCEL);
                kysymys.setHeaderText("Clear canvas");
                kysymys.showAndWait();
                if (kysymys.getResult() == ButtonType.OK)
                {
                    resetUndoIndex();
                    clearCanvas();
                    try 
                    {
                        serverListener.sendObjectToServer("CLEAR");
                    } catch (IOException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } 
            else if (b == clearOwnLinesButton)
            {
                Alert kysymys = new Alert(Alert.AlertType.CONFIRMATION, 
                        "Do you really want to delete your own lines?", ButtonType.OK, ButtonType.CANCEL);
                kysymys.setHeaderText("Clear own lines");
                kysymys.showAndWait();
                if (kysymys.getResult() == ButtonType.OK)
                {
                    resetUndoIndex();
                    try 
                    {
                        serverListener.sendObjectToServer(clearOwnLinesPair);
                    } catch (IOException ex) 
                    {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                
            }
            
            else if (b == sendButton)//Sends message object to server-constructors to be changed
            {
                if(!(typeTextField.getText().isEmpty()))
                {
                    message = new Message(typeTextField.getText(), user.getUserName(), user.getId());
                    //messageToTextArea(message);
                try 
                {
                    serverListener.sendObjectToServer(message);
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
                }
                //chatTextArea.setText(typeTextField.getText());
            }           
            penSizeSlider.setValue(Integer.valueOf(penSizeText.getText()));
        }
    };
    
    //Eventhandler for enter when typing on chat
    EventHandler<KeyEvent> chatEnter = new EventHandler<KeyEvent>() 
    {
        
        public void handle(KeyEvent enter) 
        {
            
            if(typeTextField.getText() == null ||typeTextField.getText().trim().isEmpty())
            {
                return;
            }
            
            if(enter.getCode().equals(KeyCode.ENTER))
            {
            
                message = new Message(typeTextField.getText(), user.getUserName(),
                user.getId());
                //messageToTextArea(message);
                try 
                {
                    serverListener.sendObjectToServer(message);
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            
            }
        
        }
    };
    
    
    EventHandler<ActionEvent> lineToggle = new EventHandler<ActionEvent>()
    {
        @Override
        public void handle(ActionEvent event){
            ToggleButton x = (ToggleButton)event.getSource();
            x.setSelected(true);
            x.setTextFill(Color.WHITE);
            setTool();
            if(x == eraserToggleButton){
                //penToggleButton.setTextFill(Color.WHITE);
                cursorPane.setCursor(cursor.eraser());
            } else {
                //eraserToggleButton.setTextFill(Color.BLACK);
                cursorPane.setCursor(cursor.pen());                
            }
        }
    };
    
    //Defines a listener for changing pen's size
    ChangeListener<Number> penSizeDrag = new ChangeListener<Number>() 
    {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) 
        {
            int size = newValue.intValue();
            penSizeText.setText(Integer.toString(size));
            
        }
    };

    //Defines a listener for resizing the view
    ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
        
        double xScale = rootPane.getWidth()/1210;
        double yScale = rootPane.getHeight()/800;
        newScale.setX(xScale);
        newScale.setY(yScale);
        newScale.setPivotX(0);
        newScale.setPivotY(0);
        rootPane.getTransforms().setAll(newScale);
        
    };

    //Defines a listener for changing pen's opacity
    ChangeListener<Number> penToolsOpacityListener = new ChangeListener<Number>() 
    {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) 
        {           
            double d = (double) newValue;
            penOpacityValueText.setText(df.format(d));         
        }           
    };
    
    /**
     * Updates the users list 
     * @param ual list of users on the server
     */
    public void updateUserView(UserArrayList ual) {
        userList.setAll(ual);
    }
    
    /**
     * Sets the foreing cursor which refers to the user who is drawing currently
     * @param uid user id number
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public static void setForeignCursor(int uid, double x, double y) {

        if (uid == id) {  // not a foreign id
            forCursor.setVisible(false);
            return;
        }

        if (uid == lastId) { // no need for searching
            forCursor.relocate(x, y);
            forCursor.setVisible(true);
            setCursorDelay();
            return;
        }
        
        for (User u : userList) {
            if (u.getId() == uid) {
                lastId = uid;
                forCursor.setText(u.getUserName());
                forCursor.setFill(Paint.valueOf(u.getColor()));
                forCursor.relocate(x, y);
                forCursor.setVisible(true);
                setCursorDelay();
                return;
            }
        }
        
        // unknown uid
        forCursor.setVisible(false);
    }
    
    /**
     * Defines the delay for removing foreing cursor from the canvas after drawing event 
     */
    private static void setCursorDelay() {
        
        if (cursorDelay != null)
            cursorDelay.cancel();
        
        cursorDelay = new TimerTask() {
            public void run() {
                forCursor.setVisible(false);
            }
        };
        
        long delay = 1000L;
        timer.schedule(cursorDelay, delay);
    }
    
    //Handler for file menuitems
    EventHandler<ActionEvent> fileMenuItemClicked = new EventHandler<ActionEvent>() 
    {
        @Override
        public void handle(ActionEvent event) 
        {
            MenuItem m = (MenuItem) event.getSource();
            if (m == importImageMenuItem)//if user wants to import image
            {
                FileChooser fc = new FileChooser();
                fc.setTitle("Open image file");
                fc.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.png", "*.jpg"));
                Window ownerWindow = null;
                File selectedFile = fc.showOpenDialog(ownerWindow);      
                String filePath = "";
                //if user did choose a file 
                if (selectedFile != null)
                {
                    try 
                    {
                        filePath = selectedFile.toURI().toURL().toExternalForm();
                    } catch (MalformedURLException ex) 
                    {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    byte[] fileContent = null;
                    //convert image file to base64 string
                    try 
                    {
                        fileContent = FileUtils.readFileToByteArray(selectedFile);
                    } catch (IOException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if (!hideCanvasImageCheckBox.isSelected())
                    {
                        //greate a background image from selected image file for the drawing stackpane
                        bgImage = new Image(filePath, canvasStackPane.getWidth(), canvasStackPane.getHeight(), false, true);
                        BackgroundImage selectedImage = new BackgroundImage(bgImage,
                                                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                                                        BackgroundSize.DEFAULT);
                        canvasStackPane.setBackground(new Background(selectedImage));                              
                    }
                    
                    //set image data to the canvasimage object
                    if (canvasImage != null)
                    {
                        canvasImage.setId(user.getId());
                        canvasImage.setEncodedString(fileContent);
                        
                    }
                    
                    try {
                        //Send canvas image to server
                        serverListener.sendObjectToServer(canvasImage);
                    } catch (IOException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            } else if (m == exportImageMenuItem) //user wants to export canvas as an image
            {               
                FileChooser fileChooser = new FileChooser();

                //Set extension filter for text files
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.png)", "*.png");
                fileChooser.getExtensionFilters().add(extFilter);
                Window ownerWindow = null;

                //Show save file dialog
                File outputFile = fileChooser.showSaveDialog(ownerWindow);
                
                if (outputFile != null)//if user did choose a file
                {
                    //try to take a snapshot and write it as an image
                    try 
                    {
                        SnapshotParameters parameters = new SnapshotParameters();
                        WritableImage wi = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
                        WritableImage snapshot = drawingCanvas.snapshot(new SnapshotParameters(), wi);
                        
                        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", outputFile);
                        System.out.println("Snapshot created");
                        
                        //Give user a confirmation
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Image saved successfully!");
                        alert.show();
                        
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }                    
                }

            } else if (m == connectionMenuMenuItem) //user wants to get back to the connection and profile menu
            {
                //send a "close socket" -request to the server, close the window and open a new connection menu
                try 
                {
                    id = 0;
                    serverListener.sendObjectToServer("CloseSocket");
                    Main.closeThreads();
                    ObjectSenderThread.sendCloseRequest();
                    stage = (Stage)drawingCanvas.getScene().getWindow();
                    stage.close();
                    //load a new connection menu
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("ConnectionAndProfile.fxml"));
                    loader.load();
                    Parent root = loader.getRoot();
                    Scene scene = new Scene(root, 1200, 800);
                    Stage primaryStage = new Stage();
                    primaryStage.setTitle("Whiteboard");
                    primaryStage.setScene(scene);
                    primaryStage.show();
                    ((ConnectionAndProfileController)loader.getController()).setStage(primaryStage);
                
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }                //TODO
                
            } else if (m == exitMenuItem)//user wants to exit
            {
                //send a "close socket" -request to the server and close the window
                try {
                    id = 0;
                    serverListener.sendObjectToServer("CloseSocket");
                    Main.closeThreads();
                    stage = (Stage)drawingCanvas.getScene().getWindow();
                    stage.close();                    
                    Platform.exit();
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };
    //Handler for edit menuitems
    EventHandler<ActionEvent> editMenuItemClicked = new EventHandler<ActionEvent>()
    {

        @Override
        public void handle(ActionEvent event) {
            MenuItem m = (MenuItem)event.getSource();
            
            if (m == undoMenuItem){
                undo();
            }
        }
    };
    
    //Displays mouse coordinates in canvas on coordinatesText-textArea
    
    EventHandler<MouseEvent> mouseMoved = new EventHandler<MouseEvent>(){
    @Override
    public void handle(MouseEvent event){
        double x = Math.round(event.getX());
        double y = Math.round(event.getY());
        String coordinates = Double.toString(x) + " , " + Double.toString(y);
        
        coordinatesText.setText(coordinates);
        coordinatesText.setStyle("-fx-text-fill: white; -fx-background-color:  #444554;");
        
        }
    };
    
    //checkbox listener for hiding canvas background image 
    EventHandler<ActionEvent> hideCanvasImageEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            hideCanvasImageCheckBox = (CheckBox) event.getSource();
                if (hideCanvasImageCheckBox.isSelected())
                {
                    canvasStackPane.setBackground(Background.EMPTY);
                } 
                else
                {
                    addImageToCanvas(null);
                    System.out.println("image not hidded!");
                }                
        }
    };
    
    //add image reveived from server to canvas stack

    /**
     * Adds canvas background image
     * @param object image
     */
    public void addImageToCanvas(Object object) {
        
        if (object != null)
        {
            this.canvasImage = (CanvasImage) object;
            System.out.println("JOJOJOJOJOJO");
        }
        
        if (!hideCanvasImageCheckBox.isSelected())
        {
            //make image from base64 string
            Image bgImage = null;
            BASE64Decoder base64Decoder = new BASE64Decoder();
            ByteArrayInputStream bais;
            try {
                bais = new ByteArrayInputStream(base64Decoder.decodeBuffer(this.canvasImage.getEncodedString()));
                bgImage = new Image(bais, canvasStackPane.getWidth(), canvasStackPane.getHeight(), false, true);
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            //greate a background image from selected image file for the drawing stackpane
            BackgroundImage receivedImage = new BackgroundImage(bgImage,
                                            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                                            BackgroundSize.DEFAULT);
            canvasStackPane.setBackground(new Background(receivedImage));                
        }
    }    
    
    /**
     * Hides the canvas background image
     */
    public void removeCanvasImage() {
        canvasStackPane.setBackground(Background.EMPTY);     
    }    
    
    //Ctrl + z keycombination
    
    final KeyCombination keyComb1 = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
    
 
    
    
    //   

    /**
     * Sets up the rest of the controller which cant be setted in the initialize method
     * @param u user
     * @throws InterruptedException if something goes wrong
     */
    public void setup(User u) throws InterruptedException
    {
        previewGc = previewCanvas.getGraphicsContext2D();
        previewGc.setLineJoin(StrokeLineJoin.ROUND);
        previewGc.setLineCap(StrokeLineCap.ROUND);
        CanvasImage ci = serverListener.getCanvasImage();
        System.out.println(this.canvasImage.getEncodedString());
        if (ci != null && ci.getEncodedString() != null)
        {
            addImageToCanvas(ci);
            System.out.println("setup canvasimage");
            this.canvasImage = ci;
        }
        //Draw line history
        //serverListener.getLineHistory();
        
        //Setting user information
        while(Controller.id == 0)
        {
            Thread.sleep(100);
            Controller.id = serverListener.getId();
        }
        System.out.println(Controller.id);
        user = u;
        user.setId(Controller.id);
        
        try {
            serverListener.sendObjectToServer(user);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
         
        clearOwnLinesPair = new Pair(Controller.id, "CLEAR");     
        int countOfClients = serverListener.getCountOfClients();
        changeCountOfClients(countOfClients);
        // Setting foreign cursor object for the cursor Pane.
        forCursor.setStyle("-fx-font: 15px Tahoma; -fx-fill: blue; "
                        + "-fx-stroke: white; -fx-stroke-type: outside; fx-stroke-width: 2;");
        cursorPane.getChildren().add(forCursor);
        drawingThread.addToDrawer(new Line(0,0,1,1,0));
        
        try {
            serverListener.ping();
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //CTRL + Z shortcut for undo
        rootPane.getScene().addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() 
        {    
            @Override
            public void handle(KeyEvent event) {
                if (keyComb1.match(event)) {
                    undo();
                }
            }
        });     
    }    

    /**
     * Method for initializing the controller
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known
     * @param resources The resources used to localize the root object, or null if the root object was not localized
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) 
    {                
        penColorPicker.setStyle("-fx-color-label-visible: false ;");
        penColorPicker.setValue(Color.BLACK);
        
        //Luodaan piirtotyökalu, jolle annetaan drawhandler -olion arvot
        graphicsContext = drawingCanvas.getGraphicsContext2D();
        //previewGc = previewCanvas.getGraphicsContext2D();
        
        graphicsContext.setLineJoin(StrokeLineJoin.ROUND);
        graphicsContext.setLineCap(StrokeLineCap.ROUND);
        
        //add eventlisteners to canvas
        cursorPane.addEventHandler(MOUSE_PRESSED, mouseClick);
        cursorPane.addEventHandler(MOUSE_DRAGGED, mouseDragged);
        cursorPane.addEventHandler(MOUSE_RELEASED, mouseReleased);
        cursorPane.addEventHandler(MOUSE_MOVED, mouseMoved);
        
        //set casvas cursor
        cursorPane.setCursor(cursor.pen());
        
        //Toolbar eventlisteners
        growPenSizeButton.setOnAction(toolsButtonClicked);
        witherPenSizeButton.setOnAction(toolsButtonClicked);
        clearCanvasButton.setOnAction(toolsButtonClicked);
        //penToggleButton.setOnAction(penEraserToggle);
        eraserToggleButton.setOnAction(lineToggle);
        penOpacitySlider.valueProperty().addListener(penToolsOpacityListener);
        penSizeSlider.valueProperty().addListener(penSizeDrag);
        clearOwnLinesButton.setOnAction(toolsButtonClicked);
        
        //hide backgroundimage listner
        hideCanvasImageCheckBox.setOnAction(hideCanvasImageEvent);
        
        //Line eventlisteners
        arrowToggleButton.setOnAction(lineToggle);
        lineToggleButton.setOnAction(lineToggle);
        freeLineToggleButton.setOnAction(lineToggle);
        squareToggleButton.setOnAction(lineToggle);
        circleToggleButton.setOnAction(lineToggle);

        //Window size change listeners
        rootPane.heightProperty().addListener(stageSizeListener);
        rootPane.widthProperty().addListener(stageSizeListener);
        
        //Togglebutton initialization
        
        //Linetogglebuttons initialization
        lineToggleGroup = new ToggleGroup();
        eraserToggleButton.setToggleGroup(lineToggleGroup);        
        freeLineToggleButton.setToggleGroup(lineToggleGroup);
        lineToggleButton.setToggleGroup(lineToggleGroup);
        arrowToggleButton.setToggleGroup(lineToggleGroup);
        squareToggleButton.setToggleGroup(lineToggleGroup);
        circleToggleButton.setToggleGroup(lineToggleGroup);
        
        lineToggleGroup.selectToggle(freeLineToggleButton);        
        
        //Chat listeners
        sendButton.setOnAction(toolsButtonClicked);
        typeTextField.setOnKeyPressed(chatEnter);
        
        //ListView for users
        userList = FXCollections.observableArrayList();
        clientListView.setItems(userList);
        clientListView.setCellFactory(cf -> new UserListViewCell());

        
        //set childrens to filemenubutton and set on action
        importImageMenuItem = new MenuItem("Import image");
        exportImageMenuItem = new MenuItem("Export Image");
        connectionMenuMenuItem = new MenuItem("Exit to connection menu");
        exitMenuItem = new MenuItem("Exit");
        fileMenuButton.getItems().addAll(importImageMenuItem, exportImageMenuItem, connectionMenuMenuItem, exitMenuItem);
      
        importImageMenuItem.setOnAction(fileMenuItemClicked);
        exportImageMenuItem.setOnAction(fileMenuItemClicked);
        connectionMenuMenuItem.setOnAction(fileMenuItemClicked);
        exitMenuItem.setOnAction(fileMenuItemClicked);
        undoMenuItem.setOnAction(editMenuItemClicked);
        
        
        drawingThread = new DrawingThread(graphicsContext); //Opens drawing thread
        new Thread(drawingThread).start();
        Main.addThread(drawingThread);
        
        
    }

}
