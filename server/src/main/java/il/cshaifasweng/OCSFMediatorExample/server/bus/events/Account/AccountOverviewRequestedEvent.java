package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class AccountOverviewRequestedEvent {
    private final AccountOverviewRequest request;
    private final ConnectionToClient client;

    public AccountOverviewRequestedEvent(AccountOverviewRequest request, ConnectionToClient client) {
        this.request = request;
        this.client  = client;
    }

    public AccountOverviewRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}