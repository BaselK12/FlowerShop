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
        info.setShopId(dto.getBranchName()); // treat branchName as shop identifier for now
        info.setPhone(dto.getPhone());

        // Combine date + time into LocalDateTime
        LocalDate date = dto.getPickupDate();
        String timeStr = dto.getPickupTime();
        if (date != null && timeStr != null) {
            try {
                LocalTime time = LocalTime.parse(timeStr);
                info.setScheduledAt(LocalDateTime.of(date, time));
            } catch (Exception ignored) {}
        }

        return info;
    }

    public static PickupInfoDTO toDTO(PickupInfo entity) {
        if (entity == null) return null;

        PickupInfoDTO dto = new PickupInfoDTO();
        dto.setBranchName(entity.getShopId());
        dto.setPhone(entity.getPhone());

        if (entity.getScheduledAt() != null) {
            dto.setPickupDate(entity.getScheduledAt().toLocalDate());
            dto.setPickupTime(entity.getScheduledAt().toLocalTime().toString());
        }

        return dto;
    }
}