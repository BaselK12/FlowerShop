package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Coupon;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetCouponsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetCouponsRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.GetCouponsRequestedEvent;
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
    }
}
