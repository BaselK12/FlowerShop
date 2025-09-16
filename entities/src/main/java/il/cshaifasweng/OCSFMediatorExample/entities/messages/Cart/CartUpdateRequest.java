package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;

import java.io.Serializable;

public class CartUpdateRequest implements Serializable {
    private CartItem item;
    public CartUpdateRequest(CartItem item) { this.item = item; }
    public CartItem getItem() { return item; }
}
