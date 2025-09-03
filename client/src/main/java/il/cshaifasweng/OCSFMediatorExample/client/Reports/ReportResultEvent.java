package il.cshaifasweng.OCSFMediatorExample.client.Reports;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.ReportSchema;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.ChartSuggestion;
import java.util.List;
import java.util.Map;

public class ReportResultEvent {
    public final ReportSchema schema;
    public final List<Map<String,Object>> rows;
    public final ChartSuggestion chartSuggestion;

    public ReportResultEvent(ReportSchema schema, List<Map<String,Object>> rows, ChartSuggestion chartSuggestion) {
        this.schema = schema; this.rows = rows; this.chartSuggestion = chartSuggestion;
    }
}
