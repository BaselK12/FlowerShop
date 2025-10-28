package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.SetPremiumRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class SetPremiumRequestedEvent {
    private final SetPremiumRequest request;
    private final ConnectionToClient client;

    public SetPremiumRequestedEvent(SetPremiumRequest request, ConnectionToClient client) {
        this.request = request;
        this.client = client;
    }

    public SetPremiumRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
