package il.cshaifasweng.OCSFMediatorExample.entities.messages;

import java.io.Serializable;
import java.util.Map;

public class ListCatalogRequest implements Serializable {
    private Map<String, String> filters; // optional key/value filters

    public ListCatalogRequest() {} // required for serialization

    public ListCatalogRequest(Map<String, String> filters) {
        this.filters = filters;
    }

    public Map<String, String> getFilters() { return filters; }
    public void setFilters(Map<String, String> filters) { this.filters = filters; }
}
