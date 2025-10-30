package il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.PromotionDTO;

import java.io.Serializable;

public class AddPromotionsResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private PromotionDTO promotion; // the created or updated promotion

    public AddPromotionsResponse() {}

    public AddPromotionsResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AddPromotionsResponse(boolean success, String message, PromotionDTO promotion) {
        this.success = success;
        this.message = message;
        this.promotion = promotion;
    }

    // --- Getters & Setters ---
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public PromotionDTO getPromotion() { return promotion; }
    public void setPromotion(PromotionDTO promotion) { this.promotion = promotion; }

    @Override
    public String toString() {
        return "AddPromotionsResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", promotion=" + (promotion != null ? promotion.getName() : "null") +
                '}';
    }
}
