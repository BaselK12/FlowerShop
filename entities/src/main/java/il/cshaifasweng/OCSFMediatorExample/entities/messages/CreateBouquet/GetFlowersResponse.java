package il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;

import java.io.Serializable;
import java.util.List;

public class GetFlowersResponse implements Serializable {
    private final List<FlowerDTO> flowers;
    private final String message;

    public GetFlowersResponse(List<FlowerDTO> flowers) {
        this.flowers = flowers;
        this.message = null;
    }

    public GetFlowersResponse(List<FlowerDTO> flowers, String message) {
        this.flowers = flowers;
        this.message = message;
    }

    public List<FlowerDTO> getFlowers() { return flowers; }
    public String getMessage() { return message; }
}
