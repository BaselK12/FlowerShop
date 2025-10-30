package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Promotion;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.PromotionDTO;

import java.util.List;
import java.util.stream.Collectors;

public final class PromotionMapper {

    private PromotionMapper() {} // prevent instantiation

    // --- Entity → DTO ---
    public static PromotionDTO toDto(Promotion entity) {
        if (entity == null) return null;

        return new PromotionDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType() != null ? entity.getType().name() : "PERCENT",
                entity.getAmount(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.isActive()
        );
    }

    // --- DTO → Entity ---
    public static Promotion toEntity(PromotionDTO dto) {
        if (dto == null) return null;

        Promotion promotion = new Promotion();
        promotion.setId(dto.getId());
        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        promotion.setType(Promotion.DiscountType.valueOf(dto.getType().toUpperCase()));
        promotion.setAmount(dto.getAmount());
        promotion.setValidFrom(dto.getValidFrom());
        promotion.setValidTo(dto.getValidTo());

        return promotion;
    }

    // --- List converters ---
    public static List<PromotionDTO> toDtoList(List<Promotion> entities) {
        return entities == null ? List.of() :
                entities.stream().map(PromotionMapper::toDto).collect(Collectors.toList());
    }

    public static List<Promotion> toEntityList(List<PromotionDTO> dtos) {
        return dtos == null ? List.of() :
                dtos.stream().map(PromotionMapper::toEntity).collect(Collectors.toList());
    }
}
