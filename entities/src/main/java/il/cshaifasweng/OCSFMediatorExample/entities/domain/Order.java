package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Order implements Serializable {
    public enum Status { PENDING, PAID, PREPARING, SHIPPED, DELIVERED, CANCELED }

    private String id;
    private String customerId;
    private LocalDateTime createdAt;
    private Status status;
    private List<OrderItem> items;
    private double subtotal;
    private double discountTotal;
    private double total;
    private DeliveryInfo delivery;
    private PickupInfo pickup;
    private Payment payment;
    private Promotion appliedPromotion;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

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
}
