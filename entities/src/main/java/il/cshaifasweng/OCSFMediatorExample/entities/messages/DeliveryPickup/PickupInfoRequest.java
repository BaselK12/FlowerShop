package il.cshaifasweng.OCSFMediatorExample.entities.messages.DeliveryPickup;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.PickupInfo;

import java.io.Serializable;

public class PickupInfoRequest implements Serializable {
    private PickupInfo pickupInfo;

    public PickupInfoRequest(PickupInfo pickupInfo) {
        this.pickupInfo = pickupInfo;
    }

    public PickupInfo getPickupInfo() {
        return pickupInfo;
    }

    public void setPickupInfo(PickupInfo pickupInfo) {
        this.pickupInfo = pickupInfo;
    }
}