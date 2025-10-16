package il.cshaifasweng.OCSFMediatorExample.server.handlers.cart;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.CartItemRow;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.Session;

import java.util.List;

public final class CartRepository {
    private CartRepository() {}

    public static List<CartItemRow> findByCustomer(long customerId) {
        return TX.call(s -> s.createQuery(
                "from CartItemRow c where c.customerId = :cid order by c.createdAt asc",
                CartItemRow.class).setParameter("cid", customerId).list());
    }

    public static CartItemRow upsert(Session s, long customerId, String sku,
                                     String name, String pictureUrl, double unitPrice, int qty) {
        CartItemRow row = s.createQuery(
                        "from CartItemRow c where c.customerId=:cid and c.sku=:sku",
                        CartItemRow.class).setParameter("cid", customerId)
                .setParameter("sku", sku)
                .uniqueResultOptional().orElse(null);
        if (row == null) {
            row = new CartItemRow();
            row.setCustomerId(customerId);
            row.setSku(sku);
            row.setName(name);
            row.setPictureUrl(pictureUrl);
            row.setUnitPrice(unitPrice);
            row.setQuantity(qty);
            s.persist(row);
        } else {
            row.setName(name);
            row.setPictureUrl(pictureUrl);
            row.setUnitPrice(unitPrice);
            row.setQuantity(qty);
            s.merge(row);
        }
        return row;
    }

    public static void updateQty(long customerId, String sku, int qty) {
        TX.run(s -> {
            var row = s.createQuery(
                            "from CartItemRow c where c.customerId=:cid and c.sku=:sku",
                            CartItemRow.class).setParameter("cid", customerId)
                    .setParameter("sku", sku)
                    .uniqueResultOptional().orElse(null);
            if (row == null) return;
            if (qty <= 0) s.remove(row);
            else {
                row.setQuantity(qty);
                s.merge(row);
            }
        });
    }

    public static void remove(long customerId, String sku) {
        updateQty(customerId, sku, 0);
    }

    public static void clear(long customerId) {
        TX.run(s -> s.createMutationQuery("delete from CartItemRow c where c.customerId=:cid")
                .setParameter("cid", customerId).executeUpdate());
    }
}
