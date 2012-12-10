import java.net.Socket;


public class User {
	private Socket socket;
	private String name;
	private int currentTopic;
	
	public int getTopic() {
		return currentTopic;
	}

	public void setTopic(int currentTopic) {
		this.currentTopic = currentTopic;
	}

	public User(Socket socket, String name) {
		this.socket = socket;
		this.name = name;
	}
	
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "user: " + name + ", topic: " + currentTopic;
	}
	
}
