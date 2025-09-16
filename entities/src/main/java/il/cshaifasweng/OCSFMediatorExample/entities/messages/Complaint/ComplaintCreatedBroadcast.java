package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;

import java.io.Serializable;

public class ComplaintCreatedBroadcast implements Serializable {
    private static final long serialVersionUID = 1L;
    private il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint complaint;
    public ComplaintCreatedBroadcast() {}
    public ComplaintCreatedBroadcast(Complaint c) { this.complaint = c; }
    public Complaint getComplaint() { return complaint; }
}
