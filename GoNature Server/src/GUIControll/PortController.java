package GUIControll;
import Server.ServerUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
public class PortController {
	    @FXML
	    private TextField portNum;
	    @FXML
	    private Button SubButton;
	    
	    private String getport() {
			return portNum.getText();			
		}
	    @FXML
	    void whenSubmitPort(ActionEvent event) {
	    	String p;
			
			p=getport();
			if(p.trim().isEmpty()) {
				System.out.println("You must enter a port number");
						
			}
			else
			{
				((Node)event.getSource()).getScene().getWindow().hide(); //hiding primary window
				
				Stage primaryStage = new Stage();
				FXMLLoader loader = new FXMLLoader();
				ServerUI.runServer(p);
			}
	    	
	    }
	    public void start(Stage primaryStage) throws Exception {	
			Parent root = FXMLLoader.load(getClass().getResource("loginPort1.fxml"));
			Image icon = new Image(getClass().getResourceAsStream("ConnectionIcon.png"));
			primaryStage.getIcons().add(icon);
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("loginPort.css").toExternalForm());
			primaryStage.setTitle("Port");
			primaryStage.setScene(scene);
			primaryStage.show();		
		}

}
