package il.cshaifasweng.OCSFMediatorExample.client.Reports;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.ReportRequest;

public class ReportRequestEvent {
    public final ReportRequest request;
    public ReportRequestEvent(ReportRequest request) { this.request = request; }
}