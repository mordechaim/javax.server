package javax.server;

import java.io.PrintStream;

import javax.server.Server.ConnectionToClient;

public class Logger implements ServerListener, ClientListener {

	PrintStream out;
	String prefix, delimiter, suffix;

	public Logger(PrintStream out, String prefix, String delimiter, String suffix) {
		this.out = out;
		this.prefix = prefix;
		this.delimiter = delimiter;
		this.suffix = suffix;
	}

	public Logger(PrintStream out) {
		this(out, ">", "#", ":");
	}

	public Logger() {
		this(System.out);
	}

	@Override
	public void messageReceived(Client client, Object msg) {
		out.println(prefix + "ClientListener" + delimiter + "messageReceived" + suffix + " " + msg);
		out.flush();
	}

	@Override
	public void commandReceived(Client client, Command cmd) {
		out.println(prefix + "ClientListener" + delimiter + "commandReceived" + suffix + " " + cmd);
		out.flush();
	}

	@Override
	public void disconnected(Client client) {
		out.println(prefix + "ClientListener" + delimiter + "disconnected");
		out.flush();
	}

	@Override
	public void messageSent(Client client, Object msg) {
		out.println(prefix + "ClientListener" + delimiter + "messageSent" + suffix + " " + msg);
		out.flush();
	}

	@Override
	public void commandSent(Client client, Command cmd) {
		out.println(prefix + "ClientListener" + delimiter + "commandSent" + suffix + " " + cmd);
		out.flush();
	}

	@Override
	public void connected(Client client) {
		out.println(prefix + "ClientListener" + delimiter + "connected");
		out.flush();
	}

	@Override
	public void clientConnected(Server server, ConnectionToClient ctc) {
		out.println(prefix + "ServerListener" + delimiter + "clientConnected" + suffix + " " + ctc.getClientId());
		out.flush();
	}

	@Override
	public void messageReceived(Server server, ConnectionToClient ctc, Object msg) {
		out.println(prefix + "ServerListener" + delimiter + "messageReceived" + suffix + " " + ctc.getClientId() + ", "
				+ msg);
		out.flush();
	}

	@Override
	public void commandReceived(Server server, ConnectionToClient ctc, Command cmd) {
		out.println(prefix + "ServerListener" + delimiter + "commandReceived" + suffix + " " + ctc.getClientId() + ", "
				+ cmd);
		out.flush();
	}

	@Override
	public void clientDisconnected(Server server, ConnectionToClient ctc) {
		out.println(prefix + "ServerListener" + delimiter + "clientDisconnected" + suffix + " " + ctc.getClientId());
		out.flush();
	}

	@Override
	public void messageSent(Server server, ConnectionToClient toCtc, Object msg) {
		out.println(prefix + "ServerListener" + delimiter + "messageSent" + suffix + " " + toCtc.getClientId() + ", "
				+ msg);
		out.flush();
	}

	@Override
	public void commandSent(Server server, ConnectionToClient toCtc, Command cmd) {
		out.println(prefix + "ServerListener" + delimiter + "commandSent" + suffix + " " + toCtc.getClientId() + ", "
				+ cmd);
		out.flush();
	}

}
