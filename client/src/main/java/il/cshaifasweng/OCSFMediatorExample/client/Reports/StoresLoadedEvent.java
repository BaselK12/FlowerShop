package il.cshaifasweng.OCSFMediatorExample.client.Reports;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.StoreOption;
import java.util.List;

public class StoresLoadedEvent {
    public final List<StoreOption> stores;
    public StoresLoadedEvent(List<StoreOption> stores) { this.stores = stores; }
}
