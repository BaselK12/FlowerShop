package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class GetOrdersRequestedEvent {
    private final GetOrdersRequest request;
    private final ConnectionToClient client;

    public GetOrdersRequestedEvent(GetOrdersRequest request, ConnectionToClient client) {
        this.request = request;
        this.client = client;
    }

    public GetOrdersRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
