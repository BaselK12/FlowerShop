package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.UpdateComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class UpdateComplaintRequestedEvent {
    private final UpdateComplaintRequest request;
    private final ConnectionToClient client;

    public UpdateComplaintRequestedEvent(UpdateComplaintRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public UpdateComplaintRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
