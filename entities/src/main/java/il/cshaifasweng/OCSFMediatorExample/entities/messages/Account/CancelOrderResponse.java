package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class CancelOrderResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final long orderId;
    private final String message;
    private final double refundAmount;  // new field

    public CancelOrderResponse(boolean success, long orderId, String message, double refundAmount) {
        this.success = success;
        this.orderId = orderId;
        this.message = message;
        this.refundAmount = refundAmount;
    }

    // Optional overload for compatibility (if older code still calls 3-arg constructor)
    public CancelOrderResponse(boolean success, long orderId, String message) {
        this(success, orderId, message, 0.0);
    }

    public boolean isSuccess() { return success; }
    public long getOrderId() { return orderId; }
    public String getMessage() { return message; }
    public double getRefundAmount() { return refundAmount; }
}
