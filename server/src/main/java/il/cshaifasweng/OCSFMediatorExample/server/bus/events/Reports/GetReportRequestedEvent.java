package il.cshaifasweng.OCSFMediatorExample.server.bus.events.Reports;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetReportRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public class GetReportRequestedEvent {
    private final GetReportRequest request;
    private final ConnectionToClient client;
    public GetReportRequestedEvent(GetReportRequest request, ConnectionToClient client) {
        this.request = request; this.client = client;
    }
    public GetReportRequest getRequest() { return request; }
    public ConnectionToClient getClient() { return client; }
}
