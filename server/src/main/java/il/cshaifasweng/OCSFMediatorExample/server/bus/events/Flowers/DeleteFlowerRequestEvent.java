package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Flowers;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.DeleteFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record DeleteFlowerRequestEvent(DeleteFlowerRequest request, ConnectionToClient client) {}
