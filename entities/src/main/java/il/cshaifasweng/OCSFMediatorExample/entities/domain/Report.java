package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Report implements Serializable {
    public enum Type { SALES, ORDERS, COMPLAINTS, REFUNDS }

    private Type type;
    private LocalDateTime generatedAt;
    private Map<String, String> params;  // e.g. {"from":"2025-01-01","to":"2025-01-31"}
    private List<Map<String, Object>> rows;

    public Report() {}

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public Map<String, String> getParams() { return params; }
    public void setParams(Map<String, String> params) { this.params = params; }

    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
}
