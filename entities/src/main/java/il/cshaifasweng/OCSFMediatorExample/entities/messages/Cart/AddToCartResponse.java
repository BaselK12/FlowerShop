package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import java.io.Serializable;

public class AddToCartResponse implements Serializable {
    private boolean success;
    private String message;
    private int cartSize;

    public AddToCartResponse(boolean success, String message, int cartSize) {
        this.success = success;
        this.message = message;
        this.cartSize = cartSize;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getCartSize() {
        return cartSize;
    }
}