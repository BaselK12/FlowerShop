package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;

import java.io.Serializable;
import java.util.List;

public class GetCatalogResponse implements Serializable {
    private List<Flower> flowers;

    public GetCatalogResponse(List<Flower> flowers) {
        this.flowers = flowers;
    }

    public List<Flower> getFlowers() {
        return flowers;
    }
}
