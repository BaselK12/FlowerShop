package il.cshaifasweng.OCSFMediatorExample.entities.messages.Account;

import java.io.Serializable;

public class GetCouponsRequest implements Serializable {
    private int page; // 0-based
    private int size; // items per page

    public GetCouponsRequest() {}
    public GetCouponsRequest(int page, int size) { this.page = page; this.size = size; }

    public int getPage() { return page; }
    public int getSize() { return size; }
}
