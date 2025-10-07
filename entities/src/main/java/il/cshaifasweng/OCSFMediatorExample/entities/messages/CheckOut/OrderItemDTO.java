package il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut;

import java.io.Serializable;

public class OrderItemDTO implements Serializable {
    private String sku;
    private String name;
    private int quantity;
    private double unitPrice;
    private double lineTotal;

    public OrderItemDTO() {}

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
}