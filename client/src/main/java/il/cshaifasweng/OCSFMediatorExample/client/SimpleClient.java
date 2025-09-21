package il.cshaifasweng.OCSFMediatorExample.client;

import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;

import java.net.Socket;

public class SimpleClient extends AbstractClient {
	
	private static SimpleClient client = null;

	public SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg.getClass().equals(Warning.class)) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
		}
		else{

			EventBus.getDefault().post(msg);

			String message = msg.toString();
			System.out.println("[SimpleClient] " + message);
		}
	}
	
	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3050);
		}
		return client;
	}

	public Socket getSocket() {
		return this.clientSocket;  // 'sock' is protected in AbstractClient
	}

	public boolean hasOpenSocket() {
		return getSocket() != null && getSocket().isConnected() && !getSocket().isClosed();
	}

	@Override
	protected void connectionEstablished() {
		System.out.println("[CLIENT] Connection established." + hasOpenSocket());
		// Now safe to send first requests
	}

}
