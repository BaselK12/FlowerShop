package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.AddPromotionsRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record AddPromotionsRequestEvent (AddPromotionsRequest req, ConnectionToClient client) {}
