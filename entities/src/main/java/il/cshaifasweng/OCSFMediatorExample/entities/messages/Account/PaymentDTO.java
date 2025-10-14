package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;

import java.io.Serializable;

public class PaymentDTO implements Serializable {
    private Payment.Method method;
    private String maskedCardNumber;
    private String cardHolderName;
    private String expirationDate;
    private Double amount; // 0.0 for stored method

    public Payment.Method getMethod() { return method; }
    public void setMethod(Payment.Method method) { this.method = method; }
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
