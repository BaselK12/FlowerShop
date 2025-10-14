package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class RemovePaymentRequest implements Serializable {
    private final String customerId;
    private final String paymentId;

    public RemovePaymentRequest(String customerId, String paymentId) {
        this.customerId = customerId;
        this.paymentId = paymentId;
    }

    public String getCustomerId() { return customerId; }
    public String getPaymentId() { return paymentId; }
}
