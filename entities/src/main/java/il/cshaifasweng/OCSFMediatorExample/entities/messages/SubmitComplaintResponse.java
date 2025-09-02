package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class SubmitComplaintResponse implements Serializable {
    private boolean ok;
    private String reason;

    public SubmitComplaintResponse() {}

    public SubmitComplaintResponse(boolean ok, String reason) {
        this.ok = ok;
        this.reason = reason;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
