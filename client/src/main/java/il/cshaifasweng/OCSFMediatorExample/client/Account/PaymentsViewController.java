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
    @FXML private TableColumn<Payment, String> colExpiryDate;
    @FXML private TableColumn<Payment, String> colAmount;
    @FXML private TableColumn<Payment, String> colStatus;

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
        // --- Table Column Setup ---

        colCardType.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMethod() != null ? data.getValue().getMethod().name() : ""
                )
        );

        colCardNumber.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMaskedCardNumber() != null ? data.getValue().getMaskedCardNumber() : ""
                )
        );

        colExpiryDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getExpirationDate() != null ? data.getValue().getExpirationDate() : ""
                )
        );

        colAmount.setCellValueFactory(data ->
                new SimpleStringProperty(String.format("₪ %.2f", data.getValue().getAmount()))
        );

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatus() != null ? data.getValue().getStatus().name() : ""
                )
        );

        paymentsTable.setItems(payments);

        // --- Button Setup ---
        btnRefreshPayments.setOnAction(e -> loadPayments());
        btnAddPayment.setOnAction(e -> addPayment());
        btnEmptyAddPayment.setOnAction(e -> addPayment());
        btnRemovePayment.setOnAction(e -> removeSelectedPayment());

        // --- Initial Load ---
        loadPayments();
    }

    // --- Load Mock Payments (replace later with server call) ---
    private void loadPayments() {
        payments.clear();

        Payment p1 = new Payment();
        p1.setId("PAY123");
        p1.setMethod(Payment.Method.CREDIT_CARD);
        p1.setMaskedCardNumber("**** **** **** 1234");
        p1.setCardHolderName("Maya Cohen");
        p1.setExpirationDate("05/27");
        p1.setStatus(Payment.Status.AUTHORIZED);
        p1.setAmount(49.99);

        Payment p2 = new Payment();
        p2.setId("PAY124");
        p2.setMethod(Payment.Method.CREDIT_CARD);
        p2.setMaskedCardNumber("**** **** **** 5678");
        p2.setCardHolderName("Noam Levi");
        p2.setExpirationDate("11/26");
        p2.setStatus(Payment.Status.CAPTURED);
        p2.setAmount(19.95);

        payments.addAll(p1, p2);

        updateUIState();
    }

    // --- Update Header + Visibility ---
    private void updateUIState() {
        paymentsCountLabel.setText("(" + payments.size() + ")");
        boolean empty = payments.isEmpty();
        paymentsEmptyBox.setVisible(empty);
        paymentsTable.setVisible(!empty);
        paymentsActions.setDisable(empty);
    }

    // --- Add New Payment ---
    private void addPayment() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Add Payment Method");
        alert.setContentText("This would open the Add Payment dialog or payment gateway integration.");
        alert.showAndWait();
    }

    // --- Remove Selected Payment ---
    private void removeSelectedPayment() {
        Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a payment method to remove.").showAndWait();
            return;
        }

        // TODO: Replace with server delete request
        payments.remove(selected);
        updateUIState();
    }
}