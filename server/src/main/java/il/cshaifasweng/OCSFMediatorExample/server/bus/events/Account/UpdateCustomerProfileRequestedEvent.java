package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class UpdateCustomerProfileRequestedEvent {
    private final UpdateCustomerProfileRequest request;
    private final ConnectionToClient client;

    public UpdateCustomerProfileRequestedEvent(UpdateCustomerProfileRequest request, ConnectionToClient client) {
        this.request = request;
        this.client  = client;
    }

    public UpdateCustomerProfileRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}