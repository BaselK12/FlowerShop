package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

/**
 * Toggle customer's Premium status.
 * If turning ON, attach a PaymentDTO (same shape you use in registration).
 */
public class SetPremiumRequest implements Serializable {
    private boolean premium;     // true => enable premium, false => disable
    private PaymentDTO payment;  // nullable; required when enabling

    // Required by serializer
    public SetPremiumRequest() { }

    public SetPremiumRequest(boolean premium, PaymentDTO payment) {
        this.premium = premium;
        this.payment = payment;
    }

    // Primary naming
    public boolean isPremium() { return premium; }
    public boolean getPremium() { return premium; } // handler-friendly alias
    public PaymentDTO getPayment() { return payment; }

    public void setPremium(boolean premium) { this.premium = premium; }
    public void setPayment(PaymentDTO payment) { this.payment = payment; }

    // Backward-compat aliases (some older handlers used "enable" or "payload")
    public boolean getEnable() { return premium; }
    public void setEnable(boolean enable) { this.premium = enable; }

    public PaymentDTO getPaymentData() { return payment; }
    public void setPaymentData(PaymentDTO p) { this.payment = p; }
}
