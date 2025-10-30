package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Coupon;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetCouponsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetCouponsRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.GetCouponsRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.ValidateCouponRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.CouponMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

import java.util.List;

public class CouponsHandler {
    public CouponsHandler(ServerBus bus) {
        bus.subscribe(GetCouponsRequestedEvent.class, evt -> {
            try {
                GetCouponsRequest req = evt.getRequest();
                int page = Math.max(0, req.getPage());
                int size = Math.max(1, Math.min(50, req.getSize()));

                Long customerId = SessionRegistry.get(evt.getClient());
                if (customerId == null) {
                    bus.publish(new SendToClientEvent(
                            new GetCouponsResponse(List.of(), 0, page, size), evt.getClient()));
                    return;
                }

                var result = TX.call((Session s) -> {
                    Long total = s.createQuery(
                                    "select count(c.id) from Coupon c where c.customer.id = :cid", Long.class)
                            .setParameter("cid", customerId)
                            .uniqueResult();

                    List<Coupon> rows = s.createQuery(
                                    "from Coupon c where c.customer.id = :cid order by c.expiration desc", Coupon.class)
                            .setParameter("cid", customerId)
                            .setFirstResult(page * size)
                            .setMaxResults(size)
                            .list();

                    return new Object[]{ total, rows };
                });

                long total = (Long) result[0];
                @SuppressWarnings("unchecked")
                List<Coupon> rows = (List<Coupon>) result[1];

                var items = rows.stream().map(CouponMapper::toDTO).toList();
                bus.publish(new SendToClientEvent(
                        new GetCouponsResponse(items, (int) total, page, size),
                        evt.getClient()
                ));
            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new GetCouponsResponse(List.of(), 0, 0, 0), evt.getClient()));
            }
        });
        // Validate a coupon code for the logged-in customer
        bus.subscribe(ValidateCouponRequestedEvent.class, evt -> {
            try {
                var req = evt.getRequest();
                var code = req.getCode() != null ? req.getCode().trim() : "";
                if (code.isEmpty()) {
                    bus.publish(new SendToClientEvent(
                            new il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponResponse(
                                    false, null, null, null, 0, "Empty code"), evt.getClient()));
                    return;
                }

                Long cid = SessionRegistry.get(evt.getClient());
                if (cid == null) {
                    bus.publish(new SendToClientEvent(
                            new il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponResponse(
                                    false, null, null, null, 0, "Not logged in"), evt.getClient()));
                    return;
                }

                TX.run((Session s) -> {
                    var rows = s.createQuery("from Coupon c where c.customer.id=:cid and lower(c.code)=:code",
                                    il.cshaifasweng.OCSFMediatorExample.entities.domain.Coupon.class)
                            .setParameter("cid", cid)
                            .setParameter("code", code.toLowerCase())
                            .list();

                    if (rows.isEmpty()) {
                        bus.publish(new SendToClientEvent(
                                new il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponResponse(
                                        false, code, null, null, 0, "Coupon not found"), evt.getClient()));
                        return;
                    }

                    var c = rows.get(0);
                    if (!c.isActiveToday()) {
                        bus.publish(new SendToClientEvent(
                                new il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponResponse(
                                        false, c.getCode(), c.getTitle(), null, 0, "Coupon expired or used"), evt.getClient()));
                        return;
                    }

                    // Interpret the discount from the code pattern:
                    // PCT10 -> 10% off  |  FIX5 -> $5 off  | fallback: 10% off
                    String discountType = "PERCENT";
                    double amount = 10; // default 10%

                    String upper = c.getCode() != null ? c.getCode().toUpperCase() : "";
                    if (upper.startsWith("PCT")) {
                        try { amount = Double.parseDouble(upper.substring(3)); discountType = "PERCENT"; } catch (Exception ignored) {}
                    } else if (upper.startsWith("FIX")) {
                        try { amount = Double.parseDouble(upper.substring(3)); discountType = "FIXED"; } catch (Exception ignored) {}
                    }

                    bus.publish(new SendToClientEvent(
                            new il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponResponse(
                                    true, c.getCode(), c.getTitle(), discountType, amount, "OK"), evt.getClient()));
                });

            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponResponse(
                                false, null, null, null, 0, "Server error: " + e.getMessage()), evt.getClient()));
            }
        });

    }
}
