package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

public class ReportRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public ReportType type;        // SALES, REVENUE, ...
    public Scope scope;            // COMPANY or STORE
    public String storeId;         // only if scope == STORE

    public LocalDate from;         // inclusive
    public LocalDate to;           // inclusive

    public Granularity granularity; // DAILY/WEEKLY/MONTHLY/QUARTERLY
    public String groupBy;          // e.g., "store","product","category","employee","day","month"

    public boolean completedOnly;   // orders filter (keep this per your UI)
}
