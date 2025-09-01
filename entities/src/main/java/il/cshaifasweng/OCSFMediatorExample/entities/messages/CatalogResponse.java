package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import java.io.Serializable;
import java.util.List;

public class CatalogResponse implements Serializable {
    private List<Flower> items;

    public CatalogResponse() {}

    public CatalogResponse(List<Flower> items) {
        this.items = items;
    }

    public List<Flower> getItems() { return items; }
    public void setItems(List<Flower> items) { this.items = items; }
}
