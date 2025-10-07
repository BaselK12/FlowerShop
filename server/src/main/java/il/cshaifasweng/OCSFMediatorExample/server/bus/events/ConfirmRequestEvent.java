package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.ConfirmRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record ConfirmRequestEvent (ConfirmRequest request, ConnectionToClient client) {}
