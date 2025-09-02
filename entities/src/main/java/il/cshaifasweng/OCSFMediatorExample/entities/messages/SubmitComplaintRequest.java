package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import java.io.Serializable;

public class SubmitComplaintRequest implements Serializable {
    private Complaint complaint;

    public SubmitComplaintRequest() {}

    public SubmitComplaintRequest(Complaint complaint) {
        this.complaint = complaint;
    }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }
}
