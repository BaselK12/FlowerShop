package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Complaint implements Serializable {
    public enum Status { OPEN, IN_PROGRESS, RESOLVED, REJECTED }

    private String id;
    private String customerId;
    private String orderId;
    private String text;
    private Status status;
    private LocalDateTime createdAt;
    private String resolution;

    public Complaint() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
