package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AddPaymentRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class AddPaymentRequestedEvent {
    private final AddPaymentRequest request;
    private final ConnectionToClient client;

    public AddPaymentRequestedEvent(AddPaymentRequest request, ConnectionToClient client) {
        this.request = request;
        this.client = client;
    }
    public AddPaymentRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
