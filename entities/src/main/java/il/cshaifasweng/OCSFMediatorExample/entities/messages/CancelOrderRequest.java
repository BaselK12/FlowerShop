package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;
import java.time.Instant;

public class CancelOrderRequest implements Serializable {
    private String orderId;
    private Instant now; // client-supplied timestamp (or server will ignore and use its own)

    public CancelOrderRequest() {}
    public CancelOrderRequest(String orderId, Instant now) {
        this.orderId = orderId; this.now = now;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Instant getNow() { return now; }
    public void setNow(Instant now) { this.now = now; }
}
