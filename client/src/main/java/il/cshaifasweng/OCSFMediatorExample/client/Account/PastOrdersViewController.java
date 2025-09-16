package il.cshaifasweng.OCSFMediatorExample.client.Account;


import il.cshaifasweng.OCSFMediatorExample.entities.domain.Order;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PastOrdersViewController {

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

    // Date formatter
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        // Column bindings
        colOrderId.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getId())
        );

        colOrderDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCreatedAt() != null
                                ? data.getValue().getCreatedAt().format(dateFormatter)
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

        ordersTable.setItems(orders);

        // Fill status filter
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "PENDING", "PAID", "PREPARING", "SHIPPED", "DELIVERED", "CANCELED"
        ));
        statusFilter.getSelectionModel().select("All");

        // Hook up buttons
        btnRefreshOrders.setOnAction(e -> loadOrders());
        btnApplyFilters.setOnAction(e -> applyFilters());
        btnOrderDetails.setOnAction(e -> showOrderDetails());
        btnEmptyShopNow.setOnAction(e -> handleShopNow());

        // Initial load
        loadOrders();
    }

    private void loadOrders() {
        orders.clear();

        // TODO: Replace with server call
        // Example mock data:
        Order order1 = new Order();
        order1.setId("ORD1001");
        order1.setCreatedAt(java.time.LocalDateTime.now().minusDays(5));
        order1.setTotal(59.99);
        order1.setStatus(Order.Status.DELIVERED);

        Order order2 = new Order();
        order2.setId("ORD1002");
        order2.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
        order2.setTotal(24.50);
        order2.setStatus(Order.Status.PENDING);

        orders.addAll(order1, order2);

        updateUIState();
    }

    private void applyFilters() {
        // For now, just reload all
        // Later: filter by orderSearchField, fromDatePicker, toDatePicker, statusFilter
        loadOrders();
    }

    private void updateUIState() {
        ordersCountLabel.setText("(" + orders.size() + ")");
        boolean empty = orders.isEmpty();
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
                ? selected.getCreatedAt().format(dateFormatter)
                : "") +
                "\nTotal: $" + String.format("%.2f", selected.getTotal()) +
                "\nStatus: " + (selected.getStatus() != null ? selected.getStatus().name() : "");

        new Alert(Alert.AlertType.INFORMATION, details).showAndWait();
    }

    private void handleShopNow() {
        // TODO: navigate back to catalog scene
        new Alert(Alert.AlertType.INFORMATION, "Go to catalog to shop now.").showAndWait();
    }
}
