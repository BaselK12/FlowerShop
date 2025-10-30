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

        return delivery;
    }

    public static DeliveryInfoDTO toDTO(DeliveryInfo entity) {
        if (entity == null) return null;

        DeliveryInfoDTO dto = new DeliveryInfoDTO();
        dto.setCity(entity.getCity());
        dto.setStreet(entity.getStreet());
        dto.setHouse(entity.getHouse());
        dto.setZipCode(entity.getZipCode());
        dto.setPhone(entity.getPhone());
        dto.setDeliveryDate(entity.getDeliveryDate());
        dto.setDeliveryTime(entity.getDeliveryTime());

        return dto;
    }
}