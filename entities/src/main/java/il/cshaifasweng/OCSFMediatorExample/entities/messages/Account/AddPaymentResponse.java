package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;

import java.io.Serializable;

public class AddPaymentResponse implements Serializable {
    private final Payment saved;
    public AddPaymentResponse(Payment saved) { this.saved = saved; }
    public Payment getSaved() { return saved; }
}
