package il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog;

import java.io.Serializable;

public class GetCatalogRequest implements Serializable {
    private String filter;

    public GetCatalogRequest() {
        // default for frameworks (required!)
    }

    public GetCatalogRequest(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
