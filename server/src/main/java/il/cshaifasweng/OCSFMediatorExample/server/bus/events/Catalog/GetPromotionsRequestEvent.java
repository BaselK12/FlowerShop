package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetPromotionsRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record GetPromotionsRequestEvent(GetPromotionsRequest request, ConnectionToClient client) {}
