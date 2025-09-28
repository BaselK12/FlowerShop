package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ComplaintsFetchRequestedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String status;        // e.g. "OPEN", "IN_PROGRESS"
    private final String type;          // e.g. "Delivery", "Service"
    private final Long storeId;         // filter by store
    private final Long customerId;      // filter by customer
    private final Long orderId;         // filter by order
    private final LocalDateTime from;   // filter from date (createdAt >= from)
    private final LocalDateTime to;     // filter to date   (createdAt <= to)
    private final String q;             // free-text search in complaint text
    private final String sortBy;        // field name to sort by
    private final boolean sortDesc;     // true = descending, false = ascending
    private final int page;             // page index (0-based)
    private final int pageSize;         // number of results per page
    private final transient ConnectionToClient client;


    // --- constructor ---
    public ComplaintsFetchRequestedEvent(
            String status,
            String type,
            Long storeId,
            Long customerId,
            Long orderId,
            LocalDateTime from,
            LocalDateTime to,
            String q,
            String sortBy,
            boolean sortDesc,
            int page,
            int pageSize,
            ConnectionToClient client
    ) {
        this.status = status;
        this.type = type;
        this.storeId = storeId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.from = from;
        this.to = to;
        this.q = q;
        this.sortBy = sortBy;
        this.sortDesc = sortDesc;
        this.page = page;
        this.pageSize = pageSize;
        this.client = client;
    }

    // --- getters ---
    public String getStatus() { return status; }
    public String getType() { return type; }
    public Long getStoreId() { return storeId; }
    public Long getCustomerId() { return customerId; }
    public Long getOrderId() { return orderId; }
    public LocalDateTime getFrom() { return from; }
    public LocalDateTime getTo() { return to; }
    public String getQ() { return q; }
    public String getSortBy() { return sortBy; }
    public boolean isSortDesc() { return sortDesc; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }

    @Override
    public String toString() {
        return "ComplaintsFetchRequestedEvent{" +
                "status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", storeId=" + storeId +
                ", customerId=" + customerId +
                ", orderId=" + orderId +
                ", from=" + from +
                ", to=" + to +
                ", q='" + q + '\'' +
                ", sortBy='" + sortBy + '\'' +
                ", sortDesc=" + sortDesc +
                ", page=" + page +
                ", pageSize=" + pageSize +
                '}';
    }

    public ConnectionToClient getClient() {
        return client;
    }
}