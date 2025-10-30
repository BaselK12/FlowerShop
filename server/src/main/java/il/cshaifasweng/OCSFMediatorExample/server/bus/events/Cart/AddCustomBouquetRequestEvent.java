package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.AddCustomBouquetRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record AddCustomBouquetRequestEvent(AddCustomBouquetRequest req, ConnectionToClient client) {
}
