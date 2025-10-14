package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetCouponsRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class GetCouponsRequestedEvent {
    private final GetCouponsRequest request;
    private final ConnectionToClient client;
    public GetCouponsRequestedEvent(GetCouponsRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public GetCouponsRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
