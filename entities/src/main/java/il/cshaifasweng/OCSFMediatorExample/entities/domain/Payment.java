package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment implements Serializable {

    public enum Method { CREDIT_CARD }
    public enum Status { AUTHORIZED, CAPTURED, DECLINED, CANCELED }

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    // Keep it simple: store customer id directly
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 32, nullable = false)
    private Method method;

    @Column(name = "masked_card_number", length = 32, nullable = false)
    private String maskedCardNumber;

    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;

    // store as "MM/YY" like your UI
    @Column(name = "expiration_date", length = 5)
    private String expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private Status status;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // -------- getters/setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }

    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
