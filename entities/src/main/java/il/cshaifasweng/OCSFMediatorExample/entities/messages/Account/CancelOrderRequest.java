package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class CancelOrderRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long orderId;

    public CancelOrderRequest(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() { return orderId; }
}
