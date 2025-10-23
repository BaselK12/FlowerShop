package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Reports;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class GetStoresRequestedEvent {
    private final GetStoresRequest request;
    private final ConnectionToClient client;
    public GetStoresRequestedEvent(GetStoresRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public GetStoresRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
