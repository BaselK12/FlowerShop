package il.cshaifasweng.OCSFMediatorExample.client.Customer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;

import java.net.URL;
import java.time.Year;
import java.util.ResourceBundle;

public class CustomerRegisterPaymentController implements Initializable {

    // ====== Containers ======
    @FXML private VBox paymentMainContainer;
    @FXML private GridPane paymentGrid;
    @FXML private HBox summaryRowTotal;

    // ====== Labels ======
    @FXML private Label paymentTitleLabel;
    @FXML private Label paymentErrorLabel;
    @FXML private Label totalTextLabel;
    @FXML private Label totalValueLabel;
    @FXML private Label cardHolderLabel;
    @FXML private Label cardNumberLabel;
    @FXML private Label ExpiryDateLabel;
    @FXML private Label cvvLabel;

    // ====== Input fields ======
    @FXML private TextField CardHolderText;
    @FXML private TextField CardNumberText;
    @FXML private TextField cvvText;
    @FXML private ComboBox<String> monthExpiryCombo;
    @FXML private ComboBox<String> yearExpiryCombo;

    // ====== Buttons ======
    @FXML private Button backBtn;
    @FXML private Button confirmBtn;

    // ====== Internal state ======
    private double totalToPay = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        paymentErrorLabel.setText("");
        initMonthYearCombos();

        // Button actions
        backBtn.setOnAction(e -> handleBack());
        confirmBtn.setOnAction(e -> handleConfirm());
    }

    private void initMonthYearCombos() {
        for (int m = 1; m <= 12; m++) {
            monthExpiryCombo.getItems().add(String.format("%02d", m));
        }

        int currentYear = Year.now().getValue();
        for (int y = 0; y < 10; y++) {
            yearExpiryCombo.getItems().add(String.valueOf(currentYear + y).substring(2)); // YY format
        }
    }

    public void setTotalToPay(double total) {
        this.totalToPay = total;
        totalValueLabel.setText(String.format("%.2f", total) + "₪");
    }

    private void handleBack() {
        // Example: post navigation event or callback
        System.out.println("[Payment] Back button pressed");
        EventBus.getDefault().post("BACK_TO_PREVIOUS_SCREEN");
    }

    private void handleConfirm() {
        if (!validateInputs()) {
            return;
        }

        String holder = CardHolderText.getText().trim();
        String number = CardNumberText.getText().trim();
        String month = monthExpiryCombo.getValue();
        String year = yearExpiryCombo.getValue();
        String cvv = cvvText.getText().trim();

        System.out.println("[Payment] Confirm pressed:");
        System.out.printf("Holder: %s | Number: %s | Expiry: %s/%s | CVV: %s%n",
                holder, number, month, year, cvv);

        // Example: build payment request
        // EventBus.getDefault().post(new SavePaymentRequest(holder, number, month, year, cvv, totalToPay));

        paymentErrorLabel.setText("Payment confirmed successfully!");
        paymentErrorLabel.setStyle("-fx-text-fill: green;");
    }

    private boolean validateInputs() {
        String holder = CardHolderText.getText().trim();
        String number = CardNumberText.getText().trim();
        String month = monthExpiryCombo.getValue();
        String year = yearExpiryCombo.getValue();
        String cvv = cvvText.getText().trim();

        if (holder.isEmpty() || number.isEmpty() || month == null || year == null || cvv.isEmpty()) {
            showError("Please fill in all fields.");
            return false;
        }

        if (!number.matches("\\d{16}")) {
            showError("Card number must contain 16 digits.");
            return false;
        }

        if (!cvv.matches("\\d{3}")) {
            showError("CVV must contain 3 digits.");
            return false;
        }

        hideError();
        return true;
    }

    private void showError(String message) {
        paymentErrorLabel.setText(message);
        paymentErrorLabel.setStyle("-fx-text-fill: red;");
    }

    private void hideError() {
        paymentErrorLabel.setText("");
    }
}
