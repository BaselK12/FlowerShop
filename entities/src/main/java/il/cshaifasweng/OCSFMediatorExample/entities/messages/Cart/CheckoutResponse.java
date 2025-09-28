package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import java.io.Serializable;

public class CheckoutResponse implements Serializable {
    private boolean success;
    private String message;
    public CheckoutResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
