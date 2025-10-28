package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

/** Set the current customer's store (null => Global). */
public class SetStoreRequest implements Serializable {
    private final Long storeId; // null for Global

    public SetStoreRequest(Long storeId) {
        this.storeId = storeId;
    }

    public Long getStoreId() { return storeId; }
}
