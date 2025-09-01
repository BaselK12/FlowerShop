package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record LoginRequestedEvent(LoginRequest req, ConnectionToClient client) {}