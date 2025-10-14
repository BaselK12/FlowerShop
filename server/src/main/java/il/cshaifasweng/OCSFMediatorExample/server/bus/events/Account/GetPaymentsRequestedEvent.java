package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetPaymentsRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class GetPaymentsRequestedEvent {
    private final GetPaymentsRequest request;
    private final ConnectionToClient client;

    public GetPaymentsRequestedEvent(GetPaymentsRequest request, ConnectionToClient client) {
        this.request = request;
        this.client = client;
    }
    public GetPaymentsRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
