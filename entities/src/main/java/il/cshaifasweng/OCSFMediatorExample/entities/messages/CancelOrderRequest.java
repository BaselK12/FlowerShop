
package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;
import java.time.Instant;

public class CancelOrderRequest implements Serializable {
    private String orderId;
    private String customerId; // optional but useful for validation
    private Instant now;       // client-supplied timestamp (server may ignore)

    public CancelOrderRequest() {}

    public CancelOrderRequest(String orderId, String customerId, Instant now) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.now = now;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Instant getNow() { return now; }
    public void setNow(Instant now) { this.now = now; }
}
