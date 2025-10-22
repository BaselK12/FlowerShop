package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import java.io.Serializable;

public class UpdateComplaintRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private long complaintId;
    // Optional; send only what you’re changing
    private String resolution;         // new/edited resolution notes
    private String newStatus;          // "IN_PROGRESS", "RESOLVED", "REJECTED" (or null)

    public UpdateComplaintRequest() {}
    public UpdateComplaintRequest(long complaintId, String resolution, String newStatus) {
        this.complaintId = complaintId;
        this.resolution = resolution;
        this.newStatus = newStatus;
    }

    public long getComplaintId() { return complaintId; }
    public void setComplaintId(long complaintId) { this.complaintId = complaintId; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
}
