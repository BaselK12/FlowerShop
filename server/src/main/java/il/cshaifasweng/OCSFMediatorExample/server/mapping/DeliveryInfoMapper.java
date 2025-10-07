package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.DeliveryInfo;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.DeliveryInfoDTO;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class DeliveryInfoMapper {
    public static DeliveryInfo fromDTO(DeliveryInfoDTO dto) {
        if (dto == null) return null;

        DeliveryInfo delivery = new DeliveryInfo();
        delivery.setCity(dto.getCity());
        delivery.setStreet(dto.getStreet());
        delivery.setHouse(dto.getHouse());
        delivery.setZipCode(dto.getZipCode());
        delivery.setPhone(dto.getPhone());
        delivery.setDeliveryDate(dto.getDeliveryDate());
        delivery.setDeliveryTime(dto.getDeliveryTime());

        // Optionally combine for easier scheduling logic
        if (dto.getDeliveryDate() != null && dto.getDeliveryTime() != null) {
            try {
                LocalTime time = LocalTime.parse(dto.getDeliveryTime());
                delivery.setScheduledAt(LocalDateTime.of(dto.getDeliveryDate(), time));
            } catch (Exception ignored) {}
        }

        return delivery;
    }
}
