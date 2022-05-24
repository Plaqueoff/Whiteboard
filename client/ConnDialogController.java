package whiteboard.client;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

/**
 * public class ConnDialogController
 * 
 * @author jukka
 */
public class ConnDialogController {
    //Attributes
    private String hostName = "localhost";
    private Integer portNumber = 4999;
    public static String username = "";

    /**
     * Class conrtuctor
     */
    public ConnDialogController() {
        openDialog();
    }

    /**
     * 
     * @return hostName name of the host
     */
    public String getHost() {
        return this.hostName;
    }

    /**
     *
     * @return portNumber port of the host
     */
    public Integer getPort() {
        return this.portNumber;
    }

    /**
     * 
     * @return username name choosed by the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * 
     * @param username name choosed by the user
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Opens the connection dialog
     */
    public void openDialog() {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Server connection");
        dialog.setHeaderText("Connect to a server");

        // Set the button types.
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, cancelButton);

        // Create the host and port labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField usernameTextField = new TextField();
        usernameTextField.setPromptText("Type your name...");
        usernameTextField.setText("USER_1");
        TextField host = new TextField();
        host.setText(hostName);
        TextField port = new TextField();
        port.setText(String.valueOf(portNumber));
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameTextField, 1, 0);
        grid.add(new Label("Host:"), 0, 1);
        grid.add(host, 1, 1);
        grid.add(new Label("Port:"), 0, 2);
        grid.add(port, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the host field by default.
        Platform.runLater(() -> host.requestFocus());

        // Convert the result to a host-port-pair when the connect button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                this.username = usernameTextField.getText();
                return new Pair<>(host.getText(), port.getText());
            } else if(dialogButton == cancelButton) {
                System.out.println("Whiteboard closed");
                System.exit(0);
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(hostPort -> {
            this.hostName = hostPort.getKey();
            this.portNumber = Integer.parseInt(hostPort.getValue());
            System.out.println("Host: " + hostPort.getKey() + ", Port: " + hostPort.getValue());
        });
    }
}
