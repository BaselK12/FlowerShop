package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;

import java.io.Serializable;

public class FlowerUpdatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final FlowerDTO flower;

    public FlowerUpdatedEvent(FlowerDTO flower) {
        this.flower = flower;
    }

    public FlowerDTO getFlower() {
        return flower;
    }

    @Override
    public String toString() {
        return "FlowerUpdatedEvent{" +
                "flower=" + (flower != null ? flower.getSku() : "null") +
                '}';
    }
}
