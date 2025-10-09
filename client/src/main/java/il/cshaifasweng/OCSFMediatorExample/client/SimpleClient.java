package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.Socket;

public class SimpleClient extends AbstractClient {

	// Single instance for the whole app (set from App.start())
	private static volatile SimpleClient INSTANCE;

	// Keep host/port if you want to support reconnects
	private final String host;
	private final int port;

	public SimpleClient(String host, int port) {
		super(host, port);
		this.host = host;
		this.port = port;
	}

	/* -------------------- Static accessors -------------------- */

	/** Called once at startup after constructing and opening the connection. */
	public static void setClient(SimpleClient client) {
		if (client == null) throw new IllegalArgumentException("client cannot be null");
		INSTANCE = client;
	}

	/** Controllers use this. If you didn’t set the client yet, that’s on you. */
	public static SimpleClient getClient() {
		if (INSTANCE == null) {
			throw new IllegalStateException("SimpleClient not initialized. " +
					"Create it in App.start(), call openConnection(), then setClient(...).");
		}
		return INSTANCE;
	}

	/* -------------------- Convenience API -------------------- */

	/** Try to ensure the socket is up; if not, attempt to reconnect once. */
	public void ensureConnected() throws IOException {
		if (!isConnected()) {
			try {
				openConnection();
			} catch (Exception e) {
				throw new IOException("Failed to (re)open connection to " + host + ":" + port, e);
			}
		}
	}

	/** Safe send that won’t explode if the socket was closed. */
	public void sendSafely(Object msg) throws IOException {
		ensureConnected();
		sendToServer(msg);
	}

	/** Direct access, if you really need to poke the raw socket. */
	public Socket getSocket() {
		return this.clientSocket; // protected in AbstractClient
	}

	/** Quick health check for UI guards. */
	public boolean hasOpenSocket() {
		Socket s = getSocket();
		return s != null && s.isConnected() && !s.isClosed();
	}

	/* -------------------- AbstractClient hooks -------------------- */

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg instanceof Warning) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
		} else {
			EventBus.getDefault().post(msg); // <-- wrapper
		}
		System.out.println("[SimpleClient] " + msg);
	}

	@Override
	protected void connectionEstablished() {
		System.out.println("[CLIENT] Connection established. hasOpenSocket=" + hasOpenSocket());
		// If you want: EventBus.getDefault().post(new ClientConnectedEvent());
	}

	@Override
	protected void connectionClosed() {
		System.out.println("[CLIENT] Connection closed.");
		// If you want: EventBus.getDefault().post(new ClientDisconnectedEvent());
	}

	@Override
	protected void connectionException(Exception exception) {
		System.err.println("[CLIENT] Connection exception: " + exception.getMessage());
		// If you want: EventBus.getDefault().post(new ClientConnectionErrorEvent(exception));
	}
}
