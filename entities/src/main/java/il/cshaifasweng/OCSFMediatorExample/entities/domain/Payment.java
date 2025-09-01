package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;

public class Payment implements Serializable {
    public enum Method { CREDIT_CARD }
    public enum Status { INITIATED, AUTHORIZED, CAPTURED, FAILED, REFUNDED }

    private String id;
    private Method method;
    private String maskedPan;
    private String cardholder;
    private String authCode;
    private double amount;
    private Status status;

    public Payment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }

    public String getMaskedPan() { return maskedPan; }
    public void setMaskedPan(String maskedPan) { this.maskedPan = maskedPan; }

    public String getCardholder() { return cardholder; }
    public void setCardholder(String cardholder) { this.cardholder = cardholder; }

    public String getAuthCode() { return authCode; }
    public void setAuthCode(String authCode) { this.authCode = authCode; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
