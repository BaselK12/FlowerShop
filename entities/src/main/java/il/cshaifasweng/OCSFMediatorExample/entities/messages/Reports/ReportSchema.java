package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportSchema implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public List<ColumnDef> columns = new ArrayList<>();

    public ReportSchema() {}
    public ReportSchema(List<ColumnDef> columns) { this.columns = columns; }
}