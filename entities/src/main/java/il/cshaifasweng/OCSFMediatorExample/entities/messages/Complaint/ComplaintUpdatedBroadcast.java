package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import java.io.Serializable;

public class ComplaintUpdatedBroadcast implements Serializable {
    private static final long serialVersionUID = 1L;
    private Complaint complaint;

    public ComplaintUpdatedBroadcast() {}
    public ComplaintUpdatedBroadcast(Complaint complaint) { this.complaint = complaint; }
    public Complaint getComplaint() { return complaint; }
}
