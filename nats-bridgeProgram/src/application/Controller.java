package application;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Subscription;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;

public class Controller {
	
	@FXML Button connect;
	
	@FXML private TextField inputConstr;
	@FXML private TextField inputSub;
	
	@FXML
	private void nextPage(ActionEvent event) throws IOException {
		System.out.println("clicked");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("secondPage.fxml"));
		Parent root = (Parent) loader.load();
		SecondController secondcontroller = loader.getController();
		secondcontroller.passingValues(inputConstr.getText(), inputSub.getText());
		
		Scene secondScene = new Scene(root);
		Stage secondStage = (Stage) connect.getScene().getWindow();
		secondStage.setScene(secondScene);
		secondStage.show();

	}
	
	
}
