package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class CartItem implements Serializable {
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
        this.quantity = quantity;
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
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
