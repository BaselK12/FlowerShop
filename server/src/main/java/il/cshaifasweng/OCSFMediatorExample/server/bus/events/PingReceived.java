package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;



public class PingReceived {
    public final String text;
    public final ConnectionToClient client;

    public PingReceived(String text, ConnectionToClient client) {
        this.text = text;
        this.client = client;
    }
}
