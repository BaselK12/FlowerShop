package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.*;
import java.io.Serializable;
import java.util.List;

public class CreateOrderRequest implements Serializable {
    private String customerId;            // who is ordering
    private List<OrderItem> items;        // must be non-empty
    private DeliveryInfo delivery;        // optional (if shipping)
    private PickupInfo pickup;            // optional (if pickup)
    private Payment payment;              // optional
    private GreetingCard greetingCard;    // optional
    private Promotion appliedPromotion;   // optional

    public CreateOrderRequest() {}

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public DeliveryInfo getDelivery() { return delivery; }
    public void setDelivery(DeliveryInfo delivery) { this.delivery = delivery; }

    public PickupInfo getPickup() { return pickup; }
    public void setPickup(PickupInfo pickup) { this.pickup = pickup; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public GreetingCard getGreetingCard() { return greetingCard; }
    public void setGreetingCard(GreetingCard greetingCard) { this.greetingCard = greetingCard; }

    public Promotion getAppliedPromotion() { return appliedPromotion; }
    public void setAppliedPromotion(Promotion appliedPromotion) { this.appliedPromotion = appliedPromotion; }
}
