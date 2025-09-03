package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serial;
import java.io.Serializable;

public class ColumnDef implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    // key in each row map, e.g. "date", "store", "revenue"
    public String key;
    // column header for the UI
    public String header;
    // "string" | "number" | "date" (used by client to pick renderers/axes)
    public String type;

    public ColumnDef() {}
    public ColumnDef(String key, String header, String type) {
        this.key = key; this.header = header; this.type = type;
    }
}
