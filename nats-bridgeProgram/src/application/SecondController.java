package application;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ResourceBundle;
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
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;

public class SecondController implements Initializable{
	
	private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
	private static DeviceClient client;
	
	public String connString;
	public String subject;
	
	private String msgStr;
	
	@FXML private TextArea iothubText;
	@FXML private TextArea natsText;
	@FXML ScrollPane rightBox;
	@FXML ScrollPane leftBox;
	
	
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
		rightBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		
		leftBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
	    
        ThreadNats tn = new ThreadNats();
        ExecutorService ex = Executors.newFixedThreadPool(1);
        ex.execute(tn);

	}
	
	public void passingValues(String conStr, String sub) {
		this.connString = conStr;
		this.subject = sub;
		
	}
	
	@FXML
	private void disconnect(ActionEvent event) throws IOException {
		System.out.println("disconnected Clicked");
		
//		client.closeNow();
	}
	
	private class ThreadNats implements Runnable{
		public void run() {
			
			Connection nc;
			String server = "nats://0.0.0.0:4222";
			
			try {
				
	            System.out.printf("Trying to connect to %s, and listen to %s.\n", server, subject);
	            natsText.appendText("Trying to connect to "+ server + " and listen to " +subject);
	            

	            // Connect to NATS server
				nc = Nats.connect(server);
				try {
					client = new DeviceClient(connString, protocol);
				} catch (IllegalArgumentException | URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				client.open();
				System.out.println("Successfully Connected IoT Hub");
				Platform.runLater(()->{
					iothubText.appendText("Successfully Connected IoT Hub");
				});
				
				// Subscribe
				Subscription sub = nc.subscribe(subject);
				System.out.println("Successfully Connected and Subscribed to "+subject);
				Platform.runLater(()->{
					natsText.appendText("\nSuccessfully Connected and Subscribed to "+subject);
				});

				
				while (true){
					// Read a message
					System.out.println("Reading messages...");
					Platform.runLater(()->{
						natsText.appendText("\nReading messages...");
					});
					
					io.nats.client.Message msg = sub.nextMessage(Duration.ZERO);
					msgStr = new String(msg.getData(), StandardCharsets.UTF_8);
					
					System.out.println("Received messages from subscription : "+ msgStr);
					Platform.runLater(()->{
						natsText.appendText("\nReceived messages from subscription : "+ msgStr);
					});
					
					// Create new thread and start sending messages 
			        MessageSender sender = new MessageSender();
			        ExecutorService executor = Executors.newFixedThreadPool(1);
			        executor.execute(sender);				
				}
				
			} catch (IOException  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Print the acknowledgement received from IoT Hub for the telemetry message sent.
	private class EventCallback implements IotHubEventCallback {
	    public void execute(IotHubStatusCode status, Object context) {
	    	
	        System.out.println("IoT Hub responded to message with status: " + status.name());
	        Platform.runLater(()->{
	        	iothubText.appendText("\nIoT Hub responded to message with status: " + status.name());
			});
	        
	        
	        if (context != null) {
	            synchronized (context) {
	            	context.notify();
	            }
	        }
	    }
	}
	
	private class MessageSender implements Runnable {
		public void run() {
			try {
				Message msg = new Message(msgStr);

				System.out.println("Sending message: " + msgStr);
				Platform.runLater(()->{
					iothubText.appendText("\nSending message: " + msgStr);
				});
				
				System.out.println();

				Object lockobj = new Object();

				// Send the message.
				EventCallback callback = new EventCallback();
				client.sendEventAsync(msg, callback, lockobj);

				synchronized (lockobj) {
					lockobj.wait();
				}
			} catch (InterruptedException e) {
				System.out.println("Finished.");
			}
	    }
	}
	


}
