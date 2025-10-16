package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.GetCartRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class GetCartRequestedEvent {
    private final GetCartRequest request;
    private final ConnectionToClient client;
    public GetCartRequestedEvent(GetCartRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public GetCartRequest request() { return request; }
    public ConnectionToClient client() { return client; }
}
