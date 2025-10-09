package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "flowers")
public class Flower implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "sku", length = 50)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 255)
    private String shortDescription;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "flower_categories",
            joinColumns = @JoinColumn(name = "flower_sku"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion; // NEW FIELD

    @Column(name = "is_single", nullable = false)
    private boolean isSingle = true; // default true for single flowers

    public Flower() {}

    public Flower(String sku, String name, String description, double price, String imageUrl) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<Category> getCategory() { return categories; }
    public void setCategory(List<Category> category) { this.categories = category; }

    public Promotion getPromotion() { return promotion; }
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }

    // --- Business Logic ---
    public double getEffectivePrice() {
        if (promotion == null || !promotion.isActive()) return price;

        switch (promotion.getType()) {
            case PERCENT: return price * (1 - promotion.getAmount() / 100);
            case FIXED: return Math.max(0, price - promotion.getAmount());
            default: return price;
        }
    }


    @Transient
    public boolean hasActivePromotion() {
        return promotion != null && promotion.isActive();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flower)) return false;
        Flower other = (Flower) o;
        return sku != null && sku.equals(other.sku);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void refreshPromotionStatus() {
        if (promotion != null && !promotion.isActive()) {
            promotion = null;
        }
    }

    public boolean isSingle() { return isSingle; }
    public void setSingle(boolean single) { this.isSingle = single; }
}
