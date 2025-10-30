package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.PickupInfo;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.PickupInfoDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PickupInfoMapper {

    public static PickupInfo fromDTO(PickupInfoDTO dto) {
        if (dto == null) return null;

        PickupInfo info = new PickupInfo();
        info.setBranchName(dto.getBranchName());
        info.setPickupDate(dto.getPickupDate());
        info.setPickupTime(dto.getPickupTime());
        info.setPhone(dto.getPhone());

        return info;
    }

    public static PickupInfoDTO toDTO(PickupInfo entity) {
        if (entity == null) return null;

        PickupInfoDTO dto = new PickupInfoDTO();
        dto.setBranchName(entity.getBranchName());
        dto.setPickupDate(entity.getPickupDate());
        dto.setPickupTime(entity.getPickupTime());
        dto.setPhone(entity.getPhone());

        return dto;
    }
}