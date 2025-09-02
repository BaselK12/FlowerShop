package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {
    private String customerId;
    private List<OrderItem> items = new ArrayList<>();
    private double total;

    public Cart() {}

    public Cart(String customerId, List<OrderItem> items) {
        this.customerId = customerId;
        this.items = items;
        recomputeTotal();
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        this.items = items;
        recomputeTotal();
    }

    public double getTotal() { return total; }
    public void recomputeTotal() {
        this.total = items == null ? 0.0 : items.stream().mapToDouble(OrderItem::getLineTotal).sum();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        total += item.getLineTotal();
    }
}
