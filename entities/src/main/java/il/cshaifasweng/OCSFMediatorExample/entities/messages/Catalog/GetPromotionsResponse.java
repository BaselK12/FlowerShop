package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import java.io.Serializable;
import java.util.List;

public class GetPromotionsResponse implements Serializable {
    private List<PromotionDTO> promotions;

    public GetPromotionsResponse(List<PromotionDTO> promotions) {
        this.promotions = promotions;
    }

    public List<PromotionDTO> getPromotions() { return promotions; }
}
