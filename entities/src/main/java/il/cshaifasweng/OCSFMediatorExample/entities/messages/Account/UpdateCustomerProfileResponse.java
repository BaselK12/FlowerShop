package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import java.io.Serializable;

public class UpdateCustomerProfileResponse implements Serializable {
    private final boolean ok;
    private final String reason;
    private final CustomerDTO customer;

    public UpdateCustomerProfileResponse(boolean ok, String reason, CustomerDTO customer) {
        this.ok = ok;
        this.reason = reason;
        this.customer = customer;
    }

    public boolean isOk() { return ok; }
    public boolean ok()   { return ok; }

    public String getReason() { return reason; }
    public String reason()    { return reason; }

    public CustomerDTO getCustomer() { return customer; }
    public CustomerDTO customer()    { return customer; }

    public static UpdateCustomerProfileResponse fail(String reason) {
        return new UpdateCustomerProfileResponse(false, reason, null);
    }
    public static UpdateCustomerProfileResponse success(CustomerDTO c) {
        return new UpdateCustomerProfileResponse(true, null, c);
    }
}
