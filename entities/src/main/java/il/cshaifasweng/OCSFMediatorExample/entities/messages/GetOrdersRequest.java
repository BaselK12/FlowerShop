package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class GetOrdersRequest implements Serializable {
    private String customerId;

    public GetOrdersRequest() {}

    public GetOrdersRequest(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
