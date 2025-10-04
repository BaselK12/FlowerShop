package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import java.io.Serializable;
import java.util.List;

public class GetCatalogResponse implements Serializable {
    private List<FlowerDTO> flowers;

    public GetCatalogResponse(List<FlowerDTO> flowers) {
        this.flowers = flowers;
    }

    public List<FlowerDTO> getFlowers() {
        return flowers;
    }
}
