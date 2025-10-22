package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import java.io.Serializable;

public class UpdateComplaintResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean ok;
    private String reason;          // error if not ok
    private Complaint updated;      // the updated entity for UI refresh

    public UpdateComplaintResponse() {}
    public UpdateComplaintResponse(boolean ok, String reason, Complaint updated) {
        this.ok = ok;
        this.reason = reason;
        this.updated = updated;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Complaint getUpdated() { return updated; }
    public void setUpdated(Complaint updated) { this.updated = updated; }
}
