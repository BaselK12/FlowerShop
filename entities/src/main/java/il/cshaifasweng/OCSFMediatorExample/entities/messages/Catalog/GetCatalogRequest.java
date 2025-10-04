package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import java.io.Serializable;

public class GetCatalogRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String category;          // optional: e.g. "Romantic", "Bouquet"
    private Long promotionId;         // optional: filter by promotion
    private String searchText;        // optional: search by name or description
    private boolean onlyActivePromotions; // optional: show only currently active promos

    public GetCatalogRequest() {}

    public GetCatalogRequest(String category, Long promotionId, String searchText, boolean onlyActivePromotions) {
        this.category = category;
        this.promotionId = promotionId;
        this.searchText = searchText;
        this.onlyActivePromotions = onlyActivePromotions;
    }

    public String getCategory() { return category; }
    public Long getPromotionId() { return promotionId; }
    public String getSearchText() { return searchText; }
    public boolean isOnlyActivePromotions() { return onlyActivePromotions; }
}
