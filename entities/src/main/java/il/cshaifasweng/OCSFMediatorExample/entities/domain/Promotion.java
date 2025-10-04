package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "promotions")
public class Promotion implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum DiscountType { PERCENT, FIXED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // use numeric ID for clean DB joins

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 10)
    private DiscountType type;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    // Reverse relation to Flower (optional, not required but nice for inspection)
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Flower> flowers;

    public Promotion() {}

    public Promotion(String name, String description, DiscountType type, double amount,
                     LocalDate validFrom, LocalDate validTo) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.amount = amount;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DiscountType getType() { return type; }
    public void setType(DiscountType type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public List<Flower> getFlowers() { return flowers; }
    public void setFlowers(List<Flower> flowers) { this.flowers = flowers; }

    @Override
    public String toString() {
        return name + " (" + type + " " + amount +
                (type == DiscountType.PERCENT ? "%" : "₪") + ")";
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return (validFrom == null || !today.isBefore(validFrom)) &&
                (validTo == null || !today.isAfter(validTo));
    }
}
