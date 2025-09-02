package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import java.io.Serializable;

public class CancelOrderResponse implements Serializable {
    private boolean ok;
    private String reason;  // if failed
    private Order order;    // the updated order (status should now be CANCELED)

    public CancelOrderResponse() {}

    public CancelOrderResponse(boolean ok, String reason, Order order) {
        this.ok = ok;
        this.reason = reason;
        this.order = order;
    }

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}
