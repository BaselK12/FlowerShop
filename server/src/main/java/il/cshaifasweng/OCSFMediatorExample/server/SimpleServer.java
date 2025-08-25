package il.cshaifasweng.OCSFMediatorExample.server;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import com.google.common.eventbus.Subscribe;


import il.cshaifasweng.OCSFMediatorExample.entities.messages.Ping;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.PingReceived;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.PingHandler;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Ping;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.LoginRequested;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.PingReceived;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.LoginHandler;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.PingHandler;


import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;

public class SimpleServer extends AbstractServer {
	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();

	public SimpleServer(int port) {
		super(port);

		ServerBus.get().register(new PingHandler());
		ServerBus.get().register(new LoginHandler());


		// Temporary: subscribe a tiny logger to the bus to prove wiring works
		ServerBus.get().register(new Object() {
			@Subscribe
			public void any(Object ev) {
				System.out.println("[BUS] event: " + ev.getClass().getSimpleName());
			}
		});
	}


	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		if (msg instanceof Ping) {
			Ping p = (Ping) msg;
			ServerBus.get().post(new PingReceived(p.getText(), client));
			return;
		}
		if (msg instanceof LoginRequest) {
			LoginRequest lr = (LoginRequest) msg;
			ServerBus.get().post(new LoginRequested(lr.getUsername(), lr.getPassword(), client));
			return;
		}
	}
	public void sendToAllClients(String message) {
		try {
			for (SubscribedClient subscribedClient : SubscribersList) {
				subscribedClient.getClient().sendToClient(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
