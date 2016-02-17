package javax.server;

import javax.server.Server.ConnectionToClient;

public class ServerAdapter implements ServerListener {

	@Override
	public void clientConnected(Server server, ConnectionToClient client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageReceived(Server server, ConnectionToClient client, Object msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commandReceived(Server server, ConnectionToClient client, Command cmd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientDisconnected(Server server, ConnectionToClient client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageSent(Server server, ConnectionToClient toClient, Object msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commandSent(Server server, ConnectionToClient toClient, Command cmd) {
		// TODO Auto-generated method stub
		
	}

}
