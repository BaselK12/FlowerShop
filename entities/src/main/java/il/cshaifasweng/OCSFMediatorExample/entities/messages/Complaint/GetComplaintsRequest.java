package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import java.io.Serializable;

public class GetComplaintsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long storeID;  // null = whole company
    private final String type;       // null = all types
    private final String status;     // null = all statuses

    public GetComplaintsRequest(Long storeID, String type, String status) {
        this.storeID = storeID;
        this.type = emptyToNull(type);
        this.status = emptyToNull(status);
    }

    public Long getStoreId() {
        return storeID;
    }
    public String getType() { return type; }
    public String getStatus() { return status; }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    @Override public String toString() {
        return "GetComplaintsRequest{store=" + storeID + ", type=" + type + ", status=" + status + "}";
    }
}