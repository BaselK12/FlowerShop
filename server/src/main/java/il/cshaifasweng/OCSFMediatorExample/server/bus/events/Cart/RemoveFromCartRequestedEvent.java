package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.RemoveFromCartRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class RemoveFromCartRequestedEvent {
    private final RemoveFromCartRequest request;
    private final ConnectionToClient client;

    public RemoveFromCartRequestedEvent(RemoveFromCartRequest request, ConnectionToClient client) {
        this.request = request;
        this.client = client;
    }
    public RemoveFromCartRequest request() { return request; }
    public ConnectionToClient client() { return client; }
}
