package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.OrderItem;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.OrderItemDTO;

public class OrderItemMapper {

    public static OrderItem fromDTO(OrderItemDTO dto) {
        if (dto == null) return null;

        OrderItem item = new OrderItem();
        item.setSku(dto.getSku());
        item.setName(dto.getName());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setLineTotal(dto.getLineTotal());
        return item;
    }
}
