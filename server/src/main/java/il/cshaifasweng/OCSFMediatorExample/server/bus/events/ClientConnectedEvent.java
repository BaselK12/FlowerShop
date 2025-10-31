package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class ClientConnectedEvent {
    private final ConnectionToClient client;
    public ClientConnectedEvent(ConnectionToClient client) { this.client = client; }
    public ConnectionToClient client() { return client; }
}
