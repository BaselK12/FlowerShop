package il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportResponse implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public ReportType type;
    public ReportSchema schema;

    // Each row: Map<columnKey, value>. Use only serializable values (String, Double, Integer, LocalDate, etc.)
    public List<Map<String, Object>> rows = new ArrayList<>();

    // optional total/footer metrics: e.g., {"revenue": 15023.45, "qty": 382}
    public Map<String, Number> totals = new HashMap<>();

    public ChartSuggestion chartSuggestion;
}