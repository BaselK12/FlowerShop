package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class GetPaymentsRequest implements Serializable {
    private final String customerId;

    public GetPaymentsRequest(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() { return customerId; }
}
