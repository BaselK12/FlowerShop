package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import java.io.Serializable;

public class GetComplaintsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String storeName;  // null = whole company
    private final String type;       // null = all types
    private final String status;     // null = all statuses

    public GetComplaintsRequest(String storeName, String type, String status) {
        this.storeName = emptyToNull(storeName);
        this.type = emptyToNull(type);
        this.status = emptyToNull(status);
    }

    public String getStoreName() { return storeName; }
    public String getType() { return type; }
    public String getStatus() { return status; }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    @Override public String toString() {
        return "GetComplaintsRequest{store=" + storeName + ", type=" + type + ", status=" + status + "}";
    }
}