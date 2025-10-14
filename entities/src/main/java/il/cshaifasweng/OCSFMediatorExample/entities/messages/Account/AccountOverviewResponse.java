package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import java.io.Serializable;

public class AccountOverviewResponse implements Serializable {
    private final boolean ok;
    private final String reason;
    private final CustomerDTO customer;

    public AccountOverviewResponse(boolean ok, String reason, CustomerDTO customer) {
        this.ok = ok;
        this.reason = reason;
        this.customer = customer;
    }

    // boolean
    public boolean isOk() { return ok; }
    public boolean ok()   { return ok; }

    // reason
    public String getReason() { return reason; }
    public String reason()    { return reason; }

    // payload
    public CustomerDTO getCustomer() { return customer; }
    public CustomerDTO customer()    { return customer; }

    public static AccountOverviewResponse fail(String reason) {
        return new AccountOverviewResponse(false, reason, null);
    }
    public static AccountOverviewResponse success(CustomerDTO c) {
        return new AccountOverviewResponse(true, null, c);
    }
}
