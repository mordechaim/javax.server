package javax.server;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;

/**
 * This class, represents a client which can connect to a server (that uses, or
 * subclasses {@linkplain Server}) and interact with. Its implementation uses
 * low level sockets without any protocol.
 * 
 * @author Mordechai Meisels
 * 
 * @see {@linkplain Server}
 * 
 */
public class Client {

	/*
	 * a single connection to a server.
	 */
	private ConnectionToServer connection;

	/*
	 * all incoming messages are queued here.
	 */
	private LinkedBlockingQueue<Object> messages;

	/*
	 * ID number, assigned by server, at connection.
	 */
	private AtomicInteger id;

	/*
	 * address of server, to connect to; provided by subclass.
	 */
	private String serverAddress;

	/*
	 * the listening port, specified by subclass.
	 */
	private int port;

	/*
	 * whether the client is active.
	 */
	private volatile boolean running;

	/*
	 * false, if ever shut down
	 */
	private volatile boolean alive;

	/*
	 * where listeners are saved.
	 */
	private List<ClientListener> listeners;

	/**
	 * Maximum time allowed for new connection to hang, on authentication and
	 * initialization.
	 */
	public static final int TIMEOUT = 10000;

	/**
	 * Constructs a {@code Client} trying to connect to server at specified
	 * address, on specified port.
	 * 
	 * <p>
	 * The client will start running when the {@code start()} method is invoked.
	 * it can be stopped, by calling {@code shutDown()}.
	 * 
	 * @param serverAddress
	 *            The Internet address of the server to connect to.
	 * @param port
	 *            The port to listen for.
	 */
	public Client(String serverAddress, int port) {
		this.serverAddress = serverAddress;
		this.port = port;
		id = new AtomicInteger(0);
		messages = new LinkedBlockingQueue<>();
		listeners = Collections.synchronizedList(new ArrayList<>());
		alive = true;

		addClientListener(new ClientAdapter() {
			@Override
			public void commandReceived(Client client, Command cmd) {
				if (cmd == ServerCommand.DISCONNECTED)
					shutDown();
			}
		});
	}

	/**
	 * Returns if the client is currently running. This may be changed either by
	 * the {@code shutDown()} method, or by any error occurring to the client or
	 * server.
	 * 
	 * @return If the client is currently running.
	 */
	public boolean running() {
		return running;
	}

	/**
	 * Returns if the client is eligible to start, or has started already. A
	 * dead client, means it has shut down and may not start again.
	 * 
	 * @return If the client has not ever shut down.
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Returns a unique ID, assigned by the server at connection, which will
	 * identify this client through all other clients currently connected to the
	 * server. The id number has no actual value, it is just an arbitrary random
	 * number.
	 * 
	 * <p>
	 * It is guaranteed that there will never be to clients - connected - that
	 * have the same id number, The server may, though, reuse a number once a
	 * client has disconnected.
	 * 
	 * <p>
	 * It is also guaranteed that the value will never be 0, as long this client
	 * is connected to server. Once disconnected and notified listeners, the id
	 * will always be 0.
	 * 
	 * @return This clients unique ID, assigned by server.
	 */
	public int getClientId() {
		return id.intValue();
	}

	/**
	 * Returns the address of the server currently connected to.
	 * 
	 * @return The address of the server currently connected to.
	 */
	public String getServerAddress() {
		return serverAddress;
	}

	/**
	 * Returns the connection port of the client.
	 * 
	 * @return The connection port of the client.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Initialization on every received message. A subclass may return a
	 * different object (e.g. after decode), that will be forwarded to the
	 * listeners. If the returned value is {@code null}, the message will not be
	 * forwarded to the listeners.
	 * 
	 * <p>
	 * This method will never give a {@code null} value as an argument.
	 * 
	 * <p>
	 * The current implementation simply returns the all messages, as received.
	 * 
	 * @param msg
	 *            The received message.
	 * 
	 * @return The message that should be forwarded to the listeners.
	 */
	protected Object messageReceivedInit(Object msg) {
		return msg;
	}

