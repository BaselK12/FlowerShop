package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private String sku;
    private String name;
    private int quantity;
    private double unitPrice;
    private double lineTotal;

    public OrderItem() {}

    public OrderItem(String sku, String name, int quantity, double unitPrice) {
        this.sku = sku;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity * unitPrice;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.lineTotal = this.unitPrice * quantity;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.lineTotal = this.unitPrice * this.quantity;
    }

    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
}
