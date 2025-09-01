package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;
import java.util.List;

public class CreateOrderRequest implements Serializable {
    private List<CartItem> items;     // must be non-empty
    private GreetingCard card;        // optional

    public CreateOrderRequest() {}

    public CreateOrderRequest(List<CartItem> items, GreetingCard card) {
        this.items = items;
        this.card = card;
    }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public GreetingCard getCard() { return card; }
    public void setCard(GreetingCard card) { this.card = card; }
}
