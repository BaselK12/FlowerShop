package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;

public class Payment implements Serializable {
    public enum Method { CREDIT_CARD }
    public enum Status { INITIATED, AUTHORIZED, CAPTURED, FAILED, REFUNDED }

    private String id;
    private Method method;
    private String maskedCardNumber;  // "**** **** **** 1234"
    private String cardHolderName;
    private String expirationDate;    // "04/27"
    private String idNumber;          // optional ID for verification
    private String authCode;
    private double amount;
    private Status status;

    public Payment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }

    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getAuthCode() { return authCode; }
    public void setAuthCode(String authCode) { this.authCode = authCode; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
