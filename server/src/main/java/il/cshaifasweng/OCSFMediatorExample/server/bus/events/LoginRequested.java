package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;


public class LoginRequested {
    public final String username;
    public final String password;
    public final ConnectionToClient client;

    public LoginRequested(String username, String password, ConnectionToClient client) {
        this.username = username;
        this.password = password;
        this.client = client;
    }
}
