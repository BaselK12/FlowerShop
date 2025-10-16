package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.CartUpdateRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class CartUpdateRequestedEvent {
    private final CartUpdateRequest request;
    private final ConnectionToClient client;
    public CartUpdateRequestedEvent(CartUpdateRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public CartUpdateRequest request() { return request; }
    public ConnectionToClient client() { return client; }
}
