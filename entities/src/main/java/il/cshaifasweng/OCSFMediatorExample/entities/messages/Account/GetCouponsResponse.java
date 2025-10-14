package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;
import java.util.List;

public class GetCouponsResponse implements Serializable {
    private final List<CouponDTO> items;
    private final int totalCount;
    private final int page;
    private final int size;

    public GetCouponsResponse(List<CouponDTO> items, int totalCount, int page, int size) {
        this.items = items; this.totalCount = totalCount; this.page = page; this.size = size;
    }
    public List<CouponDTO> getItems() { return items; }
    public int getTotalCount() { return totalCount; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
