package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class UpdateCustomerProfileRequest implements Serializable {
    private final long customerId; // 0 = infer from session if you have it
    private final String displayName;
    private final String email;
    private final String phone;

    public UpdateCustomerProfileRequest(long customerId, String displayName, String email, String phone) {
        this.customerId = customerId;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
    }

    public long getCustomerId()   { return customerId; }
    public String getDisplayName(){ return displayName; }
    public String getEmail()      { return email; }
    public String getPhone()      { return phone; }

    // convenience accessors to match your client style
    public long customerId()        { return customerId; }
    public String displayName()     { return displayName; }
    public String email()           { return email; }
    public String phone()           { return phone; }
}
