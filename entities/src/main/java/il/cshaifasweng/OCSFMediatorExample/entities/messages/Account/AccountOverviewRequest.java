package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class AccountOverviewRequest implements Serializable {
    private final long customerId; // 0 means "infer from session" if you support that on server

    public AccountOverviewRequest(long customerId) {
        this.customerId = customerId;
    }

    public long getCustomerId() { return customerId; }
    public long customerId()    { return customerId; } // convenience accessor
}
