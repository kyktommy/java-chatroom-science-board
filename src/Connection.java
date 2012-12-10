import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class Connection implements Runnable {
	
	User user;
	Socket socket;
	ChatServer chatServer;
	String heading;
	
	ArrayList<String> topics = new ArrayList<String>();
	int topic = -1;
	
	public Connection(User user, ChatServer chatServer) {
		this.user = user;
		this.socket = user.getSocket();
		this.chatServer = chatServer;
		this.heading = socket.getInetAddress().getHostAddress() + ":" +socket.getPort();
		
		getTopicsFromFile();
	}
	
	public void getTopicsFromFile(){
		try{
			BufferedReader br = new BufferedReader(new FileReader("topics.txt"));
			String topic = "";
			while((topic = br.readLine()) != null) {
				topics.add(topic);
			}
		} catch (IOException e) {
			
		}
	}
	
	public void addTopicToFile(String topic) {
		try{
			BufferedWriter br = new BufferedWriter(new FileWriter("topics.txt"));
			br.write(topic);
		} catch (IOException e) {
			
		}
	}

	@Override
	public void run() {
		
		try {
			//stream for receive
			for(;;) {
				InputStream is = socket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				Boolean exit = false;
				Object obj = ois.readObject();
				if( obj != null && obj instanceof Command ) {
					exit = parseCommand((Command)obj);
				}
				if(exit) {
					ois.close();
					break;
				}
			}
			
			//close 
			this.socket.close();
			this.chatServer.removeUser(user);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Boolean parseCommand(Command command) {
		Boolean exit = false;
		CMDList cmd = command.cmd;
		
		if(cmd == CMDList.GetTopics) {
			// return topics
			Command sendCmd = new Command(CMDList.ReceiveTopics);
			sendCmd.setObject(topics);
			sendCommand(sendCmd);
		}
		else if( cmd == CMDList.SelectTopic ) {
			// Join topic
			topic = (Integer)command.getObject();
			user.setTopic(topic);
			Command sendCmd = new Command(CMDList.ConfirmTopic);
			sendCommand(sendCmd);
		}
		else if( cmd == CMDList.SendMessage ) {
			String msg = (String)command.getObject();
			parseMessage(msg);
		}
		else if( cmd == CMDList.Exit ) {
			exit = true;
		}
		return exit;
	}
	
	public void parseMessage(String message) {
		// boardCast message
		chatServer.broadcast(user, message);
		// TODO: Record in File
	}
	
	public void sendCommand(Command cmd) {
		try {
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			
			// Send Command
			oos.writeObject(cmd);
			oos.flush();
		} catch (Exception ex) {
		}
	}
	
	public void println(String str) {
		System.out.println(str);
	}
}