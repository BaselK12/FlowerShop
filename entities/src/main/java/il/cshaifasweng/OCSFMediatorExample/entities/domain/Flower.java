package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.util.List;

public class Flower implements Serializable {
    private String sku;
    private String name;
    private String description;
    private String shortDescription;
    private double price;
    private String imageUrl;
    private boolean promo;
    private List<Category> category;

    public Flower() {}

    public Flower(String sku, String name, String description, double price, String imageUrl) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // --- Getters & Setters ---

    public String getSku() {
        return sku;
    }
    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPromo() {
        return promo;
    }
    public void setPromo(boolean promo) {
        this.promo = promo;
    }

    // Getters/Setters
    public List<Category> getCategory() {
        return category;
    }
    public void setCategory(List<Category> category) {
        this.category = category;
    }
}
