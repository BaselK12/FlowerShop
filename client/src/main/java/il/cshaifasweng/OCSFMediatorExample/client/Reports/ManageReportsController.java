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
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
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
            buildTable(ev.schema, ev.rows);
            buildCharts(ev.schema, ev.rows, ev.chartSuggestion);
            RowCountLabel.setText(Integer.toString(ev.rows.size()));
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
    }

    private void buildTable(ReportSchema schema, List<Map<String,Object>> data) {
        Table.getColumns().clear();
        rows.setAll(data);

        if (schema == null || schema.columns == null) return;

        for (ColumnDef col : schema.columns) {
            TableColumn<Map<String,Object>, Object> tc = new TableColumn<>(col.header);

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

    private void buildCharts(ReportSchema schema, List<Map<String,Object>> data, ChartSuggestion sugg) {
        BarChart.getData().clear();
        LineChart.getData().clear();
        PieChart.getData().clear();

        if (data == null || data.isEmpty() || schema == null || schema.columns == null || schema.columns.isEmpty())
            return;

        try {
            if (sugg != null && sugg.kind == ChartKind.PIE) {
                for (Map<String,Object> row : data) {
                    String cat = Objects.toString(row.get(sugg.categoryKey), "");
                    Number val = toNumber(row.get(sugg.valueKey));
                    if (val != null) {
                        PieChart.Data slice = new PieChart.Data(cat, val.doubleValue());
                        PieChart.getData().add(slice);
                    }
                }
                return;
            }

            String categoryKey = (sugg != null && sugg.categoryKey != null) ? sugg.categoryKey : schema.columns.get(0).key;
            String valueKey = (sugg != null && sugg.valueKey != null) ? sugg.valueKey : firstNumeric(schema);
            String seriesKey = (sugg != null) ? sugg.seriesKey : null;
            if (valueKey == null) return;

            if (seriesKey == null) {
                // Single series
                XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
                XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
                barSeries.setName(valueKey);
                lineSeries.setName(valueKey);

                for (Map<String,Object> row : data) {
                    String x = Objects.toString(row.get(categoryKey), "");
                    Number y = toNumber(row.get(valueKey));
                    if (y != null) {
                        barSeries.getData().add(new XYChart.Data<>(x, y));
                        lineSeries.getData().add(new XYChart.Data<>(x, y));
                    }
                }
                BarChart.getData().add(barSeries);
                LineChart.getData().add(lineSeries);
            } else {
                // Multiple series
                Map<String, XYChart.Series<String, Number>> barSeriesMap = new LinkedHashMap<>();
                Map<String, XYChart.Series<String, Number>> lineSeriesMap = new LinkedHashMap<>();

                for (Map<String,Object> row : data) {
                    String series = Objects.toString(row.get(seriesKey), "");
                    barSeriesMap.computeIfAbsent(series, k -> {
                        XYChart.Series<String, Number> s = new XYChart.Series<>();
                        s.setName(k);
                        return s;
                    });
                    lineSeriesMap.computeIfAbsent(series, k -> {
                        XYChart.Series<String, Number> s = new XYChart.Series<>();
                        s.setName(k);
                        return s;
                    });

                    String x = Objects.toString(row.get(categoryKey), "");
                    Number y = toNumber(row.get(valueKey));
                    if (y != null) {
                        barSeriesMap.get(series).getData().add(new XYChart.Data<>(x, y));
                        lineSeriesMap.get(series).getData().add(new XYChart.Data<>(x, y));
                    }
                }
                BarChart.getData().addAll(barSeriesMap.values());
                LineChart.getData().addAll(lineSeriesMap.values());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String firstNumeric(ReportSchema schema) {
        for (ColumnDef c : schema.columns) {
            if ("number".equalsIgnoreCase(c.type)) return c.key;
        }
        return null;
    }

    private static Number toNumber(Object o) {
        if (o instanceof Number n) return n;
        try { return o == null ? null : Double.parseDouble(o.toString()); }
        catch (Exception e) { return null; }
    }


}
