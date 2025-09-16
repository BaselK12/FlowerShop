package il.cshaifasweng.OCSFMediatorExample.client.Account;


import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PaymentsViewController {

    // Header
    @FXML private Label paymentsTitleLabel;
    @FXML private Label paymentsCountLabel;
    @FXML private Button btnRefreshPayments;

    // Table
    @FXML private StackPane paymentsTableStack;
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, String> colCardType;
    @FXML private TableColumn<Payment, String> colCardNumber;
    @FXML private TableColumn<Payment, String> colExpiryDate; // currently placeholder

    // Empty state
    @FXML private VBox paymentsEmptyBox;
    @FXML private Button btnEmptyAddPayment;

    // Actions
    @FXML private HBox paymentsActions;
    @FXML private Button btnRemovePayment;
    @FXML private Button btnAddPayment;

    // Data
    private final ObservableList<Payment> payments = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Column bindings
        colCardType.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMethod() != null ? data.getValue().getMethod().name() : ""
                )
        );

        colCardNumber.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMaskedPan() != null ? data.getValue().getMaskedPan() : ""
                )
        );

        // Expiry date is not in your Payment entity right now
        colExpiryDate.setCellValueFactory(data ->
                new SimpleStringProperty("") // empty until entity includes expiry
        );

        paymentsTable.setItems(payments);

        // Hook up buttons
        btnRefreshPayments.setOnAction(e -> loadPayments());
        btnAddPayment.setOnAction(e -> addPayment());
        btnEmptyAddPayment.setOnAction(e -> addPayment());
        btnRemovePayment.setOnAction(e -> removeSelectedPayment());

        // Initial load
        loadPayments();
    }

    private void loadPayments() {
        payments.clear();

        // TODO: Replace with server call
        Payment p1 = new Payment();
        p1.setId("PAY123");
        p1.setMethod(Payment.Method.CREDIT_CARD);
        p1.setMaskedPan("**** 1234");
        p1.setStatus(Payment.Status.AUTHORIZED);
        p1.setAmount(49.99);

        Payment p2 = new Payment();
        p2.setId("PAY124");
        p2.setMethod(Payment.Method.CREDIT_CARD);
        p2.setMaskedPan("**** 5678");
        p2.setStatus(Payment.Status.CAPTURED);
        p2.setAmount(19.95);

        payments.addAll(p1, p2);

        updateUIState();
    }

    private void updateUIState() {
        paymentsCountLabel.setText("(" + payments.size() + ")");
        boolean empty = payments.isEmpty();
        paymentsEmptyBox.setVisible(empty);
        paymentsTable.setVisible(!empty);
    }

    private void addPayment() {
        // TODO: Replace with dialog or integration with payment gateway
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Add Payment Method dialog goes here.");
        alert.showAndWait();
    }

    private void removeSelectedPayment() {
        Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a payment method to remove.").showAndWait();
            return;
        }

        // TODO: Send delete request to server
        payments.remove(selected);
        updateUIState();
    }
}
