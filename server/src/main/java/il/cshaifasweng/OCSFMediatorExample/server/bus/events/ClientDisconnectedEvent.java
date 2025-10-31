package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class ClientDisconnectedEvent {
    private final ConnectionToClient client;
    public ClientDisconnectedEvent(ConnectionToClient client) { this.client = client; }
    public ConnectionToClient client() { return client; }
}
