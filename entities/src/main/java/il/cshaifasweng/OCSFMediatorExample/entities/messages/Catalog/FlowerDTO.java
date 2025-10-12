package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import java.io.Serializable;
import java.util.List;


public class FlowerDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sku;
    private String name;
    private String description;         // full description (TEXT)
    private String shortDescription;    // short version for previews
    private double price;
    private double effectivePrice;
    private String imageUrl;

    private PromotionDTO promotion;     // may be null
    private List<String> categories;    // e.g. ["Romantic", "Bouquet"]

    public FlowerDTO() {}

    public FlowerDTO(String sku, String name, String description, String shortDescription,
                     double price, double effectivePrice, String imageUrl,
                     PromotionDTO promotion, List<String> categories) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.shortDescription = shortDescription;
        this.price = price;
        this.effectivePrice = effectivePrice;
        this.imageUrl = imageUrl;
        this.promotion = promotion;
        this.categories = categories;
    }

    // --- Getters & Setters ---
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getEffectivePrice() { return effectivePrice; }
    public void setEffectivePrice(double effectivePrice) { this.effectivePrice = effectivePrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public PromotionDTO getPromotion() { return promotion; }
    public void setPromotion(PromotionDTO promotion) { this.promotion = promotion; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    // --- Utility ---
    public boolean hasActivePromotion() {
        return promotion != null && promotion.isActive();
    }
}
