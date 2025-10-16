package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.ContinueShoppingRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class ContinueShoppingRequestedEvent {
    private final ContinueShoppingRequest request;
    private final ConnectionToClient client;
    public ContinueShoppingRequestedEvent(ContinueShoppingRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public ContinueShoppingRequest request() { return request; }
    public ConnectionToClient client() { return client; }
}
