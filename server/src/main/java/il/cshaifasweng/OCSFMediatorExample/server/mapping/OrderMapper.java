package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.OrderDTO;

import java.util.stream.Collectors;

public class OrderMapper {

    public static Order fromDTO(OrderDTO dto) {
        if (dto == null) return null;

        Order order = new Order();
        order.setId(dto.getId());
        order.setCustomerId(dto.getCustomerId());
        order.setCreatedAt(dto.getCreatedAt());
        order.setStatus(dto.getStatus());
        order.setSubtotal(dto.getSubtotal());
        order.setDiscountTotal(dto.getDiscountTotal());
        order.setTotal(dto.getTotal());
        order.setDelivery(DeliveryInfoMapper.fromDTO(dto.getDelivery()));
        order.setPickup(PickupInfoMapper.fromDTO(dto.getPickup()));
        order.setPayment(PaymentMapper.fromDTO(dto.getPayment()));
        order.setGreetingCard(GreetingCardMapper.fromDTO(dto.getGreetingCard()));

        if (dto.getItems() != null)
            order.setItems(dto.getItems().stream()
                    .map(OrderItemMapper::fromDTO)
                    .collect(Collectors.toList()));

        return order;
    }
}