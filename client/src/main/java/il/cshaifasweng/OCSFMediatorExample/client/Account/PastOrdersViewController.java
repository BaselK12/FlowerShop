package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.RequiresSession;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Status;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class PastOrdersViewController implements RequiresSession {

    // Header
    @FXML private Label ordersTitleLabel;
    @FXML private Label ordersCountLabel;
    @FXML private Button btnRefreshOrders;

    // Filters
    @FXML private TextField orderSearchField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button btnApplyFilters;

    // Table
    @FXML private StackPane ordersTableStack;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colOrderId;
    @FXML private TableColumn<Order, String> colOrderDate;
    @FXML private TableColumn<Order, String> colOrderTotal;
    @FXML private TableColumn<Order, String> colOrderStatus;

    // Empty state
    @FXML private VBox ordersEmptyBox;
    @FXML private Label ordersEmptyLabel;
    @FXML private Button btnEmptyShopNow;

    // Actions
    @FXML private HBox ordersActions;
    @FXML private Button btnOrderDetails;

    // Data
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private FilteredList<Order> filtered;
    private long customerId = -1;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Table columns
        colOrderId.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getId()))
        );

        colOrderDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCreatedAt() != null
                                ? data.getValue().getCreatedAt().toLocalDate().format(dateFormatter)
                                : ""
                )
        );

        colOrderTotal.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("$%.2f", data.getValue().getTotal()))
        );

        colOrderStatus.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatus() != null ? data.getValue().getStatus().name() : ""
                )
        );

        filtered = new FilteredList<>(orders, o -> true);
        ordersTable.setItems(filtered);

        // Status filter options
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", Status.PENDING.name(), Status.PAID.name(), Status.PREPARING.name(),
                Status.SHIPPED.name(), Status.DELIVERED.name(), Status.CANCELED.name()
        ));
        statusFilter.getSelectionModel().select("All");

        // Buttons
        btnRefreshOrders.setOnAction(e -> requestOrders());
        btnApplyFilters.setOnAction(e -> applyFilters());
        btnOrderDetails.setOnAction(e -> showOrderDetails());
        btnEmptyShopNow.setOnAction(e -> handleShopNow());

        updateUIState();
    }

    @Override
    public void setCustomerId(long customerId) {
        this.customerId = customerId;
        requestOrders();
    }

    private void requestOrders() {
        try {
            orders.clear();
            updateUIState();
            SimpleClient.getClient().sendSafely(new GetOrdersRequest(String.valueOf(customerId)));
        } catch (Exception e) {
            showError("Failed to request orders: " + e.getMessage());
        }
    }

    @Subscribe
    public void onOrders(GetOrdersResponse resp) {
        Platform.runLater(() -> {
            if (resp == null || resp.getOrders() == null) {
                updateUIState();
                return;
            }
            orders.setAll(resp.getOrders());
            applyFilters(); // reapply current filters to fresh data
            updateUIState();
        });
    }

    private void applyFilters() {
        String q = orderSearchField.getText() == null ? "" : orderSearchField.getText().trim().toLowerCase();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to   = toDatePicker.getValue();
        String status = statusFilter.getSelectionModel().getSelectedItem();
        boolean filterAll = status == null || "All".equals(status);

        filtered.setPredicate(o -> {
            if (o == null) return false;

            boolean matchesText = q.isEmpty()
                    || String.valueOf(o.getId()).contains(q);

            boolean matchesDate = true;
            if (from != null && o.getCreatedAt() != null) {
                matchesDate &= !o.getCreatedAt().toLocalDate().isBefore(from);
            }
            if (to != null && o.getCreatedAt() != null) {
                matchesDate &= !o.getCreatedAt().toLocalDate().isAfter(to);
            }

            boolean matchesStatus = filterAll
                    || (o.getStatus() != null && Objects.equals(o.getStatus().name(), status));

            return matchesText && matchesDate && matchesStatus;
        });

        updateUIState();
    }

    private void updateUIState() {
        int count = filtered == null ? 0 : filtered.size();
        ordersCountLabel.setText("(" + count + ")");
        boolean empty = count == 0;
        ordersEmptyBox.setVisible(empty);
        ordersTable.setVisible(!empty);
    }

    private void showOrderDetails() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an order first.").showAndWait();
            return;
        }
        String details = "Order ID: " + selected.getId() +
                "\nDate: " + (selected.getCreatedAt() != null
                ? selected.getCreatedAt().toLocalDate().format(dateFormatter)
                : "") +
                "\nTotal: $" + String.format("%.2f", selected.getTotal()) +
                "\nStatus: " + (selected.getStatus() != null ? selected.getStatus().name() : "");
        new Alert(Alert.AlertType.INFORMATION, details).showAndWait();
    }

    @FXML
    private void handleShopNow() {
        // Jump to Catalog
        Nav.go(ordersTableStack, "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml");
    }

    private static void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
