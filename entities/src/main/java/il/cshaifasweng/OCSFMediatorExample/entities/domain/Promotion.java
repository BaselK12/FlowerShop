package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class Promotion implements Serializable {
    public enum DiscountType { PERCENT, FIXED }

    private String id;
    private String name;
    private String description;
    private DiscountType type;
    private double amount;
    private LocalDate validFrom;
    private LocalDate validTo;
    private List<String> appliesToSkus;

    public Promotion() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public List<String> getAppliesToSkus() { return appliesToSkus; }
    public void setAppliesToSkus(List<String> appliesToSkus) { this.appliesToSkus = appliesToSkus; }
}
