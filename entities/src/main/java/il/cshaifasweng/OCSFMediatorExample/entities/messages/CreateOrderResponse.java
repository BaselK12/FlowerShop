package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class CreateOrderResponse implements Serializable {
    private String orderId;

    public CreateOrderResponse() {}
    public CreateOrderResponse(String orderId) { this.orderId = orderId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}
