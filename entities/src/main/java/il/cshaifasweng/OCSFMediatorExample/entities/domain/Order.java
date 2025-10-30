package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_store",      columnList = "store_id"),
                @Index(name = "idx_orders_customer",   columnList = "customer_id"),
                @Index(name = "idx_orders_created_at", columnList = "created_at"),
                @Index(name = "idx_orders_status",     columnList = "status")
        }
)
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    // Matches the ENUM('INITIATED','COMPLETED','CANCELLED','REFUNDED') in SQL
    public enum Status {
        INITIATED, COMPLETED, CANCELLED, REFUNDED
    }

    // ---------- persisted columns ----------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_id", nullable = true) // NULL allowed in SQL
    private Long customerId;

    @Column(name = "employee_id", nullable = true)
    private Long employeeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal total = java.math.BigDecimal.ZERO;

    // Linked optional entities
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private DeliveryInfo delivery;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private PickupInfo pickup;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private GreetingCard greetingCard;

    // ---------- non-persistent (computed / runtime only) ----------
    @Transient private List<OrderItem> items;
    @Transient private double subtotal;
    @Transient private double discountTotal;
    @Transient private Payment payment;
    @Transient private Promotion appliedPromotion;

    public Order() {}

    // ---------- lifecycle ----------
    @PrePersist
    private void onInsert() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null)    status = Status.INITIATED;
    }

    // ---------- getters/setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // Return double for convenience
    public double getTotal() {
        return total != null ? total.doubleValue() : 0.0;
    }

    public void setTotal(double total) {
        this.total = java.math.BigDecimal.valueOf(total);
    }

    public void setTotal(java.math.BigDecimal total) {
        this.total = total;
    }

    public DeliveryInfo getDelivery() { return delivery; }
    public void setDelivery(DeliveryInfo delivery) { this.delivery = delivery; }

    public PickupInfo getPickup() { return pickup; }
    public void setPickup(PickupInfo pickup) { this.pickup = pickup; }

    public GreetingCard getGreetingCard() { return greetingCard; }
    public void setGreetingCard(GreetingCard greetingCard) { this.greetingCard = greetingCard; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(double discountTotal) { this.discountTotal = discountTotal; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public Promotion getAppliedPromotion() { return appliedPromotion; }
    public void setAppliedPromotion(Promotion appliedPromotion) { this.appliedPromotion = appliedPromotion; }
}