	/**
	 * Commands that the server may send, may be initialized here. Commands are
	 * interactions between server and client that is not meant to be shown for
	 * the end-user, rather it is to update each other with back-side
	 * information, or to request certain types of data.
	 * 
	 * <p>
	 * A subclass may return a different object (e.g. after decode), that will
	 * be forwarded to the listeners. If the returned value is {@code null}, the
	 * command will not be forwarded to the listeners.
	 * 
	 * <p>
	 * This method will never give a {@code null} value as an argument.
	 * 
	 * <p>
	 * The current implementation simply returns the all commands, as received.
	 *
	 * @param cmd
	 *            The received command.
	 * 
	 * @return The command that should be forwarded to the listeners.
	 */
	protected Command commandReceivedInit(Command cmd) {
		return cmd;
	}

	/**
	 * This method is called by client, once it successfully connected to the
	 * server. a subclass may exchange any information - as passwords - from or
	 * to server, as long {@linkplain Server#connectionInit} is overridden
	 * accordingly. The subclass may, as well, do here any initialization
	 * required for new connection. Examples include, wrapping the streams (e.g.
	 * with {@code javax.crypto.CipherOutputStream} - for network security),
	 * which is allowed, as long the top-most stream is an Object stream.
	 * 
	 * <p>
	 * In any event, where a subclass want's to prevent the connection, it
	 * should return {@code null}.
	 * 
	 * <p>
	 * Please note: At this stage of the client connection-life, it is
	 * technically not running, so {@code send()} <em>will <b>not</b> send</em>.
	 * A subclass should manually send with the stream's methods. Remember to
	 * flush and reset the output stream after writing, (or use the
	 * {@code force()} convenience method).
	 * 
	 * <p>
	 * Also note, that the socket has been set to time out if it blocks for
	 * {@linkplain TIMEOUT} milliseconds on a read. We strongly advise not to
	 * change this behavior; all time consuming outputs (e.g reading from
	 * {@code System.in}, and send to server), should be avoided in this method.
	 * 
	 * <p>
	 * This method may throw either an {@code IOException}, or a
	 * {@code ClassNotFoundException}. If any if these occur, the connection
	 * will be shut down.
	 * 
	 * <p>
	 * The current implementation of this method simply returns a new instance
	 * of {@code ConnectionToServer}. A subclass may return a custom version of
	 * that class.
	 * 
	 * @param socket
	 *            The socket connected to client.
	 * @param in
	 *            Input stream to read input from client.
	 * @param out
	 *            Output stream to write output to client.
	 * @return A {@code ConnectionToServer}, or {@code null} to reject
	 *         connection.
	 */
	protected ConnectionToServer connectionInit(Socket socket, ObjectInputStream in, ObjectOutputStream out)
			throws IOException, ClassNotFoundException {
		return new ConnectionToServer(socket, in, out);

	}

	/**
	 * A subclass may do any initialization in this method, that will be called
	 * in the event the client or server is shut down. This method will always
	 * be called <em>after</em> the streams have been closed, it should be used
	 * just for client-side stuff.
	 * 
	 * <p>
	 * The current implementation does nothing.
	 */
	protected void disconnectionInit() {
	}

	/**
	 * Sends given serializable object to the server, may be a command too.
	 * 
	 * @param msg
	 *            The message to be sent.
	 * @return If message has been sent successfully.
	 */
	public boolean send(Serializable msg) {
		return getConnection().send(msg);
	}

	/**
	 * Returns the connection to the server.
	 * 
	 * @return The connection to the server.
	 */
	protected ConnectionToServer getConnection() {
		return connection;
	}

	/**
	 * Registers a {@code ClientListener} to listen for client events.
	 * 
	 * @param cl
	 *            The {@code ClientListener} to register.
	 */

	public void addClientListener(ClientListener cl) {
		listeners.add(cl);
	}

