import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Connection implements Runnable {
	
	// Constants
	public final String TOPIC_FILE = "topics.txt";
	public final String USER_FILE = "users.txt";
	
	// Variable
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
			BufferedReader br = new BufferedReader(new FileReader(TOPIC_FILE));
			String topic = "";
			while((topic = br.readLine()) != null) {
				topics.add(topic);
			}
		} catch (IOException e) {
			
		}
	}
	
	public void addTopicToFile(String topic) {
		if(topics.contains(topic)) return;
		try{
			BufferedWriter br = new BufferedWriter(new FileWriter(TOPIC_FILE, true));
			br.write(topic);
			br.newLine();
			br.close();
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
		
		if(cmd == CMDList.SendUsernameAndPassword) {
			// auth
			ArrayList<String> info = (ArrayList<String>)command.getObject();
			String username = info.get(0);
			String password = info.get(1);
			
			Boolean success = auth(username, password);
			Command sendCmd = new Command(CMDList.AuthConfirmation);
			sendCmd.setObject(success);
			sendCommand(sendCmd);
		}
		else if(cmd == CMDList.GetTopics) {
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
			ArrayList<String> comments = getAllCommentsFromTopic(topic);
			sendCmd.setObject(comments);
			sendCommand(sendCmd);
		}
		else if( cmd == CMDList.SendMessage ) {
			String msg = (String)command.getObject();
			parseMessage(msg);
		}
		else if( cmd == CMDList.CreateTopic) {
			String topic = (String)command.getObject();
			String msg = "";
			if(! user.getCreatedTopic() ) {
				// create topic
				addTopicToFile(topic);
				user.setCreatedTopic(true);
				changeUserCreatedTopic();
				msg = "Successful created";
			} else {
				msg = "you can only create one topic";
			}
			Command sendCmd = new Command(CMDList.ReceiveMessage);
			sendCmd.setObject(msg);
			sendCommand(sendCmd);
		}
		else if( cmd == CMDList.Exit ) {
			exit = true;
		}
		return exit;
	}
	
	private ArrayList<String> getAllCommentsFromTopic(int t) {
		File file = new File(t + ".txt");
		if(!file.exists()) return null;
		
		ArrayList<String> comments = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(t + ".txt"));
			String line = null;
			while((line = reader.readLine()) != null) {
				comments.add(line);
			}
			reader.close();
		} catch(Exception e) {
			
		}
		return comments;
	}

	private void changeUserCreatedTopic() {
		String line = null;
		ArrayList<String> users = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(USER_FILE));
			while((line = reader.readLine()) != null) {
				String[] info = line.split(" ");
				if( user.getName().equals(info[0].trim()) ) {
					line = info[0] + " " + info[1] + " true";
				}
				users.add(line); // add all line
			}
			reader.close();
		}catch(Exception e) {
			
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE));
			for(String user : users) {
				writer.write(user);
				writer.newLine();
			}
			writer.close();
		}catch(Exception e) {
			
		}
		
	}

	private Boolean auth(String username, String password) {
		// check if username is inside the users list and matched password
		String line = null;
		Boolean success = false;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(USER_FILE));
			while((line = reader.readLine()) != null) {
				// check
				String[] info = line.split(" ");
				String name = info[0];
				String pw = info[1];
				if( name.equals(username.trim()) && pw.equals(password.trim()) ) {
					success = true;
					
					// set user info
					user.setName(username);
					String haveTopic = info[2];
					user.setCreatedTopic(Boolean.valueOf(haveTopic));
				}
			}
		} catch (Exception e) {
			
		}
		return success;
	}

	public void parseMessage(String message) {
		String username = user.getName();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String now = dateFormat.format(new Date());
		
		// Message
		String msg = "[" + now + "] " + username + ": " + message;
		
		// boardCast message
		chatServer.broadcast(user, msg);
		// TODO: Record in File
		writeMessageToTopicFile(msg);
	}
	
	public void writeMessageToTopicFile(String message) {
		String topicFileName = String.valueOf(user.getTopic()) + ".txt";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(topicFileName, true));
			writer.write(message);
			writer.newLine();
			writer.close();
		} catch(Exception ex) {
		}
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
