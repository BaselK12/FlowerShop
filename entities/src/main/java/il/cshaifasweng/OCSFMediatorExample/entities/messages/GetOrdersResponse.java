package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import java.io.Serializable;
import java.util.List;

public class GetOrdersResponse implements Serializable {
    private List<Order> orders;

    public GetOrdersResponse() {}

    public GetOrdersResponse(List<Order> orders) {
        this.orders = orders;
    }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
}
