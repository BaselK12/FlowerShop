package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class AddPaymentRequest implements Serializable {
    private final String customerId;
    private final PaymentDTO payment;

    public AddPaymentRequest(String customerId, PaymentDTO payment) {
        this.customerId = customerId;
        this.payment = payment;
    }

    public String getCustomerId() { return customerId; }
    public PaymentDTO getPayment() { return payment; }
}
