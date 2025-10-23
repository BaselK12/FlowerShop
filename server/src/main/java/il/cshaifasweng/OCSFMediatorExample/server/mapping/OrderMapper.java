package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.OrderDTO;

import java.util.Locale;
import java.util.stream.Collectors;

public class OrderMapper {

    public static Order fromDTO(OrderDTO dto) {
        if (dto == null) return null;

        Order order = new Order();
        order.setId(dto.getId());
        order.setCustomerId(dto.getCustomerId());
        // If your OrderDTO has storeId, don't forget:
        // order.setStoreId(dto.getStoreId());

        order.setCreatedAt(dto.getCreatedAt());

        // ---- status mapping fix ----
        if (dto.getStatus() != null) {
            String s = dto.getStatus().toString().trim().toUpperCase(Locale.ROOT);

            // normalize legacy names
            switch (s) {
                case "PENDING", "PROCESSING", "PAID", "PREPARING", "SHIPPED", "DELIVERED":
                    s = "INITIATED"; break;
                case "CANCELED": // American spelling → DB spelling
                    s = "CANCELLED"; break;
            }

            try {
                order.setStatus(Order.Status.valueOf(s));
            } catch (IllegalArgumentException e) {
                order.setStatus(Order.Status.INITIATED);
            }
        }
        // If null, @PrePersist in Order will default to PENDING anyway

        order.setSubtotal(dto.getSubtotal());
        order.setDiscountTotal(dto.getDiscountTotal());
        order.setTotal(dto.getTotal());
        order.setDelivery(DeliveryInfoMapper.fromDTO(dto.getDelivery()));
        order.setPickup(PickupInfoMapper.fromDTO(dto.getPickup()));
        order.setPayment(PaymentMapper.fromDTO(dto.getPayment()));
        order.setGreetingCard(GreetingCardMapper.fromDTO(dto.getGreetingCard()));

        if (dto.getItems() != null) {
            order.setItems(dto.getItems().stream()
                    .map(OrderItemMapper::fromDTO)
                    .collect(Collectors.toList()));
        }

        return order;
    }
}
