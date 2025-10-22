package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders") // must match your table name
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    // Persisted columns we actually need
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "store_id")
    private Long storeId;

    // Everything else stays non-persistent for now
    @Transient private LocalDateTime createdAt;
    @Transient private Status status;
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

    // --- persisted fields ---
    public Long getId() { return id; }
    /** Needed for your in-memory OrdersRepository. DO NOT set this on entities you plan to persist. */
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    // --- transient fields (same API you had) ---
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
