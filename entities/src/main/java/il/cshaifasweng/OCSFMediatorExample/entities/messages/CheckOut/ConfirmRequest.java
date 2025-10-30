package il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut;

import java.io.Serializable;

public class ConfirmRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private OrderDTO order;

    // NEW: optional coupon code selected during checkout
    private String couponCode;

    public ConfirmRequest() {}

    public ConfirmRequest(OrderDTO order) {
        this.order = order;
    }

    public ConfirmRequest(OrderDTO order, String couponCode) {
        this.order = order;
        this.couponCode = couponCode;
    }

    public OrderDTO getOrder() { return order; }
    public void setOrder(OrderDTO order) { this.order = order; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    @Override
    public String toString() {
        return "ConfirmRequest{orderId=" + (order != null ? order.getId() : "null") +
                ", couponCode=" + couponCode + "}";
    }
}
