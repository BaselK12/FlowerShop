package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serializable;
import java.util.List;

public class GetStoresResponse implements Serializable {
    public List<StoreOption> stores;
    public GetStoresResponse() {}
    public GetStoresResponse(List<StoreOption> stores) { this.stores = stores; }
}
