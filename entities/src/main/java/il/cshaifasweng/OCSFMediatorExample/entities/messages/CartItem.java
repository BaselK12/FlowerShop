package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;
import java.util.Objects;

public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sku;
    private String name;
    private String type;
    private String pictureUrl;
    private int quantity;
    private double unitPrice;

    public CartItem() {}

    public CartItem(String sku, String name, String type, String pictureUrl,
                    int quantity, double unitPrice) {
        this.sku = sku;
        this.name = name;
        this.type = type;
        this.pictureUrl = pictureUrl;
        setQuantity(quantity); // ensures clamping
        this.unitPrice = unitPrice;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity); // clamp to >= 1
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    // Convenience
    public double getSubtotal() {
        return unitPrice * quantity;
    }

    // Helpful for list operations and de-duplication
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem that = (CartItem) o;
        // if sku is null, fall back to identity equality
        return sku != null && sku.equals(that.sku);
    }

    @Override
    public int hashCode() {
        return sku != null ? sku.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", qty=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
