package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Category;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Promotion;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.PromotionDTO;

import java.util.List;
import java.util.stream.Collectors;

public class FlowerMapper {

    // ============================
    //  Entity → DTO
    // ============================
    public static FlowerDTO toDTO(Flower flower) {
        if (flower == null) return null;

        // Convert Promotion → PromotionDTO (if exists)
        PromotionDTO promoDTO = null;
        Promotion promo = flower.getPromotion();
        if (promo != null) {
            promoDTO = new PromotionDTO(
                    promo.getId(),
                    promo.getName(),
                    promo.getDescription(),
                    promo.getType().name(),
                    promo.getAmount(),
                    promo.getValidFrom(),
                    promo.getValidTo(),
                    promo.isActive()
            );
        }

        // Convert categories to simple display names
        List<String> categoryNames = flower.getCategory() != null
                ? flower.getCategory().stream()
                .map(Category::getDisplayName)
                .collect(Collectors.toList())
                : List.of();

        // Build DTO
        return new FlowerDTO(
                flower.getSku(),
                flower.getName(),
                flower.getShortDescription(),
                flower.getPrice(),
                flower.getEffectivePrice(),
                flower.getImageUrl(),
                promoDTO,
                categoryNames
        );
    }

    // ============================
    //  DTO → Entity
    // ============================
    public static Flower toEntity(FlowerDTO dto, List<Category> availableCategories, Promotion promotion) {
        if (dto == null) return null;

        Flower flower = new Flower();
        flower.setSku(dto.getSku());
        flower.setName(dto.getName());
        flower.setShortDescription(dto.getShortDescription());
        flower.setPrice(dto.getPrice());
        flower.setImageUrl(dto.getImageUrl());
        flower.setPromotion(promotion);

        if (dto.getCategories() != null && availableCategories != null) {
            List<Category> categories = availableCategories.stream()
                    .filter(c -> dto.getCategories().contains(c.getDisplayName()))
                    .collect(Collectors.toList());
            flower.setCategory(categories);
        }

        return flower;
    }
}
