package javax.server;

public interface ClientListener {

	public void messageReceived(Object msg);

	public void commandReceived(Command cmd);

	public void disconnected();

	public void messageSent(Object msg);

	public void commandSent(Command cmd);

	public void connected();
	
}
