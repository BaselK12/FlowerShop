package il.cshaifasweng.OCSFMediatorExample.server.handlers.reports;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.ErrorResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.*;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Reports.GetReportRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class GetReportHandler {

    public GetReportHandler(ServerBus bus) {
        bus.subscribe(GetReportRequestedEvent.class, evt -> {
            try {
                ReportRequest req = evt.getRequest().payload;
                if (req == null) {
                    bus.publish(new SendToClientEvent(new ErrorResponse("Empty report request"), evt.getClient()));
                    return;
                }

                // Load all orders
                List<Order> all = TX.call(s -> s.createQuery("from Order", Order.class).list());
                boolean hasCreatedAt = all.stream().anyMatch(o -> o.getCreatedAt() != null);

                // Completed-only filter
                if (req.completedOnly) {
                    all = all.stream()
                            .filter(o -> o.getStatus() == Order.Status.COMPLETED)
                            .collect(Collectors.toList());
                }

                // Date range filter (if there are nulls, we keep them out of time buckets anyway)
                LocalDate from = req.from;
                LocalDate to   = req.to;
                if (from != null || to != null) {
                    all = all.stream().filter(o -> {
                        LocalDate d = (o.getCreatedAt() == null) ? null : o.getCreatedAt().toLocalDate();
                        if (d == null) return false; // don’t let nulls pass a ranged query
                        if (from != null && d.isBefore(from)) return false;
                        if (to   != null && d.isAfter(to))     return false;
                        return true;
                    }).collect(Collectors.toList());
                }

                // Scope filter
                if (req.scope == Scope.STORE && req.storeId != null && !req.storeId.isBlank()) {
                    try {
                        long sid = Long.parseLong(req.storeId.trim());
                        all = all.stream()
                                .filter(o -> Objects.equals(o.getStoreId(), sid))
                                .collect(Collectors.toList());
                    } catch (NumberFormatException ignore) { /* user typed poetry into storeId */ }
                }

                // Decide grouping: prefer explicit groupBy, otherwise derive from granularity
                String gb = req.groupBy == null ? "" : req.groupBy.toLowerCase(Locale.ROOT);
                if (gb.isBlank()) {
                    if (req.granularity == Granularity.MONTHLY) gb = "month";
                    else if (req.granularity == Granularity.DAILY) gb = "day";
                }
                if (!List.of("day", "month", "store").contains(gb)) {
                    gb = hasCreatedAt ? "day" : "store";
                }
                if (!hasCreatedAt && (gb.equals("day") || gb.equals("month"))) {
                    gb = "store"; // hard degrade if literally no timestamps
                }

                ReportSchema schema = new ReportSchema();
                List<Map<String, Object>> rows = new ArrayList<>();
                ChartSuggestion chart = new ChartSuggestion();
                chart.valueKey = "orders";

                switch (gb) {
                    case "store" -> {
                        schema.columns.add(new ColumnDef("store",  "Store",  "string"));
                        schema.columns.add(new ColumnDef("orders", "Orders", "number"));

                        Map<String, Long> m = all.stream().collect(
                                Collectors.groupingBy(
                                        o -> String.valueOf(o.getStoreId()),
                                        LinkedHashMap::new,
                                        Collectors.counting()
                                )
                        );

                        m.forEach((k, v) -> {
                            Map<String, Object> r = new LinkedHashMap<>();
                            r.put("store", k);
                            r.put("orders", v.intValue());
                            rows.add(r);
                        });

                        chart.kind = ChartKind.BAR;
                        chart.categoryKey = "store";
                    }

                    case "month" -> {
                        schema.columns.add(new ColumnDef("month",  "Month",  "string"));
                        schema.columns.add(new ColumnDef("orders", "Orders", "number"));

                        Map<YearMonth, Long> m = all.stream()
                                .filter(o -> o.getCreatedAt() != null)
                                .collect(Collectors.groupingBy(
                                        o -> YearMonth.from(o.getCreatedAt()),
                                        TreeMap::new,     // sorted by YearMonth
                                        Collectors.counting()
                                ));

                        m.forEach((ym, cnt) -> {
                            Map<String, Object> r = new LinkedHashMap<>();
                            r.put("month", ym.toString()); // ISO yyyy-MM
                            r.put("orders", cnt.intValue());
                            rows.add(r);
                        });

                        chart.kind = ChartKind.BAR;
                        chart.categoryKey = "month";
                    }

                    case "day" -> {
                        schema.columns.add(new ColumnDef("date",   "Date",   "date"));
                        schema.columns.add(new ColumnDef("orders", "Orders", "number"));

                        Map<LocalDate, Long> m = all.stream()
                                .filter(o -> o.getCreatedAt() != null)
                                .collect(Collectors.groupingBy(
                                        o -> o.getCreatedAt().toLocalDate(),
                                        TreeMap::new,    // sorted by LocalDate
                                        Collectors.counting()
                                ));

                        m.forEach((d, cnt) -> {
                            Map<String, Object> r = new LinkedHashMap<>();
                            r.put("date", d.toString());   // ISO yyyy-MM-dd
                            r.put("orders", cnt.intValue());
                            rows.add(r);
                        });

                        chart.kind = ChartKind.LINE;
                        chart.categoryKey = "date";
                    }
                }

                ReportResponse resp = new ReportResponse();
                resp.type = req.type;
                resp.schema = schema;
                resp.rows = rows;
                resp.chartSuggestion = chart;

                bus.publish(new SendToClientEvent(new GetReportResponse(resp), evt.getClient()));
            } catch (Exception e) {
                e.printStackTrace();
                bus.publish(new SendToClientEvent(new ErrorResponse("Failed to build report: " + e.getMessage()), evt.getClient()));
            }
        });
    }
}
