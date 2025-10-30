package il.cshaifasweng.OCSFMediatorExample.server.handlers.cart;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.CartItemRow;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.*;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartState;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart.*;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;

public class CartHandler {
    public CartHandler(ServerBus bus) {

        // 1) Get cart
        bus.subscribe(GetCartRequestedEvent.class, evt -> {
            Long cid = SessionRegistry.get(evt.client());
            if (cid == null) {
                bus.publish(new SendToClientEvent(
                        new ContinueShoppingResponse("Not logged in"), evt.client()));
                return;
            }
            var rows = CartRepository.findByCustomer(cid);
            var dto = toState(rows);
            bus.publish(new SendToClientEvent(dto, evt.client())); // CartState
        });

        // 2) Add to cart — FIXED: actually reads price/name/image
        bus.subscribe(AddToCartRequestedEvent.class, evt -> {
            Long cid = SessionRegistry.get(evt.client());
            if (cid == null) {
                bus.publish(new SendToClientEvent(
                        new AddToCartResponse(false, "Not logged in", 0), evt.client()));
                return;
            }
            AddToCartRequest r = evt.request();

            TX.run(s -> {
                var existing = CartRepository.find(s, cid, r.getSku());
                int currentQty = existing != null && existing.getQuantity() != null
                        ? existing.getQuantity()
                        : 0;
                int newQty = currentQty + Math.max(1, r.getQuantity());

                // Load product data for correct price/name/image
                Flower f = s.get(Flower.class, r.getSku());
                String name = f != null ? f.getName() : r.getSku();
                String imageUrl = f != null ? f.getImageUrl() : null;
                double unitPrice = f != null ? f.getPrice() : 0.0;

                CartRepository.upsert(s,
                        cid,
                        r.getSku(),
                        name,
                        imageUrl,
                        unitPrice,
                        newQty);
            });

            int size = CartRepository.findByCustomer(cid).size();
            bus.publish(new SendToClientEvent(
                    new AddToCartResponse(true, "Added", size), evt.client()));
        });

        // 3) Update qty (server already deletes on qty<=0)
        bus.subscribe(CartUpdateRequestedEvent.class, evt -> {
            Long cid = SessionRegistry.get(evt.client());
            if (cid == null) return;
            CartItem it = evt.request().getItem();
            CartRepository.updateQty(cid, it.getSku(), it.getQuantity());
            bus.publish(new SendToClientEvent(
                    new CartUpdateResponse(it, "OK"), evt.client()));
        });

        // 4) New: Remove from cart explicitly
        bus.subscribe(RemoveFromCartRequestedEvent.class, evt -> {
            Long cid = SessionRegistry.get(evt.client());
            if (cid == null) return;
            CartRepository.remove(cid, evt.request().getSku());
            bus.publish(new SendToClientEvent(
                    new RemoveFromCartResponse(evt.request().getSku(), "Removed"), evt.client()));
        });

        // 5) Continue (no-op server side, keep for compatibility)
        bus.subscribe(ContinueShoppingRequestedEvent.class, evt -> {
            bus.publish(new SendToClientEvent(
                    new ContinueShoppingResponse("OK"), evt.client()));
        });

        // 6) Checkout: keep clearing cart on server for when you want it
        bus.subscribe(CheckoutRequestedEvent.class, evt -> {
            Long cid = SessionRegistry.get(evt.client());
            if (cid == null) {
                bus.publish(new SendToClientEvent(
                        new CheckoutResponse(false, "Not logged in"), evt.client()));
                return;
            }
            CartRepository.clear(cid);
            bus.publish(new SendToClientEvent(
                    new CheckoutResponse(true, "Checked out"), evt.client()));
        });
    }

    private static CartState toState(List<CartItemRow> rows) {
        double[] total = {0.0};
        var items = rows.stream().map(r -> {
            CartItem c = new CartItem();
            c.setSku(r.getSku());
            c.setName(r.getName());
            c.setPictureUrl(r.getPictureUrl());
            c.setUnitPrice(r.getUnitPrice() != null ? r.getUnitPrice() : 0.0);
            c.setQuantity(r.getQuantity() != null ? r.getQuantity() : 1);
            total[0] += c.getSubtotal();
            return c;
        }).toList();
        return new CartState(items, total[0]);
    }
}
