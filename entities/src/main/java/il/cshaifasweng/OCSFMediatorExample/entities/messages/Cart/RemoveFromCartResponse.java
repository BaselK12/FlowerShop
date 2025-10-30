package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import java.io.Serializable;

public class RemoveFromCartResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sku;
    private final String message;

    public RemoveFromCartResponse(String sku, String message) {
        this.sku = sku;
        this.message = message;
    }

    public String getSku() { return sku; }
    public String getMessage() { return message; }
}
