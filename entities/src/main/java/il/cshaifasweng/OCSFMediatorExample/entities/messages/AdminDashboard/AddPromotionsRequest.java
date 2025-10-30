package il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class AddPromotionsRequest implements Serializable {
    private static final long serialVersionUID = 1L;


    private String name;
    private String description;
    private String type;      // "PERCENT" or "FIXED"
    private double amount;
    private LocalDate validFrom;
    private LocalDate validTo;

    // Flowers attached to this promotion (by admin selection)
    private List<FlowerDTO> flowers;

    public AddPromotionsRequest() {}

    public AddPromotionsRequest( String name, String description, double amount, LocalDate validFrom,
                                LocalDate validTo, List<FlowerDTO> flowers) {
        this.name = name;
        this.description = description;
        this.type = "PERCENT";
        this.amount = amount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.flowers = flowers;
    }



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

    public List<FlowerDTO> getFlowers() { return flowers; }
    public void setFlowers(List<FlowerDTO> flowers) { this.flowers = flowers; }

    @Override
    public String toString() {
        return "AddPromotionsRequest{" +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", flowersCount=" + (flowers != null ? flowers.size() : 0) +
                '}';
    }
}