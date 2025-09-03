package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serial;
import java.io.Serializable;

public class ChartSuggestion implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public ChartKind kind = ChartKind.BAR; // default

    // X axis (or category in Pie)
    public String categoryKey;

    // Y axis numeric value (or slice value for Pie)
    public String valueKey;

    // Optional: split into multiple series (e.g., "store")
    public String seriesKey;
}
