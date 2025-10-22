package il.cshaifasweng.OCSFMediatorExample.client.Reports;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.*;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ManageReportsController {

    // Tab Pane
    @FXML private TabPane ChartsTabs;

    // Charts
    @FXML private BarChart<String, Number> BarChart;
    @FXML private PieChart PieChart;
    @FXML private LineChart<String, Number> LineChart;

    // Progress Indicator
    @FXML private ProgressIndicator Loading;

    // CheckBox
    @FXML private CheckBox OnlyCompletedOrdersBox;

    // buttons
    @FXML private Button QTRBtn;
    @FXML private Button RunBtn;
    @FXML private Button D30Btn;
    @FXML private Button D7Btn;
    @FXML private Button ExportPDFBtn;

    // Labels
    @FXML private Label RowCountLabel;

    // ComboBox
    @FXML private ComboBox<ReportType> ReportTypeBox;
    @FXML private ComboBox<Scope> ScopeBox;
    @FXML private ComboBox<StoreOption> StoreBox;
    @FXML private ComboBox<Granularity> GranularityBox;
    @FXML private ComboBox<String> GroupByBox;

    // Date Picker
    @FXML private DatePicker ToDate;
    @FXML private DatePicker FromDate;

    // Table view
    @FXML private TableView<Map<String,Object>> Table;

    private final ObservableList<Map<String,Object>> rows = FXCollections.observableArrayList();
    private ReportSchema lastSchema;
    private List<Map<String,Object>> lastRows = new ArrayList<>();
    private ReportRequest lastRequest;

    @FXML private void initialize() {

        // subscribe
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        // Defaults
        LocalDate today = LocalDate.now();
        FromDate.setValue(today.minusDays(7));
        ToDate.setValue(today);

        ReportTypeBox.setItems(FXCollections.observableArrayList(ReportType.values()));
        ScopeBox.setItems(FXCollections.observableArrayList(Scope.values()));
        GranularityBox.setItems(FXCollections.observableArrayList(Granularity.values()));
        GroupByBox.setItems(FXCollections.observableArrayList("store","product","category","employee","day","month"));

        ScopeBox.getSelectionModel().select(Scope.COMPANY);
        ReportTypeBox.getSelectionModel().select(ReportType.SALES);
        GranularityBox.getSelectionModel().select(Granularity.DAILY);

        // Store box disabled unless scope=STORE
        StoreBox.setDisable(true);
        ScopeBox.valueProperty().addListener((obs, oldVal, val) -> {
            StoreBox.setDisable(val != Scope.STORE);
        });

        // Table base
        Table.setItems(rows);
        RowCountLabel.setText("0");
        setLoading(false);
        ExportPDFBtn.setDisable(true);


        try {
            // Ask server for stores
            SimpleClient.getClient().sendToServer(new GetStoresRequest());
        } catch (Exception e) {
            setLoading(false);
            e.printStackTrace();
        }
    }




    @FXML
    void ExportPDF(ActionEvent event) {
        if (lastSchema == null || lastSchema.columns == null || lastSchema.columns.isEmpty() || lastRows.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Run a report before exporting.", ButtonType.OK).showAndWait();
            return;
        }

        Window owner = ExportPDFBtn.getScene() != null ? ExportPDFBtn.getScene().getWindow() : null;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

        String suggested = buildSuggestedFileName();
        if (suggested != null) {
            chooser.setInitialFileName(suggested);
        }

        File chosen = chooser.showSaveDialog(owner);
        if (chosen == null) {
            return;
        }

        File target = ensurePdfExtension(chosen);

        List<String> lines = buildPdfLines();

        try {
            writeSimplePdf(target, lines);
            new Alert(Alert.AlertType.INFORMATION,
                    "Report exported to:\n" + target.getAbsolutePath(), ButtonType.OK).showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Failed to export PDF: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private static ReportRequest copyRequest(ReportRequest original) {
        if (original == null) {
            return null;
        }
        ReportRequest copy = new ReportRequest();
        copy.type = original.type;
        copy.scope = original.scope;
        copy.storeId = original.storeId;
        copy.from = original.from;
        copy.to = original.to;
        copy.granularity = original.granularity;
        copy.groupBy = original.groupBy;
        copy.completedOnly = original.completedOnly;
        return copy;
    }

    private String buildSuggestedFileName() {
        if (lastRequest == null || lastRequest.type == null) {
            return null;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        StringBuilder name = new StringBuilder(lastRequest.type.name().toLowerCase(Locale.ROOT));
        if (lastRequest.from != null && lastRequest.to != null) {
            name.append("-").append(lastRequest.from.format(fmt)).append("-").append(lastRequest.to.format(fmt));
        }
        name.append("-report.pdf");
        return name.toString();
    }

    private List<String> buildPdfLines() {
        List<String> lines = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        if (lastRequest != null) {
            lines.add("Report type: " + lastRequest.type);
            lines.add("Scope: " + lastRequest.scope + (lastRequest.storeId != null ? " (#" + lastRequest.storeId + ")" : ""));
            LocalDate from = lastRequest.from;
            LocalDate to = lastRequest.to;
            if (from != null || to != null) {
                lines.add("Range: " + (from != null ? from.format(fmt) : "?") + " to " + (to != null ? to.format(fmt) : "?"));
            }
            lines.add("Completed orders only: " + (lastRequest.completedOnly ? "Yes" : "No"));
        } else {
            lines.add("Report export");
        }

        lines.add("");

        List<ColumnDef> columns = lastSchema.columns;
        int columnCount = columns.size();
        String[] headers = new String[columnCount];
        int[] widths = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
            String header = Objects.toString(columns.get(i).header, "");
            headers[i] = header;
            widths[i] = Math.max(widths[i], header.length());
        }

        List<String[]> dataRows = new ArrayList<>();
        for (Map<String, Object> row : lastRows) {
            String[] values = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                ColumnDef col = columns.get(i);
                String val = Objects.toString(row.get(col.key), "");
                values[i] = val;
                widths[i] = Math.max(widths[i], val.length());
            }
            dataRows.add(values);
        }

        if (columnCount > 0) {
            lines.add(buildRowLine(headers, widths));
            lines.add(buildDivider(widths));
            for (String[] values : dataRows) {
                lines.add(buildRowLine(values, widths));
            }
        } else {
            lines.add("(No columns defined)");
        }

        lines.add("");
        lines.add("Row count: " + lastRows.size());

        return lines;
    }

    private static String buildRowLine(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append(padRight(values[i], widths[i]));
        }
        return sb.toString();
    }

    private static String buildDivider(int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            if (i > 0) {
                sb.append("-+-");
            }
            sb.append(repeat('-', Math.max(1, widths[i])));
        }
        return sb.toString();
    }

    private static String padRight(String value, int width) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= width) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static String repeat(char c, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static File ensurePdfExtension(File chosen) {
        if (chosen == null) {
            return null;
        }
        String name = chosen.getName();
        if (name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return chosen;
        }
        File parent = chosen.getParentFile();
        if (parent == null) {
            return new File(name + ".pdf");
        }
        return new File(parent, name + ".pdf");
    }

    private static void writeSimplePdf(File target, List<String> lines) throws IOException {
        if (target == null) {
            throw new IOException("No file selected");
        }

        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        contentStream.write("BT\n".getBytes(StandardCharsets.US_ASCII));
        contentStream.write("/F1 12 Tf\n".getBytes(StandardCharsets.US_ASCII));
        contentStream.write("14 TL\n".getBytes(StandardCharsets.US_ASCII));
        contentStream.write("72 780 Td\n".getBytes(StandardCharsets.US_ASCII));

        if (lines == null || lines.isEmpty()) {
            contentStream.write("(No data) Tj\n".getBytes(StandardCharsets.US_ASCII));
        } else {
            for (String line : lines) {
                contentStream.write(("(" + escapePdfText(line) + ") Tj\n").getBytes(StandardCharsets.US_ASCII));
                contentStream.write("T*\n".getBytes(StandardCharsets.US_ASCII));
            }
        }

        contentStream.write("ET\n".getBytes(StandardCharsets.US_ASCII));
        byte[] content = contentStream.toByteArray();

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        writeAscii(pdf, "%PDF-1.4\n");

        int offset1 = pdf.size();
        writeAscii(pdf, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        int offset2 = pdf.size();
        writeAscii(pdf, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        int offset3 = pdf.size();
        writeAscii(pdf, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");

        int offset4 = pdf.size();
        writeAscii(pdf, "4 0 obj\n<< /Length " + content.length + " >>\nstream\n");
        pdf.write(content);
        writeAscii(pdf, "endstream\nendobj\n");

        int offset5 = pdf.size();
        writeAscii(pdf, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        int startxref = pdf.size();
        writeAscii(pdf, "xref\n0 6\n");
        writeAscii(pdf, "0000000000 65535 f \n");
        writeAscii(pdf, formatXref(offset1));
        writeAscii(pdf, formatXref(offset2));
        writeAscii(pdf, formatXref(offset3));
        writeAscii(pdf, formatXref(offset4));
        writeAscii(pdf, formatXref(offset5));
        writeAscii(pdf, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + startxref + "\n%%EOF\n");

        try (FileOutputStream fos = new FileOutputStream(target)) {
            pdf.writeTo(fos);
        }
    }

    private static String formatXref(int offset) {
        return String.format(Locale.ROOT, "%010d 00000 n \n", offset);
    }

    private static void writeAscii(ByteArrayOutputStream out, String text) {
        out.writeBytes(text.getBytes(StandardCharsets.US_ASCII));
    }

    private static String escapePdfText(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\\' || ch == '(' || ch == ')') {
                sb.append('\\');
            }
            if (ch < 0x20 && ch != '\t') {
                sb.append(' ');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    @FXML
    void Last7Days(ActionEvent e) {
        LocalDate t = LocalDate.now();
        FromDate.setValue(t.minusDays(7));
        ToDate.setValue(t);
    }

    @FXML
    void Last30Days(ActionEvent e) {
        LocalDate t = LocalDate.now();
        FromDate.setValue(t.minusDays(30));
        ToDate.setValue(t);
    }

    @FXML
    void ThisQuarter(ActionEvent e) {
        LocalDate t = LocalDate.now();
        Month first = t.getMonth().firstMonthOfQuarter();
        LocalDate start = LocalDate.of(t.getYear(), first, 1).with(TemporalAdjusters.firstDayOfMonth());
        FromDate.setValue(start);
        ToDate.setValue(t);
    }

    @FXML
    void runReport(ActionEvent event) {
        ReportRequest req = new ReportRequest();
        req.type = ReportTypeBox.getValue();
        req.scope = ScopeBox.getValue();
        StoreOption sel = StoreBox.getValue();
        req.storeId = (req.scope == Scope.STORE && sel != null) ? sel.id : null;
        req.from = FromDate.getValue();
        req.to = ToDate.getValue();
        req.granularity = GranularityBox.getValue();
        req.groupBy = GroupByBox.getValue();
        req.completedOnly = OnlyCompletedOrdersBox.isSelected();

        lastRequest = copyRequest(req);
        setLoading(true);

        try {
            // Ask server for stores
            SimpleClient.getClient().sendToServer(new GetReportRequest(req));
        } catch (Exception e) {
            setLoading(false);
            e.printStackTrace();
        }
    }


    // event bus funtions
    @Subscribe
    public void onStoresLoaded(StoresLoadedEvent ev) {
        Platform.runLater(() -> {
            StoreBox.setItems(FXCollections.observableArrayList(ev.stores));
            // Keep disabled unless scope=STORE (listener controls it)
        });
    }

    @Subscribe
    public void onReportResult(ReportResultEvent ev) {
        Platform.runLater(() -> {
            setLoading(false);
            lastSchema = ev.schema;
            lastRows = ev.rows == null ? new ArrayList<>() : new ArrayList<>(ev.rows);
            buildTable(ev.schema, ev.rows);
            buildCharts(ev.schema, ev.rows, ev.chartSuggestion);
            RowCountLabel.setText(Integer.toString(lastRows.size()));
            ExportPDFBtn.setDisable(lastRows.isEmpty());
        });
    }

    @Subscribe
    public void onReportError(ReportErrorEvent ev) {
        Platform.runLater(() -> {
            setLoading(false);
            new Alert(Alert.AlertType.ERROR, ev.message, ButtonType.OK).showAndWait();
        });
    }


    // helper function //

    private void setLoading(boolean v) {
        Loading.setVisible(v);
        RunBtn.setDisable(v);
        ExportPDFBtn.setDisable(v || lastRows.isEmpty());
    }

    private void buildTable(ReportSchema schema, List<Map<String,Object>> data) {
        Table.getColumns().clear();
        rows.setAll(data);

        if (schema == null || schema.columns == null) return;

        for (ColumnDef col : schema.columns) {
            TableColumn<Map<String, Object>, Object> tc = new TableColumn<>(col.header);

            // Column width & alignment by type
            if ("string".equalsIgnoreCase(col.type)) {
                tc.setPrefWidth(180);
            } else if ("number".equalsIgnoreCase(col.type)) {
                tc.setPrefWidth(100);
                tc.setStyle("-fx-alignment: CENTER-RIGHT;");
            } else if ("date".equalsIgnoreCase(col.type)) {
                tc.setPrefWidth(140);
            } else {
                tc.setPrefWidth(120);
            }

            // Disable sorting if you want consistent report order
            // tc.setSortable(false);
            tc.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(col.key)));
            Table.getColumns().add(tc);
        }
    }

    private void buildCharts(ReportSchema schema, List<Map<String,Object>> data, ChartSuggestion suggestion) {
        BarChart.getData().clear();
        LineChart.getData().clear();
        PieChart.getData().clear();

        if (data == null || data.isEmpty() || schema == null || schema.columns == null || schema.columns.isEmpty()) {
            return;
        }

        ChartKind kind = suggestion != null && suggestion.kind != null ? suggestion.kind : ChartKind.BAR;
        String categoryKey = suggestion != null ? suggestion.categoryKey : null;
        String valueKey = suggestion != null ? suggestion.valueKey : null;
        String seriesKey = suggestion != null ? suggestion.seriesKey : null;

        if (categoryKey == null || !hasColumn(schema, categoryKey)) {
            categoryKey = firstColumnOfType(schema, "string");
        }
        if (categoryKey == null && !schema.columns.isEmpty()) {
            categoryKey = schema.columns.get(0).key;
        }

        if (valueKey == null || !hasColumn(schema, valueKey)) {
            valueKey = firstColumnOfType(schema, "number");
        }
        if (valueKey == null) {
            for (ColumnDef column : schema.columns) {
                if (!Objects.equals(column.key, categoryKey)) {
                    valueKey = column.key;
                    break;
                }
            }
        }

        if (valueKey == null || categoryKey == null) {
            return;
        }

        switch (kind) {
            case LINE -> buildSeriesChart(LineChart, data, categoryKey, valueKey, seriesKey);
            case PIE -> buildPieChart(data, categoryKey, valueKey);
            case BAR -> buildSeriesChart(BarChart, data, categoryKey, valueKey, seriesKey);
        }

        selectChartTab(kind);
    }

    private void buildSeriesChart(XYChart<String, Number> chart,
                                  List<Map<String, Object>> data,
                                  String categoryKey,
                                  String valueKey,
                                  String seriesKey) {
        Map<String, XYChart.Series<String, Number>> seriesMap = new LinkedHashMap<>();

        for (Map<String, Object> row : data) {
            String category = Objects.toString(row.get(categoryKey), "(blank)");
            Double value = toDouble(row.get(valueKey));
            if (value == null) continue;

            String seriesName = valueKey;
            if (seriesKey != null && hasValue(row, seriesKey)) {
                seriesName = Objects.toString(row.get(seriesKey), "(blank)");
            }

            XYChart.Series<String, Number> series = seriesMap.computeIfAbsent(seriesName, key -> {
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName(key);
                return s;
            });
            series.getData().add(new XYChart.Data<>(category, value));
        }

        chart.getData().setAll(new ArrayList<>(seriesMap.values()));
    }

    private void buildPieChart(List<Map<String, Object>> data,
                               String categoryKey,
                               String valueKey) {
        Map<String, Double> slices = new LinkedHashMap<>();

        for (Map<String, Object> row : data) {
            String category = Objects.toString(row.get(categoryKey), "(blank)");
            Double value = toDouble(row.get(valueKey));
            if (value == null) continue;
            slices.merge(category, value, Double::sum);
        }

        List<PieChart.Data> pieData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : slices.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        PieChart.getData().setAll(pieData);
    }

    private void selectChartTab(ChartKind kind) {
        if (ChartsTabs == null || ChartsTabs.getTabs() == null) return;

        int index = switch (kind) {
            case BAR -> 0;
            case LINE -> 1;
            case PIE -> 2;
        };

        if (index >= 0 && index < ChartsTabs.getTabs().size()) {
            ChartsTabs.getSelectionModel().select(index);
        }
    }

    private boolean hasColumn(ReportSchema schema, String key) {
        if (schema == null || schema.columns == null) return false;
        for (ColumnDef column : schema.columns) {
            if (Objects.equals(column.key, key)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValue(Map<String, Object> row, String key) {
        return row.containsKey(key) && row.get(key) != null;
    }

    private String firstColumnOfType(ReportSchema schema, String type) {
        if (schema == null || schema.columns == null) return null;
        for (ColumnDef column : schema.columns) {
            if (type.equalsIgnoreCase(column.type)) {
                return column.key;
            }
        }
        return null;
    }

    private Double toDouble(Object raw) {
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        if (raw instanceof String str) {
            try {
                return Double.parseDouble(str.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
