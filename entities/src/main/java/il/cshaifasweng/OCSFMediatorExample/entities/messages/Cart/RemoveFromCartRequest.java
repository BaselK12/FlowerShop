package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import java.io.Serializable;

public class RemoveFromCartRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sku;

    public RemoveFromCartRequest(String sku) { this.sku = sku; }

    public String getSku() { return sku; }
}
