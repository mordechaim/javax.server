package javax.server;

import java.io.PrintStream;

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
	public void messageReceived(Object msg) {
		out.println(prefix + "ClientListener" + delimiter + "messageReceived" + suffix + " " + msg);
		out.flush();
	}

	@Override
	public void commandReceived(Command cmd) {
		out.println(prefix + "ClientListener" + delimiter + "commandReceived" + suffix + " " + cmd);
		out.flush();
	}

	@Override
	public void disconnected() {
		out.println(prefix + "ClientListener" + delimiter + "disconnected");
		out.flush();
	}

	@Override
	public void messageSent(Object msg) {
		out.println(prefix + "ClientListener" + delimiter + "messageSent" + suffix + " " + msg);
		out.flush();
	}

	@Override
	public void commandSent(Command cmd) {
		out.println(prefix + "ClientListener" + delimiter + "commandSent" + suffix + " " + cmd);
		out.flush();
	}

	@Override
	public void connected() {
		out.println(prefix + "ClientListener" + delimiter + "connected");
		out.flush();
	}

	@Override
	public void clientConnected(int id) {
		out.println(prefix + "ServerListener" + delimiter + "clientConnected" + suffix + " " + id);
		out.flush();
	}

	@Override
	public void messageReceived(int id, Object msg) {
		out.println(prefix + "ServerListener" + delimiter + "messageReceived" + suffix + " " + id + ", " + msg);
		out.flush();
	}

	@Override
	public void commandReceived(int id, Command cmd) {
		out.println(prefix + "ServerListener" + delimiter + "commandReceived" + suffix + " " + id + ", " + cmd);
		out.flush();
	}

	@Override
	public void clientDisconnected(int id) {
		out.println(prefix + "ServerListener" + delimiter + "clientDisconnected" + suffix + " " + id);
		out.flush();
	}

	@Override
	public void messageSent(int toId, Object msg) {
		out.println(prefix + "ServerListener" + delimiter + "messageSent" + suffix + " " + toId + ", " + msg);
		out.flush();
	}

	@Override
	public void commandSent(int toId, Command cmd) {
		out.println(prefix + "ServerListener" + delimiter + "commandSent" + suffix + " " + toId + ", " + cmd);
		out.flush();
	}

}
