package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.*;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.ConfirmRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.ConfirmResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.OrderDTO;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.ConfirmRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.account.OrdersRepository;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.cart.CartRepository;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.OrderMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.Locale;

public class ConfirmRequestHandler {

    public ConfirmRequestHandler(ServerBus bus) {
        bus.subscribe(ConfirmRequestEvent.class, evt -> {
            ConfirmRequest req = evt.request();
            try {
                Long cid = SessionRegistry.get(evt.client());
                if (cid == null) {
                    bus.publish(new SendToClientEvent(new ConfirmResponse(null, "Not logged in", false), evt.client()));
                    return;
                }
                if (req == null || req.getOrder() == null) {
                    bus.publish(new SendToClientEvent(new ConfirmResponse(null, "Empty order", false), evt.client()));
                    return;
                }

                final String couponCode = req.getCouponCode();

                // Compute totals server-side for integrity
                double subtotal = TX.call((Session s) -> {
                    return CartRepository.findByCustomer(cid).stream()
                            .mapToDouble(r -> (r.getUnitPrice() != null ? r.getUnitPrice() : 0.0) *
                                    (r.getQuantity()   != null ? r.getQuantity()   : 1))
                            .sum();
                });

                boolean isPremium = TX.call((Session s) -> {
                    Customer c = s.get(Customer.class, cid);
                    return c != null && c.isPremium();
                });

                // Premium: 10% only if subtotal > $20
                double premiumDiscount = (isPremium && subtotal > 20.0) ? subtotal * 0.10 : 0.0;

                // Coupon: validate and compute effective discount
                class Cup { boolean ok; String type; double amount; Coupon entity; }
                Cup cup = TX.call((Session s) -> {
                    Cup c = new Cup();
                    if (couponCode == null || couponCode.isBlank()) return c;
                    var list = s.createQuery(
                                    "from Coupon c where c.customer.id = :cid and lower(c.code) = :code",
                                    Coupon.class).setParameter("cid", cid)
                            .setParameter("code", couponCode.toLowerCase(Locale.ROOT)).list();
                    if (list.isEmpty()) return c;
                    var coupon = list.get(0);
                    if (!coupon.isActiveToday()) return c;

                    // Interpret discount from code
                    String upper = coupon.getCode() != null ? coupon.getCode().toUpperCase(Locale.ROOT) : "";
                    String type = "PERCENT";
                    double amount = 10; // default 10%

                    if (upper.startsWith("PCT")) {
                        try { amount = Double.parseDouble(upper.substring(3)); type = "PERCENT"; } catch (Exception ignored) {}
                    } else if (upper.startsWith("FIX")) {
                        try { amount = Double.parseDouble(upper.substring(3)); type = "FIXED"; } catch (Exception ignored) {}
                    }

                    c.ok = true; c.type = type; c.amount = amount; c.entity = coupon;
                    return c;
                });

                double afterPremium = Math.max(0.0, subtotal - premiumDiscount);
                double couponDiscount = 0.0;
                if (cup.ok) {
                    if ("PERCENT".equals(cup.type)) {
                        couponDiscount = afterPremium * (cup.amount / 100.0);
                    } else {
                        couponDiscount = cup.amount;
                    }
                    couponDiscount = Math.min(couponDiscount, afterPremium);
                }

                double total = Math.max(0.0, afterPremium - couponDiscount);

                // Map DTO->entity and persist some metadata
                OrderDTO dto = req.getOrder();
                dto.setSubtotal(subtotal);
                dto.setDiscountTotal(premiumDiscount + couponDiscount);
                dto.setTotal(total);
                dto.setStatus(Status.PENDING);
                dto.setCreatedAt(LocalDateTime.now());
                dto.setCustomerId(cid);

                Order entity = OrderMapper.fromDTO(dto);
                if (dto.getStoreId() == null) {
                    throw new IllegalArgumentException("Missing storeId on order DTO");
                }
                entity.setStoreId(dto.getStoreId());
                if (dto.getPickup() != null) {
                    // if mapper didn’t wire it, wire it now
                    if (entity.getPickup() == null) {
                        // use your existing mapper if you have one
                        PickupInfo p = il.cshaifasweng.OCSFMediatorExample.server.mapping.PickupInfoMapper.fromDTO(dto.getPickup());
                        entity.setPickup(p);
                    }
                }

                if (dto.getDelivery() != null) {
                    if (entity.getDelivery() == null) {
                        DeliveryInfo d = il.cshaifasweng.OCSFMediatorExample.server.mapping.DeliveryInfoMapper.fromDTO(dto.getDelivery());
                        entity.setDelivery(d);
                    }
                }

                if (entity.getPickup() != null)   { try { entity.getPickup().setOrder(entity); } catch (Exception ignored) {} }
                if (entity.getDelivery() != null) { try { entity.getDelivery().setOrder(entity); } catch (Exception ignored) {} }





                // Persist, mark coupon used, clear cart
                TX.run((Session s) -> {
                    if (cup.ok && cup.entity != null) {
                        cup.entity.setUsed(true);
                        s.merge(cup.entity);
                    }




                    // You probably persist to DB elsewhere; OrdersRepository keeps an in-memory list as well
                    s.persist(entity);
                    CartRepository.clear(cid);
                });

                // Also add to the in-memory repo for "My Orders" view
                entity.setCustomerId(cid);
                entity.setCreatedAt(LocalDateTime.now());
                OrdersRepository.add(entity);

                String msg = "Order confirmed. Total: $" + String.format(Locale.US, "%.2f", total);
                bus.publish(new SendToClientEvent(new ConfirmResponse(entity.getId(), msg, true), evt.client()));

            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new ConfirmResponse(null, "Failed to confirm order: " + ex.getMessage(), false),
                        evt.client()));
            }
        });
    }
}
