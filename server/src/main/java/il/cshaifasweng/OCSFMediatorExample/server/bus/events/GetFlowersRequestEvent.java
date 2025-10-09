package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.GetFlowersRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record GetFlowersRequestEvent (GetFlowersRequest request, ConnectionToClient client) {}
