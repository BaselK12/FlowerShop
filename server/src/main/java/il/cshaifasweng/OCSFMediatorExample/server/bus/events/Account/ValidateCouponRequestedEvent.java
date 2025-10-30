package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class ValidateCouponRequestedEvent {
    private final ValidateCouponRequest request;
    private final ConnectionToClient client;

    public ValidateCouponRequestedEvent(ValidateCouponRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public ValidateCouponRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
