package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import java.io.Serializable;

public class SubmitComplaintResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean ok;
    private String reason;       // error message if not ok
    private String complaintId;  // generated ID if ok

    public SubmitComplaintResponse() {}

    public SubmitComplaintResponse(boolean ok, String reason, String complaintId) {
        this.ok = ok;
        this.reason = reason;
        this.complaintId = complaintId;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }
}
