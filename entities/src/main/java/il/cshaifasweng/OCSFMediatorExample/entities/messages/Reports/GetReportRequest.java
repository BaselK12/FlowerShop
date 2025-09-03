package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serializable;

public class GetReportRequest implements Serializable {
    public ReportRequest payload;
    public GetReportRequest() {}
    public GetReportRequest(ReportRequest payload) { this.payload = payload; }
}
