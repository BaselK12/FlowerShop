package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;
import java.util.List;

public class CatalogResponse implements Serializable {
    private List<FlowerDTO> items;

    public CatalogResponse() {}
    public CatalogResponse(List<FlowerDTO> items) { this.items = items; }

    public List<FlowerDTO> getItems() { return items; }
    public void setItems(List<FlowerDTO> items) { this.items = items; }
}
