package javax.server;

public interface ServerListener {
	
	public void clientConnected(int id);
	
	public void messageReceived(int id, Object msg);
	
	public void commandReceived(int id, Command cmd);
	
	public void clientDisconnected(int id);
	
	public void messageSent(int toId, Object msg);
	
	public void commandSent(int toId, Command cmd);

}
