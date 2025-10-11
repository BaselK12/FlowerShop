package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Flowers;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.SaveFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record SaveFlowerRequestEvent(SaveFlowerRequest request, ConnectionToClient client) {}
