package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class AddToCartRequestedEvent {
    private final AddToCartRequest request;
    private final ConnectionToClient client;
    public AddToCartRequestedEvent(AddToCartRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public AddToCartRequest request() { return request; }
    public ConnectionToClient client() { return client; }
}
