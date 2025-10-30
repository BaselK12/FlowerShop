package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Coupon;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.CancelOrderRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.CancelOrderResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.CancelOrderRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class CancelOrderHandler {

    public CancelOrderHandler(ServerBus bus) {
        bus.subscribe(CancelOrderRequestEvent.class, evt -> {
            try {
                Long customerId = SessionRegistry.get(evt.client());
                if (customerId == null) {
                    bus.publish(new SendToClientEvent(
                            new CancelOrderResponse(false, -1, "You must be logged in.", 0.0),
                            evt.client()));
                    return;
                }

                CancelOrderRequest req = evt.req();

                var result = TX.call((Session session) -> {
                    Order order = session.get(Order.class, req.getOrderId());
                    if (order == null || !Objects.equals(order.getCustomerId(), customerId)) {
                        return new Object[]{false, "Order not found or does not belong to you.", 0.0};
                    }

                    if (order.getStatus() == Order.Status.CANCELLED) {
                        return new Object[]{false, "This order is already cancelled.", 0.0};
                    }

                    // Determine the scheduled delivery/pickup time
                    LocalDateTime scheduledAt = null;
                    if (order.getDelivery() != null) {
                        LocalDate d = order.getDelivery().getDeliveryDate();
                        String t = order.getDelivery().getDeliveryTime();
                        if (d != null && t != null) {
                            scheduledAt = LocalDateTime.of(d, LocalTime.parse(t));
                        }
                    } else if (order.getPickup() != null) {
                        LocalDate d = order.getPickup().getPickupDate();
                        String t = order.getPickup().getPickupTime();
                        if (d != null && t != null) {
                            scheduledAt = LocalDateTime.of(d, LocalTime.parse(t));
                        }
                    }

                    if (scheduledAt == null) {
                        return new Object[]{false, "Order has no valid scheduled time.", 0.0};
                    }

                    long minutesLeft = Duration.between(LocalDateTime.now(), scheduledAt).toMinutes();

                    if (minutesLeft < 60) {
                        return new Object[]{false, "You can no longer cancel this order (less than 1 hour left).", 0.0};
                    }

                    double refundPercent = (minutesLeft > 180) ? 1.0 : 0.5;
                    double refundAmount = order.getTotal() * refundPercent;

                    // Mark order as cancelled
                    order.setStatus(Order.Status.CANCELLED);
                    session.merge(order);

                    // -------------------------
                    // Create refund coupon
                    // -------------------------
                    Customer customer = session.get(Customer.class, customerId);
                    if (customer != null && refundAmount > 0) {
                        Coupon coupon = new Coupon();

                        String code = "REF" + System.currentTimeMillis() % 100000;
                        coupon.setCustomer(customer);
                        coupon.setCode(code);
                        coupon.setTitle("Refund Coupon");
                        coupon.setDescription(String.format("Refund for Order #%d (₪%.2f)", order.getId(), refundAmount));
                        coupon.setExpiration(LocalDate.now().plusDays(30));
                        coupon.setUsed(false);

                        session.persist(coupon);

                        String msg = String.format(
                                "Order #%d cancelled successfully. Refund: %.0f%% (₪%.2f). " +
                                        "A refund coupon (%s) was added to your account.",
                                order.getId(), refundPercent * 100, refundAmount, code
                        );

                        return new Object[]{true, msg, refundAmount};
                    }

                    String msg = String.format(
                            "Order #%d cancelled successfully. Refund: %.0f%% (₪%.2f).",
                            order.getId(), refundPercent * 100, refundAmount);

                    return new Object[]{true, msg, refundAmount};
                });

                boolean ok = (boolean) result[0];
                String msg = (String) result[1];
                double refundAmount = (double) result[2];

                bus.publish(new SendToClientEvent(
                        new CancelOrderResponse(ok, req.getOrderId(), msg, refundAmount),
                        evt.client()));

            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new CancelOrderResponse(false, -1, "Server error: " + e.getMessage(), 0.0),
                        evt.client()));
            }
        });
    }
}
