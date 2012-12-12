import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;


public class ChatServer {
	
	ServerSocket serverSocket;
	Collection<User> users;
	
	public ChatServer() {
		try {
			serverSocket = new ServerSocket(9999);
			users = new ArrayList<User>();
		} catch(IOException ex) {
			System.err.println(ex);
		}
	}
	
	public void addUser(User user) {
		this.users.add(user);
	}
	
	public void removeUser(User user){
		this.users.remove(user);
	}
	
	public void broadcast(User user, String msg) {
		for(User u: users) {
			
			//ignore sender
			Socket s = u.getSocket();
			if(s == user.getSocket()) continue;
			
			// ignore not the same topic
			int topic = user.getTopic();
			if(topic != u.getTopic()) continue;
			
			//send to other
			Command cmd = new Command(CMDList.ReceiveMessage);
			System.out.println(msg);
			cmd.setObject(msg);
			sendCommand(cmd, s);
		}
	}
	
	public void sendCommand(Command cmd, Socket socket) {
		try {
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			
			// Send Command
			oos.writeObject(cmd);
			oos.flush();
		} catch (Exception ex) {
		}
	}
	
	public void run() {
		System.out.println("Server Up ! Listening for Clients in port 9999");
		try {
			for(;;) {
			
				Socket socket = serverSocket.accept();
				
				// Create new User
				User user = new User(socket, "user" + users.size());
				
				//new connection
				this.addUser(user);
				
				//create new thread for new socket
				Connection connection = new Connection(user, this);
				Thread thread = new Thread(connection);
				thread.start();	
				
			}
			
		} catch(IOException ex) {
//			System.err.println(ex);
		} finally {
			
		}
	}
	
	public static void main(String[] args) {
		ChatServer server = new ChatServer();
		server.run();
	}
	
}
