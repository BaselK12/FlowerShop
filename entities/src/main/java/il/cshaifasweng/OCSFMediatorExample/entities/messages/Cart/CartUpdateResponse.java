package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;

import java.io.Serializable;

public class CartUpdateResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private CartItem item;
    private String message;

    public CartUpdateResponse(CartItem item, String message) {
        this.item = item;
        this.message = message;
    }

    public CartItem getItem() {
        return item;
    }

    public String getMessage() {
        return message;
    }
}