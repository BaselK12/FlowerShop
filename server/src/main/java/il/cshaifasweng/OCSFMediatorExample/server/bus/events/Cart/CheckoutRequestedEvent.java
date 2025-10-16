package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.CheckoutRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class CheckoutRequestedEvent {
    private final CheckoutRequest request;
    private final ConnectionToClient client;
    public CheckoutRequestedEvent(CheckoutRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public CheckoutRequest request() { return request; }
    public ConnectionToClient client() { return client; }
}
