package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class SubmitComplaintRequest implements Serializable {
    private String customerId;
    private String orderId;   // optional
    private String text;

    public SubmitComplaintRequest() {}

    public SubmitComplaintRequest(String customerId, String orderId, String text) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.text = text;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
