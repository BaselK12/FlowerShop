package il.cshaifasweng.OCSFMediatorExample.client.CheckoutStage;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;

public class CheckoutController {

    /* ===========================
       Step Navigation
       =========================== */
    @FXML private StackPane contentPane;
    @FXML private VBox step1;
    @FXML private VBox step2;
    @FXML private VBox step3;

    @FXML private Label badge1;
    @FXML private Label badge2;
    @FXML private Label badge3;

    /* ===========================
       Step 1 – Pickup / Delivery
       =========================== */
    @FXML private ToggleButton btnPickup;
    @FXML private ToggleButton btnDelivery;

    @FXML private GridPane pickupFields;
    @FXML private GridPane deliveryFields;

    @FXML private ComboBox<String> pickupBranch;
    @FXML private DatePicker pickupDate;
    @FXML private TextField pickupTime;
    @FXML private TextField pickupPhone;

    @FXML private ComboBox<String> CityBox;
    @FXML private TextField StreetText;
    @FXML private TextField HouseText;
    @FXML private TextField ZipText;
    @FXML private TextField PhoneText;

    @FXML private CheckBox giftCheck;
    @FXML private VBox gifVBox;
    @FXML private TextField RecipientPhoneText;
    @FXML private TextField RecepientNameText;
    @FXML private TextArea GiftNoteText;
    @FXML private DatePicker DeliveryDatePicker;
    @FXML private TextField DeliveryTimeText;

    @FXML private Button Back1;
    @FXML private Button next1;

    /* ===========================
       Step 2 – Payment
       =========================== */
    @FXML private TextField CardNumberText;
    @FXML private ComboBox<String> MMBOX;
    @FXML private ComboBox<String> YYBOX;
    @FXML private TextField cvvText;
    @FXML private TextField fullNameText;
    @FXML private TextField IdNumberText;

    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label shippingLabel;
    @FXML private Label grandTotalLabel;

    @FXML private Button back2;
    @FXML private Button payBtn;

    /* ===========================
       Step 3 – Review / Confirm
       =========================== */
    @FXML private TextArea reviewBox;
    @FXML private Button back3;
    @FXML private Button confirmBtn;

