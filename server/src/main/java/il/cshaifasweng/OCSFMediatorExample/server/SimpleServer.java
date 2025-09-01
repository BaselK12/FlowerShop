package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.*;
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
			} else if (msg instanceof RegisterRequest rr) {
				bus.publish(new RegisterRequestedEvent(rr, client));
			} else if (msg instanceof String s) {
				switch (s) {
					case "FETCH_EMPLOYEES" ->
							bus.publish(new EmployeesFetchRequestedEvent(client));

					case "EMPLOYEES_OPEN_EDITOR:NEW" ->
							bus.publish(new EmployeesOpenEditorEvent(
									EmployeesOpenEditorEvent.EditorMode.NEW, null, client));

					default -> {
						if (s.startsWith("EMPLOYEES_OPEN_EDITOR:EDIT:")) {
							var idStr = s.substring("EMPLOYEES_OPEN_EDITOR:EDIT:".length());
							try {
								long id = Long.parseLong(idStr);
								bus.publish(new EmployeesOpenEditorEvent(
										EmployeesOpenEditorEvent.EditorMode.EDIT, id, client));
							} catch (NumberFormatException e) {
								bus.publish(new SendToClientEvent(
										new ErrorResponse("Bad employee id: " + idStr), client));
							}
						} else if (s.startsWith("EMPLOYEES_DELETE:")) {
							var idStr = s.substring("EMPLOYEES_DELETE:".length());
							try {
								long id = Long.parseLong(idStr);
								bus.publish(new EmployeesDeleteRequestedEvent(id, client));
							} catch (NumberFormatException e) {
								bus.publish(new SendToClientEvent(
										new ErrorResponse("Bad employee id: " + idStr), client));
							}
						} else {
							bus.publish(new SendToClientEvent(new ErrorResponse("Unknown command: " + s), client));
						}
					}
				}
			}
		} catch (Exception e) {
			bus.publish(new SendToClientEvent(
					new ErrorResponse("Server error: " + e.getMessage()), client));
		}
	}
}
