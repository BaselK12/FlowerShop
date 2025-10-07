package il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut;

import java.io.Serializable;

public class ConfirmRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private OrderDTO order;

    public ConfirmRequest() {}

    public ConfirmRequest(OrderDTO order) {
        this.order = order;
    }

    public OrderDTO getOrder() {
        return order;
    }

    public void setOrder(OrderDTO order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "ConfirmRequest{orderId=" + (order != null ? order.getId() : "null") + "}";
    }
}