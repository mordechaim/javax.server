package javax.server;

import javax.server.Server.ConnectionToClient;

public interface ServerListener {
	
	public void clientConnected(Server server, ConnectionToClient client);
	
	public void messageReceived(Server server, ConnectionToClient client, Object msg);
	
	public void commandReceived(Server server, ConnectionToClient client, Command cmd);
	
	public void clientDisconnected(Server server, ConnectionToClient client);
	
	public void messageSent(Server server, ConnectionToClient toClient, Object msg);
	
	public void commandSent(Server server, ConnectionToClient toClient, Command cmd);

}
