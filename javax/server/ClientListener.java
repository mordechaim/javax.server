package javax.server;

public interface ClientListener {

	public void messageReceived(Client client, Object msg);

	public void commandReceived(Client client, Command cmd);

	public void disconnected(Client client);

	public void messageSent(Client client, Object msg);

	public void commandSent(Client client, Command cmd);

	public void connected(Client client);
	
}
