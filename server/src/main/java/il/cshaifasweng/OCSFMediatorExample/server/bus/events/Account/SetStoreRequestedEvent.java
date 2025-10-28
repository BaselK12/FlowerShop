package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.SetStoreRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class SetStoreRequestedEvent {
    private final SetStoreRequest request;
    private final ConnectionToClient client;

    public SetStoreRequestedEvent(SetStoreRequest request, ConnectionToClient client) {
        this.request = request;
        this.client = client;
    }

    public SetStoreRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
