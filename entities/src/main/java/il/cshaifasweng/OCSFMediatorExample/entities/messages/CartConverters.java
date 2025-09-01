package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.OrderItem;
import java.util.List;
import java.util.stream.Collectors;

public final class CartConverters {
    private CartConverters() {}

    public static OrderItem toOrderItem(CartItem it) {
        // CartItem has extras (type, pictureUrl) that are UI-only; we ignore them for orders
        return new OrderItem(it.getSku(), it.getName(), it.getQuantity(), it.getUnitPrice());
    }

    public static List<OrderItem> toOrderItems(List<CartItem> items) {
        return items.stream().map(CartConverters::toOrderItem).collect(Collectors.toList());
    }
}
