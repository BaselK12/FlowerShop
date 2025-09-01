package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.CustomerLoginNavEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.LoginRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ObservableServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

// your existing entities/messages
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.ErrorResponse;

public class SimpleServer extends ObservableServer {
	private final ServerBus bus;

	public SimpleServer(int port, ServerBus bus) {
		super(port);
		this.bus = bus;
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			if (msg instanceof String s) {
				switch (s) {
					case "CustomerLoginPage Back" -> bus.publish(new CustomerLoginNavEvent("BACK", client));
					case "CustomerLoginPage register" -> bus.publish(new CustomerLoginNavEvent("REGISTER", client));
					default -> bus.publish(new SendToClientEvent(
							new ErrorResponse("Unknown command: " + s), client));
				}
			} else if (msg instanceof LoginRequest lr) {
				bus.publish(new LoginRequestedEvent(lr, client));
			} else {
				bus.publish(new SendToClientEvent(
						new ErrorResponse("Unknown payload type: " + msg.getClass().getSimpleName()),
						client));
			}
		} catch (Exception e) {
			bus.publish(new SendToClientEvent(
					new ErrorResponse("Server error: " + e.getMessage()), client));
		}
	}
}
