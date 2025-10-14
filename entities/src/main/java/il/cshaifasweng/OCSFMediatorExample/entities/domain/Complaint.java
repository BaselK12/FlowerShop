package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "complaints")
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { OPEN, IN_PROGRESS, RESOLVED, REJECTED }

    // Identity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Store (added for UI “Store” column/filter)
    // You can fill these on the server when joining order → store.
    @Column(name = "store_id", length = 50, nullable = false)
    private Long storeId;        // keep as String to match DTO/UI

    @Transient
    private String storeName;

    // Existing fields
    @Column(name = "customer_id", length = 50, nullable = false)
    private Long customerId;

    @Column(name = "order_id", length = 50, nullable = false)
    private Long orderId;

    // Type (added for UI “Type” column/filter)
    // Keep as String to match the controller’s String cell value factory.
    // Examples: "Service", "Product Quality", "Delivery", "Pricing", "Billing", "Refund", "Technical", "Other"
    @Column(name = "type", length = 100, nullable = false)
    private String type;

    @Column(name = "subject", columnDefinition = "TEXT", nullable = false)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String text;


    @Column(name = "anonymous", nullable = false)  // adjust nullable as needed
    private boolean anonymous;

    /* Optional fields from DTO
    @Column(name = "anonymous", columnDefinition = "BIT(1)")
    private boolean anonymous;*/

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            columnDefinition = "ENUM('OPEN','IN_PROGRESS','RESOLVED','REJECTED')"
    )
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolution")
    private String resolution;

    // --- Constructors ---
    public Complaint() {}

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    // --- Convenience for UI ---
    @Transient
    public String getStatusName() {
        return status == null ? "" : status.name();
    }

    public String getSummary() {
        return getSummary(120);
    }

    public String getSummary(int maxLen) {
        String s = text == null ? "" : text.trim();
        if (s.length() <= maxLen) return s;
        return s.substring(0, Math.max(0, maxLen - 1)) + "…";
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "id=" + id +
                ", customerId='" + customerId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", type='" + type + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Complaint)) return false;
        Complaint that = (Complaint) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
