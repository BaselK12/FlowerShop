package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;


import java.io.Serializable;

public class GetReportResponse implements Serializable {
    public ReportResponse payload;
    public GetReportResponse() {}
    public GetReportResponse(ReportResponse payload) { this.payload = payload; }
}