    /* ===========================
       Initialization
       =========================== */
    @FXML
    private void initialize() {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Create toggle group for Pickup / Delivery buttons
        var methodGroup = new ToggleGroup();
        btnPickup.setToggleGroup(methodGroup);
        btnDelivery.setToggleGroup(methodGroup);
        btnPickup.setSelected(true);

        // Hide delivery fields and gift VBox initially
        deliveryFields.setVisible(false);
        deliveryFields.setManaged(false);
        gifVBox.setVisible(false);
        gifVBox.setManaged(false);

        // Setup gift checkbox listener
        giftCheck.selectedProperty().addListener((obs, oldVal, isChecked) -> {
            gifVBox.setVisible(isChecked);
            gifVBox.setManaged(isChecked);
        });

        MMBOX.getItems().addAll("01","02","03","04","05","06","07","08","09","10","11","12");
        for (int y = 25; y <= 35; y++) {
            YYBOX.getItems().add(String.valueOf(y));  // e.g. 25 = 2025
        }

        // Setup pickup/delivery toggle listener
        methodGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            boolean pickup = selected == btnPickup;
            pickupFields.setVisible(pickup);
            pickupFields.setManaged(pickup);
            deliveryFields.setVisible(!pickup);
            deliveryFields.setManaged(!pickup);
        });
    }

    /* ===========================
       Navigation Logic
       =========================== */

    @FXML
    private void handleNext1() {
        showStep(2);
    }

    @FXML
    private void handleBack1() {
        // Optionally go back to previous scene or close checkout
    }

    @FXML
    private void handleBack2() {
        showStep(1);
    }

    @FXML
    private void handlePay() {
        // 1) Clear previous error styles
        CardNumberText.getStyleClass().remove("error-field");
        MMBOX.getStyleClass().remove("error-field");
        YYBOX.getStyleClass().remove("error-field");
        cvvText.getStyleClass().remove("error-field");
        fullNameText.getStyleClass().remove("error-field");
        IdNumberText.getStyleClass().remove("error-field");

        // 2) Read values from fields
        String cardNum = CardNumberText.getText();
        String month = MMBOX.getValue();
        String year = YYBOX.getValue();
        String cvv = cvvText.getText();
        String fullName = fullNameText.getText();
        String id = IdNumberText.getText();

        boolean valid = true;
        StringBuilder errorMsg = new StringBuilder();

        // 3) Validation checks
        if (cardNum == null || !cardNum.matches("\\d{16}")) {
            CardNumberText.getStyleClass().add("error-field");
            errorMsg.append("• Card number must contain 16 digits.\n");
            valid = false;
        }

        if (month == null || month.isBlank()) {
            MMBOX.getStyleClass().add("error-field");
            errorMsg.append("• Please select expiration month.\n");
            valid = false;
        }

        if (year == null || year.isBlank()) {
            YYBOX.getStyleClass().add("error-field");
            errorMsg.append("• Please select expiration year.\n");
            valid = false;
        }

        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            cvvText.getStyleClass().add("error-field");
            errorMsg.append("• CVV must be 3–4 digits.\n");
            valid = false;
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            fullNameText.getStyleClass().add("error-field");
            errorMsg.append("• Full name cannot be empty.\n");
            valid = false;
        }

        if (id == null || !id.matches("\\d{8,9}")) {
            IdNumberText.getStyleClass().add("error-field");
            errorMsg.append("• ID number must be 8–9 digits.\n");
            valid = false;
        }

        // 4) If any invalid fields — show alert and stop
        if (!valid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Payment Error");
            alert.setHeaderText("Please correct the highlighted fields:");
            alert.setContentText(errorMsg.toString());

            // Apply your dark-themed dialog CSS
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/Styles/checkout.css").toExternalForm()
            );
            dialogPane.getStyleClass().add("dialog-pane");

            alert.showAndWait();
            return;
        }

        // 5) If valid → continue
        showStep(3);
        fillReview();
    }


    @FXML
    private void handleBack3() {
        showStep(2);
    }

    @FXML
    private void handleConfirm() {
        // TODO: send order to server or show confirmation dialog
        System.out.println("Order confirmed!");
    }

    /* ===========================
       Helper Methods
       =========================== */

    private void showStep(int step) {
        step1.setVisible(false); step1.setManaged(false);
        step2.setVisible(false); step2.setManaged(false);
        step3.setVisible(false); step3.setManaged(false);

        badge1.getStyleClass().remove("active");
        badge2.getStyleClass().remove("active");
        badge3.getStyleClass().remove("active");

        switch (step) {
            case 1 -> {
                step1.setVisible(true);
                step1.setManaged(true);
                badge1.getStyleClass().add("active");
            }
            case 2 -> {
                step2.setVisible(true);
                step2.setManaged(true);
                badge2.getStyleClass().add("active");
            }
            case 3 -> {
                step3.setVisible(true);
                step3.setManaged(true);
                badge3.getStyleClass().add("active");
            }
        }
    }

    private void fillReview() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pickup/Delivery: ")
                .append(btnPickup.isSelected() ? "Pickup" : "Delivery").append("\n\n");

        if (btnPickup.isSelected()) {
            sb.append("Branch: ").append(pickupBranch.getValue()).append("\n");
            sb.append("Date: ").append(pickupDate.getValue()).append("\n");
            sb.append("Time: ").append(pickupTime.getText()).append("\n");
            sb.append("Phone: ").append(pickupPhone.getText()).append("\n");
        } else {
            sb.append("City: ").append(CityBox.getValue()).append("\n");
            sb.append("Street: ").append(StreetText.getText()).append(" ").append(HouseText.getText()).append("\n");
            sb.append("Phone: ").append(PhoneText.getText()).append("\n");
            if (giftCheck.isSelected()) {
                sb.append("Gift for ").append(RecepientNameText.getText())
                        .append(" (").append(RecipientPhoneText.getText()).append(")\n");
                sb.append("Note: ").append(GiftNoteText.getText()).append("\n");
            }
        }

        sb.append("\nTotal: ₪ ").append(grandTotalLabel.getText());
        reviewBox.setText(sb.toString());
    }
}