package il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint;

import java.io.Serializable;

public class GetCustomerComplaintsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long customerId;

    public GetCustomerComplaintsRequest(long customerId) { this.customerId = customerId; }
    public long getCustomerId() { return customerId; }

    @Override public String toString() { return "GetCustomerComplaintsRequest{customerId=" + customerId + "}"; }
}
