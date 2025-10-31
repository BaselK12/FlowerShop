package il.cshaifasweng.OCSFMediatorExample.client.bus.events;

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
