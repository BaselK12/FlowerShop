package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.RemovePaymentRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class RemovePaymentRequestedEvent {
    private final RemovePaymentRequest request;
    private final ConnectionToClient client;

    public RemovePaymentRequestedEvent(RemovePaymentRequest request, ConnectionToClient client) {
        this.request = request;
        this.client = client;
    }
    public RemovePaymentRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
