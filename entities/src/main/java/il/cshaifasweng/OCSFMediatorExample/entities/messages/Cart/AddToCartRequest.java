package il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart;

import java.io.Serializable;

public class AddToCartRequest implements Serializable {
    private String sku;
    private int quantity;

    public AddToCartRequest(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }
}