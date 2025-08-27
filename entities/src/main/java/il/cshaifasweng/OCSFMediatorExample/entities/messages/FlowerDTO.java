package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;

public class FlowerDTO implements Serializable {
    private String sku;
    private String name;
    private String type;         // Rose, Tulip, etc.
    private double unitPrice;    // was 'price'
    private String pictureUrl;
    private String description;  // optional, from UML

    public FlowerDTO() {}

    public FlowerDTO(String sku, String name, String type, double unitPrice,
                     String pictureUrl, String description) {
        this.sku = sku;
        this.name = name;
        this.type = type;
        this.unitPrice = unitPrice;
        this.pictureUrl = pictureUrl;
        this.description = description;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
