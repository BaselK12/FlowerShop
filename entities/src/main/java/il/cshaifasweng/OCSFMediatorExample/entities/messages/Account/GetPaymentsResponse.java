package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;

import java.io.Serializable;
import java.util.List;

public class GetPaymentsResponse implements Serializable {
    private final List<Payment> payments;
    public GetPaymentsResponse(List<Payment> payments) { this.payments = payments; }
    public List<Payment> getPayments() { return payments; }
}
