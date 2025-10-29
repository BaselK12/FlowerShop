package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.common.RequiresSession;
import il.cshaifasweng.OCSFMediatorExample.client.ui.ViewTracker;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Status;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
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
import org.greenrobot.eventbus.ThreadMode;

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
        ordersTable.setPlaceholder(new Label("No orders yet."));

        // Status filter options (keep your enum spelling)
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

        // Boot immediately if we already have a session
        long id = ClientSession.getCustomerId();
        if (id > 0) {
            setCustomerId(id);
        } else {
            ordersEmptyLabel.setText("Log in to view your orders.");
            updateUIState();
        }
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
            if (customerId <= 0) return;
            // your server expects String customerId, fine
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

    // Kick off loading when login completes
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        long id = ClientSession.getCustomerId();
        if (id > 0) setCustomerId(id);
    }

    // Refresh when hydrated overview arrives
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;
        long id = ClientSession.getCustomerId();
        if (id > 0) setCustomerId(id);
    }

    // Optional: reload if user navigates here and data is empty
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActive(ViewTracker.ActiveControllerChanged e) {
        if (e == null) return;
        String id = e.controllerId != null ? e.controllerId : e.getControllerId();
        if ("PastOrdersView".equals(id) && orders.isEmpty() && ClientSession.getCustomerId() > 0) {
            requestOrders();
        }
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
