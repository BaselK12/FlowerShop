package il.cshaifasweng.OCSFMediatorExample.entities.messages.DeliveryPickup;

import java.io.Serializable;

public class PickupInfoResponse implements Serializable {
    private boolean success;
    private String message;

    public PickupInfoResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
