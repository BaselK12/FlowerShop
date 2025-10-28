package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Admin.AdminLoginRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record AdminLoginRequestEvent(AdminLoginRequest req, ConnectionToClient client) {}