	/**
	 * Unregisters a {@code ClientListener} from getting client events.
	 * 
	 * @param cl
	 *            The {@code ClientListener} to remove.
	 */
	public void removeClientListener(ClientListener cl) {
		listeners.remove(cl);
	}

	private volatile boolean started = false;

	/**
	 * Starts the client, and returns if it has successfully started.
	 * 
	 * <p>
	 * If the client has been shut down once (dead), it will not be started, and
	 * will return {@code false}.
	 * 
	 * @return If the client has been started successfully.
	 */
	public boolean start() {
		if (!isAlive() || running())
			return false;

		if (started)
			return false;
		synchronized (this) {
			if (started)
				return false;
			started = true;
		}

		Socket socket = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		try {
			socket = new Socket(serverAddress, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			connection = authenticate(socket, in, out);

		} catch (IOException e) {
			try {
				if (out != null)
					out.close();
			} catch (IOException e1) {
			}
			return false;
		}

		if (connection == null) {
			shutDown();
			return false;
		}
		running = true;

		Thread messageHandling = new Thread(new MessageHandling(), "Message handling thread");
		messageHandling.setDaemon(true);
		messageHandling.start();
		connection.startReading();

		for (ClientListener cl : listeners)
			cl.connected(this);

		return true;
	}

	/**
	 * Blocks the calling thread, until the client has been shut down or
	 * unsynced.
	 * 
	 * @throws InterruptedException
	 *             If the thread has been interrupted while waiting.
	 */
	public void sync() throws InterruptedException {
		if (!running)
			return;
		synchronized (this) {
			if (!running)
				return;
			wait();
		}
	}

	/**
	 * Blocks the calling thread for the given time, unless the client has been
	 * shut down or unsynced.
	 * 
	 * @param timeout
	 *            The maximum amount of milliseconds to block.
	 * 
	 * @throws InterruptedException
	 *             If the thread has been interrupted while waiting.
	 */
	public void sync(int timeout) throws InterruptedException {
		if (!running)
			return;
		synchronized (this) {
			if (!running)
				return;
			wait(timeout);
		}
	}

	/**
	 * Unblocks all blocked threads, which called {@code sync()}.
	 */
	synchronized public void unsync() {
		notifyAll();
	}

	/**
	 * Successfully shuts down this client. Once shut down the client can not be
	 * restarted. A new instance must be created.
	 */
	public void shutDown() {
		if (!isAlive())
			return;
		synchronized (this) {
			if (!alive)
				return;
			alive = false;
			running = false;
		}

		ConnectionToServer con = getConnection();
		if (con == null)
			return;

		try {
			if (!con.socket.isClosed())
				con.out.writeObject(ClientCommand.DISCONNECT);

			con.in.close();
			con.out.close();
		} catch (IOException e) {
		}

		disconnectionInit();
		for (ClientListener cl : listeners)
			cl.disconnected(this);

		id.set(0);
		unsync();
	}

	/**
	 * Convenience method to force written objects on the stream, since objects
	 * operate weirdly on ObjectOutputStreams.
	 */
	protected static void force(ObjectOutputStream out) throws IOException {
		out.flush();
		out.reset();
	}

	/*
	 * Here we make sure it is actually my server, not a foreign server running
	 * on same computer on same port.
	 * 
	 * We don't want to hang too long, for the tiny chance the above may happen.
	 * So we set a socket timeout.
	 */
	private ConnectionToServer authenticate(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
		try {
			socket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
		}

		ConnectionToServer cts = null;
		try {
			Object handShake = in.readObject();
			if (handShake != ServerCommand.HANDSHAKE) {
				System.err.println("Unknown server. Disconnecting...");
				return null;
			}
			out.writeObject(ClientCommand.HANDSHAKE);
			force(out);
			id.set(in.readInt());

			cts = connectionInit(socket, in, out);

			Object connected = in.readObject();

			if (connected != ServerCommand.CONNECTED) {
				System.err.println(connected);
				return null;
			}

		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return null;
		}
		try {
			socket.setSoTimeout(0);
		} catch (SocketException e) {
		}
		return cts;
	}

	/*
	 * A thread that constantly takes (or blocks until available) messages from
	 * the message queue. It then forwards it to the right method, either
	 * command or message.
	 */
	private class MessageHandling implements Runnable {
		public void run() {
			while (running()) {
				try {
					Object msg = messages.take();
					if (msg == null)
						continue;

					if (msg instanceof Command) {
						msg = commandReceivedInit((Command) msg);

						if (msg != null)
							for (ClientListener cl : listeners)
								cl.commandReceived(Client.this, (Command) msg);

					} else {
						msg = messageReceivedInit(msg);

						if (msg != null)
							for (ClientListener cl : listeners)
								cl.messageReceived(Client.this, msg);

					}
				} catch (InterruptedException e) {
				} catch (Throwable t) {
					t.printStackTrace();
					shutDown();
				}
			}
		}
	}

	/**
	 * This class represents a connection to a server. When created it will
	 * start running and listen for data received from server. Once it is shut
	 * down, it cannot be started again. A new instance must be created.
	 * 
	 */
	public class ConnectionToServer {
		private Socket socket;
		private ObjectInputStream in;
		private ObjectOutputStream out;

		/**
		 * Constructs a new instance of a server connection.
		 * 
		 * @param socket
		 *            The server's socket, must be open.
		 * @param in
		 *            The object input stream, for reading server input.
		 * @param out
		 *            The object output stream for sending data to server.
		 */
		public ConnectionToServer(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
			if (socket.isClosed())
				throw new IllegalStateException("Server's socket is closed.");

			this.socket = socket;
			this.in = in;
			this.out = out;
		}

		private void startReading() {
			Thread read = new Thread(new Reading(), "Message reading thread");
			read.setDaemon(true);
			read.start();
		}

		/**
		 * Returns the socket connecting the server.
		 * 
		 * @return The socket connecting the server.
		 */
		protected Socket getSocket() {
			return socket;
		}

		/**
		 * Returns the input stream.
		 * 
		 * @return The input stream.
		 */
		protected ObjectInputStream getInputStream() {
			return in;
		}

		/**
		 * Returns the output stream.
		 * 
		 * @return The output stream.
		 */
		protected ObjectOutputStream getOutputStream() {
			return out;
		}

		/*
		 * This class takes care of all incoming messages from the server.
		 *
		 */
		private class Reading implements Runnable {
			public void run() {
				while (running()) {
					try {
						Object msg = in.readObject();
						messages.put(msg);
					} catch (InterruptedException e) {
					} catch (IOException e) {
						shutDown();
					} catch (Throwable t) {
						t.printStackTrace();
						shutDown();
					}
				}
			}
		}

		/**
		 * Sends given serializable object to the server.
		 * 
		 * @param msg
		 *            The message to be sent, may be a command too.
		 * @return If message has been sent successfully.
		 */
		protected boolean send(Serializable msg) {
			if (msg == null)
				return false;

			msg = sendInit(msg);
			if (msg == null)
				return false;

			try {
				if (socket.isClosed()) {
					shutDown();
					return false;
				}
				out.writeObject(msg);
				force(out);

				if (msg instanceof Command)
					for (ClientListener cl : listeners)
						cl.commandSent(Client.this, (Command) msg);
				else
					for (ClientListener cl : listeners)
						cl.messageSent(Client.this, msg);

				return true;
			} catch (IOException e) {
				return false;
			}
		}

		/**
		 * A subclass may work with an object asked to send, here (e.g. encode).
		 * Only the returned object will be sent. Returning {@code null} will
		 * successfully abort the sending of this message.
		 * 
		 * <p>
		 * This implementation simply returns the same object, given as
		 * argument.
		 * 
		 * @param msg
		 *            The message asked to be sent.
		 * @return The actual message to send.
		 */
		protected Serializable sendInit(Serializable msg) {
			return msg;
		}

		@Override
		protected void finalize() {
			shutDown();
		}
	}
}
