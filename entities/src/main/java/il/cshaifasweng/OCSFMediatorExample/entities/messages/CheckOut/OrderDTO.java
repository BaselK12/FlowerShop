package il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Status;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long customerId;
    private LocalDateTime createdAt;
    private Status status;

    private List<OrderItemDTO> items;
    private double subtotal;
    private double discountTotal;
    private double total;

    private DeliveryInfoDTO delivery;
    private PickupInfoDTO pickup;
    private PaymentDTO payment;
    private GreetingCardDTO greetingCard;

    private String notes;

    public OrderDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(double discountTotal) { this.discountTotal = discountTotal; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public DeliveryInfoDTO getDelivery() { return delivery; }
    public void setDelivery(DeliveryInfoDTO delivery) { this.delivery = delivery; }

    public PickupInfoDTO getPickup() { return pickup; }
    public void setPickup(PickupInfoDTO pickup) { this.pickup = pickup; }

    public PaymentDTO getPayment() { return payment; }
    public void setPayment(PaymentDTO payment) { this.payment = payment; }

    public GreetingCardDTO getGreetingCard() { return greetingCard; }
    public void setGreetingCard(GreetingCardDTO greetingCard) { this.greetingCard = greetingCard; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
