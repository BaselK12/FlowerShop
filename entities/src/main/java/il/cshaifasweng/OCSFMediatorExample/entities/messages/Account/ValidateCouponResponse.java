package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class ValidateCouponResponse implements Serializable {
    private boolean valid;
    private String code;
    private String title;
    // Discount is expressed two ways so the client can render clearly
    private String discountType; // "PERCENT" or "FIXED"
    private double amount;       // percent if PERCENT, absolute USD if FIXED
    private String message;

    public ValidateCouponResponse() {}

    public ValidateCouponResponse(boolean valid, String code, String title,
                                  String discountType, double amount, String message) {
        this.valid = valid;
        this.code = code;
        this.title = title;
        this.discountType = discountType;
        this.amount = amount;
        this.message = message;
    }

    public boolean isValid() { return valid; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDiscountType() { return discountType; }
    public double getAmount() { return amount; }
    public String getMessage() { return message; }
}
