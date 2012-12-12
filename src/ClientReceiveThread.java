import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * class: client receive thread
 * when message come from server, display message to GUI
 */

public class ClientReceiveThread implements Runnable{
	
	ChatClient chatClient;
	
	public ClientReceiveThread(ChatClient chatClient) {
		this.chatClient = chatClient;
	}
	
	public void run() {
		try {
			for(;;) {
				ObjectInputStream ois = new ObjectInputStream(chatClient.socket.getInputStream());
				Object obj = ois.readObject();
				if(obj == null) continue;
				if(obj instanceof Command) {
					parseCommand((Command)obj);
				}
			}
		} catch(IOException ex){
			System.err.println("Read Object: " + ex);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void parseCommand(Command command) {
		CMDList cmd = command.cmd;
		if( cmd == CMDList.AuthConfirmation) {
			chatClient.receiveLoginConfirm(command);
		}
		else if( cmd == CMDList.ReceiveTopics ) {
			chatClient.receiveTopics(command);
		}
		else if (cmd == CMDList.ConfirmTopic) {
			chatClient.confirmTopic();
		}
		else if (cmd == CMDList.ReceiveMessage) {
			chatClient.receiveMessage(command);
		}
		else if( cmd == CMDList.Exit ) {
			
		}
	}
}
