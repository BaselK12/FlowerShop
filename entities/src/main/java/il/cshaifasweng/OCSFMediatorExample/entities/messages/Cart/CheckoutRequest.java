package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;

import java.io.Serializable;
import java.util.List;

public class CheckoutRequest implements Serializable {
    private List<CartItem> items;
    public CheckoutRequest(List<CartItem> items) { this.items = items; }
    public List<CartItem> getItems() { return items; }
}
