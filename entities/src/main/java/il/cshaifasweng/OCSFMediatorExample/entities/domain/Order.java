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

    // Keep this inner enum: server code uses Order.Status.COMPLETED
    public enum Status {
        INITIATED, COMPLETED, CANCELLED, REFUNDED  // ← NOPE
    }

    // ---------- persisted columns ----------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    // ---------- non-persistent (computed / not stored yet) ----------
    @Transient private List<OrderItem> items;
    @Transient private double subtotal;
    @Transient private double discountTotal;
    @Transient private double total;
    @Transient private DeliveryInfo delivery;
    @Transient private PickupInfo pickup;
    @Transient private Payment payment;
    @Transient private Promotion appliedPromotion;
    @Transient private GreetingCard greetingCard;

    public Order() {}

    // ---------- lifecycle ----------
    @PrePersist
    private void onInsert() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null)    status = Status.INITIATED;  // matches DB
    }

    // ---------- getters/setters ----------
    public Long getId() { return id; }
    /** Only for synthetic/in-memory use. Don’t set this for persisted entities. */
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(double discountTotal) { this.discountTotal = discountTotal; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public DeliveryInfo getDelivery() { return delivery; }
    public void setDelivery(DeliveryInfo delivery) { this.delivery = delivery; }

    public PickupInfo getPickup() { return pickup; }
    public void setPickup(PickupInfo pickup) { this.pickup = pickup; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public Promotion getAppliedPromotion() { return appliedPromotion; }
    public void setAppliedPromotion(Promotion appliedPromotion) { this.appliedPromotion = appliedPromotion; }

    public GreetingCard getGreetingCard() { return greetingCard; }
    public void setGreetingCard(GreetingCard greetingCard) { this.greetingCard = greetingCard; }
}
