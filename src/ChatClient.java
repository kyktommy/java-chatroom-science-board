import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;


public class ChatClient {
	
	// User info
	String username;
	String password;
	
	Scanner scanner = new Scanner(System.in);
	Socket socket;
	ClientReceiveThread clientreceiveThread;
	
	public ChatClient() {
		this.username = "user";
	}

	public void run() {
		promptID();
		fetchTopic();
		receiveMessage();
	}
	
	public void promptID() {
//		// Input username
//		println( "Username >" );
//		username = scanner.nextLine();
//		println( "Password >" );
//		username = scanner.nextLine();
	}
	
	public void fetchTopic() {
		println( "Welcome " + this.username );
		println( "Select topics: " );
		// Connect Server
		connectToServer("127.0.0.1");
		// Get topics
		getTopics();
	}
	
	public void messageLoop() {
		// After join a topic
		// New Thread for submit message
		Thread msgThread = new Thread(new Runnable() {
			public void run() {
				String msg = "";
				do {
					// Input 
					System.out.print("> ");
					msg = scanner.nextLine();
					if( msg.indexOf('/') != 0 ) {
						// Message
						Command cmd = new Command(CMDList.SendMessage);
						cmd.obj = msg;
						sendCommand(cmd);
					} 
					else if(msg.indexOf("/ct") == 0) {
						// Create topic
					}
					// Command
				} while(!msg.equals("/exit"));
			}
		});
		msgThread.start();
		
		// Back to select topic
		
		// OR exit
//		println("exiting programme");
//		System.exit(0);
	}
	
	/** Socket **/
	
	public void connectToServer(String ip) {
		while(socket == null) {
			try {
				socket = new Socket(ip, 9999);
			} catch (UnknownHostException ex) {
				System.err.println(ex);
				
			} catch (IOException ex) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					//System.err.println(e);
				}
			}
		}
	}

	// Command 
	
	public void sendCommand(Command cmd) {
		try {
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			
			// Write Command to send
			oos.writeObject(cmd);
			oos.flush();
			
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
	
	public void receiveMessage() {
		this.clientreceiveThread = new ClientReceiveThread(this);
		Thread thread = new Thread(clientreceiveThread);
		thread.start();
	}
	
	/* UI Flow */
	
	public void getTopics() {
		Command cmd = new Command(CMDList.GetTopics);
		sendCommand(cmd);
	}
	
	public void receiveTopics(Command cmd) {
		ArrayList<String> topics = (ArrayList<String>)cmd.getObject();
		println("--------------------------");
		println("Topics: ");
		for(String t : topics) {
			println(t);
		}
		println("--------------------------");
		selectTopic();
	}
	
	public void selectTopic() {
		System.out.print("> ");
		Command cmd = new Command(CMDList.SelectTopic);
		Integer topic = Integer.parseInt(scanner.nextLine());
		cmd.setObject(topic);
		sendCommand(cmd);
		
	}
	
	public void confirmTopic() {
		println("Joined topic");
		messageLoop();
	}
	
	public void receiveMessage(Command command) {
		String msg = (String)command.obj;
		println(msg);
	}
	
	// Helper Function
	
	public void println(String str) {
		System.out.println(str);
	}
	
	public static void main(String[] args) {
		ChatClient app = new ChatClient();
		app.run();
	}
}
