package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class RemovePaymentResponse implements Serializable {
    private final boolean success;
    private final String removedId;

    public RemovePaymentResponse(boolean success, String removedId) {
        this.success = success;
        this.removedId = removedId;
    }

    public boolean isSuccess() { return success; }
    public String getRemovedId() { return removedId; }
}
