package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class ValidateCouponRequest implements Serializable {
    private String code;

    public ValidateCouponRequest() {}
    public ValidateCouponRequest(String code) { this.code = code; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
