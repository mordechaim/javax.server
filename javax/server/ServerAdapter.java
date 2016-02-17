package javax.server;

public class ServerAdapter implements ServerListener {

	@Override
	public void clientConnected(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageReceived(int id, Object msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commandReceived(int id, Command cmd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clientDisconnected(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageSent(int toId, Object msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commandSent(int toId, Command cmd) {
		// TODO Auto-generated method stub

	}

}
