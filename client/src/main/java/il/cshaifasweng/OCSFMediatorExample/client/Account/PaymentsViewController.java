package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.common.RequiresSession;
import il.cshaifasweng.OCSFMediatorExample.client.ui.ViewTracker;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AddPaymentRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AddPaymentResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetPaymentsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetPaymentsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.PaymentDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.RemovePaymentRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.RemovePaymentResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import javafx.scene.layout.GridPane;

import java.util.List;

public class PaymentsViewController implements RequiresSession {

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

    private long customerId = -1;

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

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
        paymentsTable.setPlaceholder(new Label("No payment methods yet."));

        // Enable/disable Remove by selection
        if (btnRemovePayment != null) {
            btnRemovePayment.setDisable(true);
            paymentsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) ->
                    btnRemovePayment.setDisable(n == null));
        }

        // --- Button Setup ---
        btnRefreshPayments.setOnAction(e -> requestPayments());
        btnAddPayment.setOnAction(e -> addPayment());
        btnEmptyAddPayment.setOnAction(e -> addPayment());
        btnRemovePayment.setOnAction(e -> removeSelectedPayment());

        // Boot if already logged in; otherwise lock down actions
        long id = ClientSession.getCustomerId();
        if (id > 0) {
            setCustomerId(id);
        } else {
            lockWhenLoggedOut(true);
            updateUIState();
        }
    }

    @Override
    public void setCustomerId(long customerId) {
        this.customerId = customerId;
        lockWhenLoggedOut(customerId <= 0);
        requestPayments();
    }

    // --- Request payments from server ---
    private void requestPayments() {
        try {
            payments.clear();
            updateUIState();
            if (customerId <= 0) return; // don't spam the server without a user
            SimpleClient.getClient().sendToServer(new GetPaymentsRequest(String.valueOf(customerId)));
        } catch (Exception ex) {
            showError("Failed to request payments: " + ex.getMessage());
        }
    }

    @Subscribe
    public void onGetPayments(GetPaymentsResponse resp) {
        Platform.runLater(() -> {
            List<Payment> list = resp != null ? resp.getPayments() : List.of();
            payments.setAll(list);
            updateUIState();
        });
    }

    @Subscribe
    public void onAddPayment(AddPaymentResponse resp) {
        Platform.runLater(this::requestPayments);
    }

    @Subscribe
    public void onRemovePayment(RemovePaymentResponse resp) {
        Platform.runLater(this::requestPayments);
    }

    // React when login completes so this tab can load itself
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        long id = ClientSession.getCustomerId();
        if (id > 0) setCustomerId(id);
    }

    // React when overview hydrates the customer
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;
        long id = ClientSession.getCustomerId();
        if (id > 0) setCustomerId(id);
    }

    // Optional: when this view becomes active and we have nothing yet, fetch
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActive(ViewTracker.ActiveControllerChanged e) {
        if (e == null) return;
        String id = e.controllerId != null ? e.controllerId : e.getControllerId();
        if ("PaymentsView".equals(id) && payments.isEmpty() && ClientSession.getCustomerId() > 0) {
            requestPayments();
        }
    }

    // --- Update Header + Visibility ---
    private void updateUIState() {
        paymentsCountLabel.setText("(" + payments.size() + ")");
        boolean empty = payments.isEmpty();
        paymentsEmptyBox.setVisible(empty);
        paymentsTable.setVisible(!empty);
        paymentsActions.setDisable(empty || ClientSession.getCustomerId() <= 0);
        btnAddPayment.setDisable(ClientSession.getCustomerId() <= 0);
        btnEmptyAddPayment.setDisable(ClientSession.getCustomerId() <= 0);
    }

    private void lockWhenLoggedOut(boolean loggedOut) {
        if (loggedOut) {
            if (paymentsTable != null) paymentsTable.setPlaceholder(new Label("Log in to manage payment methods."));
            if (btnAddPayment != null) btnAddPayment.setDisable(true);
            if (btnEmptyAddPayment != null) btnEmptyAddPayment.setDisable(true);
            if (btnRemovePayment != null) btnRemovePayment.setDisable(true);
        }
    }

    // --- Add New Payment (simple inline dialog) ---
    private void addPayment() {
        Dialog<PaymentDTO> dialog = new Dialog<>();
        dialog.setTitle("Add Payment Method");
        dialog.setHeaderText("Enter card details");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        // Form controls
        TextField holder = new TextField();
        holder.setPromptText("Card holder (e.g., John Doe)");

        TextField number = new TextField();
        number.setPromptText("Card number (digits only)");

        TextField expiry = new TextField();
        expiry.setPromptText("MM/YY");

        ComboBox<Payment.Method> method = new ComboBox<>();
        method.getItems().addAll(Payment.Method.CREDIT_CARD);
        method.getSelectionModel().select(Payment.Method.CREDIT_CARD);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.addRow(0, new Label("Method:"), method);
        grid.addRow(1, new Label("Holder:"), holder);
        grid.addRow(2, new Label("Number:"), number);
        grid.addRow(3, new Label("Expiry:"), expiry);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String digits = number.getText() == null ? "" : number.getText().replaceAll("\\D", "");
                if (digits.length() < 12) {
                    showWarn("Invalid card number.");
                    return null;
                }
                String last4 = digits.substring(digits.length() - 4);
                String masked = "**** **** **** " + last4;

                String exp = expiry.getText() == null ? "" : expiry.getText().trim();
                if (!exp.matches("^(0[1-9]|1[0-2])\\/\\d{2}$")) {
                    showWarn("Expiry must be MM/YY.");
                    return null;
                }

                PaymentDTO dto = new PaymentDTO();
                dto.setMethod(method.getSelectionModel().getSelectedItem());
                dto.setMaskedCardNumber(masked);
                dto.setCardHolderName(holder.getText());
                dto.setExpirationDate(exp);
                dto.setAmount(0.0); // storing method; not a charge
                return dto;
            }
            return null;
        });

        PaymentDTO dto = dialog.showAndWait().orElse(null);
        if (dto == null) return;

        try {
            SimpleClient.getClient().sendToServer(new AddPaymentRequest(String.valueOf(customerId), dto));
        } catch (Exception ex) {
            showError("Failed to add payment: " + ex.getMessage());
        }
    }

    // --- Remove Selected Payment ---
    private void removeSelectedPayment() {
        Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Please select a payment method to remove.");
            return;
        }
        try {
            SimpleClient.getClient().sendToServer(
                    new RemovePaymentRequest(String.valueOf(customerId), selected.getId())
            );
        } catch (Exception ex) {
            showError("Failed to remove payment: " + ex.getMessage());
        }
    }

    private static void showWarn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }
    private static void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
