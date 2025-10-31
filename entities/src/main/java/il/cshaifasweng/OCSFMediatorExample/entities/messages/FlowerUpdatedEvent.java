package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;

public class FlowerUpdatedEvent {
    private final FlowerDTO flower;

    public FlowerUpdatedEvent(FlowerDTO flower) {
        this.flower = flower;
    }

    public FlowerDTO getFlower() {
        return flower;
    }
}
