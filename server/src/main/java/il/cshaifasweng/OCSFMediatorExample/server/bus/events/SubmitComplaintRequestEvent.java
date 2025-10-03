package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class SubmitComplaintRequestEvent {
    private SubmitComplaintRequest request;
    private ConnectionToClient client; // reference to the client

    public SubmitComplaintRequestEvent(SubmitComplaintRequest request, ConnectionToClient client) {
        this.request = request;
        this.client = client;
    }

    public SubmitComplaintRequest getRequest() {
        return request;
    }

    public ConnectionToClient getClient() {
        return client;
    }
}
