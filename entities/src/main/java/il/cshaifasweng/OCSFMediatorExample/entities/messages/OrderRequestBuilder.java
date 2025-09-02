package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.*;
import java.util.List;

public final class OrderRequestBuilder {
    private OrderRequestBuilder() {}

    public static CreateOrderRequest fromCart(
            String customerId,
            List<CartItem> cartItems,
            DeliveryInfo delivery,      // pass null if pickup
            PickupInfo pickup,          // pass null if delivery
            Payment payment,            // or null if you authorize later
            GreetingCard greetingCard,  // or null
            Promotion promotion         // or null
    ) {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(customerId);
        req.setItems(CartConverters.toOrderItems(cartItems));
        req.setDelivery(delivery);
        req.setPickup(pickup);
        req.setPayment(payment);
        req.setGreetingCard(greetingCard);
        req.setAppliedPromotion(promotion);
        return req;
    }
}
