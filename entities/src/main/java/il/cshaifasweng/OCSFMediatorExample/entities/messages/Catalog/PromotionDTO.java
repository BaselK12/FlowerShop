package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import java.io.Serializable;
import java.time.LocalDate;

public class PromotionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String type;      // "PERCENT" or "FIXED"
    private double amount;
    private LocalDate validFrom;
    private LocalDate validTo;
    private boolean active;

    public PromotionDTO() {}

    public PromotionDTO(Long id, String name, double amount, String type) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.type = type;
    }

    public PromotionDTO(Long id, String name, String description,
                        String type, double amount,
                        LocalDate validFrom, LocalDate validTo, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.amount = amount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name + (active ? " (Active)" : " (Expired)");
    }
}