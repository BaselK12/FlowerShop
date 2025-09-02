package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;

public class Flower implements Serializable {
    private String sku;
    private String name;
    private String description;
    private double price;
    private String imageUrl;

    public Flower() {}

    public Flower(String sku, String name, String description, double price, String imageUrl) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
