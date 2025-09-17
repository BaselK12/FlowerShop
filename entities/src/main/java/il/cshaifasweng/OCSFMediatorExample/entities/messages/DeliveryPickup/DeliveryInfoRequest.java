package il.cshaifasweng.OCSFMediatorExample.entities.messages.DeliveryPickup;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.DeliveryInfo;

import java.io.Serializable;

public class DeliveryInfoRequest implements Serializable {
    private DeliveryInfo deliveryInfo;

    public DeliveryInfoRequest(DeliveryInfo deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }

    public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }
}