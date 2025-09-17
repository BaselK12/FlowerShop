package il.cshaifasweng.OCSFMediatorExample.client.CheckoutStage;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.DeliveryInfo;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.PickupInfo;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.DeliveryPickup.DeliveryInfoRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.DeliveryPickup.PickupInfoRequest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PickupDeliveryController {

    // === Step badges ===
    @FXML private Label badgeStep1;
    @FXML private Label badgeStep2Pickup;
    @FXML private Label badgeStep2Delivery;
    @FXML private Label badgeStep3Delivery;
    @FXML private Label badgeStep4;

    // === Step panes ===
    @FXML private VBox step2PickupPane;
    @FXML private VBox step2DeliveryPane;
    @FXML private VBox step3DeliveryPane;
    @FXML private VBox reviewPane;

    // === Step 1 ===
    @FXML private RadioButton pickupRadio;
    @FXML private RadioButton deliveryRadio;
    @FXML private ToggleGroup methodGroup;
    @FXML private Button step1NextBtn;
    @FXML private Button step1BackBtn;


    // === Step 2a (Pickup) ===
    @FXML private ComboBox<String> pickupBranchBox;
    @FXML private DatePicker pickupDatePicker;
    @FXML private TextField pickupTimeField;
    @FXML private TextField pickupPhoneField;
    @FXML private Button pickupBackBtn;
    @FXML private Button pickupNextBtn;

    // === Step 2b (Delivery) ===
    @FXML private ComboBox<String> cityBox;
    @FXML private TextField streetField;
    @FXML private TextField houseField;
    @FXML private TextField zipField;
    @FXML private TextField deliveryPhoneField;
    @FXML private Button deliveryBackBtn;
    @FXML private Button deliveryNextBtn;

    // === Step 3 (Delivery Options) ===
    @FXML private CheckBox giftCheck;
    @FXML private VBox giftFieldsBox;
    @FXML private TextField recipientNameField;
    @FXML private TextField recipientPhoneField;
    @FXML private TextArea giftNoteArea;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField deliveryTimeField;
    @FXML private Button optionsBackBtn;
    @FXML private Button optionsNextBtn;

    // === Step 4 (Review) ===
    @FXML private TextArea reviewSummaryArea;
    @FXML private Button reviewBackBtn;
    @FXML private Button confirmBtn;

    // === Internal state ===
    private enum Method { PICKUP, DELIVERY }
    private Method chosenMethod = null;

    @FXML
    public void initialize() {
        // Toggle gift box visibility
        giftCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            giftFieldsBox.setVisible(newVal);
        });

        // Populate dropdowns
        pickupBranchBox.getItems().addAll("Haifa – Main", "Tel Aviv – Center", "Jerusalem – City");
        cityBox.getItems().addAll("Haifa", "Tel Aviv", "Jerusalem");

        highlightStep(1);
    }

    // === Highlight current step badge ===
    private void highlightStep(int step) {
        badgeStep1.setStyle("-fx-background-color: #eaeaea;");
        badgeStep2Pickup.setStyle("-fx-background-color: #eaeaea;");
        badgeStep2Delivery.setStyle("-fx-background-color: #eaeaea;");
        badgeStep3Delivery.setStyle("-fx-background-color: #eaeaea;");
        badgeStep4.setStyle("-fx-background-color: #eaeaea;");

        switch (step) {
            case 1 -> badgeStep1.setStyle("-fx-background-color: lightgreen;");
            case 2 -> {
                if (chosenMethod == Method.PICKUP) badgeStep2Pickup.setStyle("-fx-background-color: lightgreen;");
                else badgeStep2Delivery.setStyle("-fx-background-color: lightgreen;");
            }
            case 3 -> badgeStep3Delivery.setStyle("-fx-background-color: lightgreen;");
            case 4 -> badgeStep4.setStyle("-fx-background-color: lightgreen;");
        }
    }

    // === Step 1 → Next ===
    @FXML
    private void handleStep1Next() {
        if (pickupRadio.isSelected()) {
            chosenMethod = Method.PICKUP;
            step2PickupPane.setVisible(true);
        } else if (deliveryRadio.isSelected()) {
            chosenMethod = Method.DELIVERY;
            step2DeliveryPane.setVisible(true);
        } else {
            showAlert("Please choose Pickup or Delivery.");
            return;
        }
        step1NextBtn.getParent().getParent().setVisible(false);
        highlightStep(2);
    }

    // === Step 2a (Pickup) → Next ===
    @FXML
    private void handlePickupNext() {
        if (pickupBranchBox.getValue() == null ||
                pickupDatePicker.getValue() == null ||
                pickupTimeField.getText().isEmpty()) {
            showAlert("Please fill branch, date, and time.");
            return;
        }
        step2PickupPane.setVisible(false);
        reviewPane.setVisible(true);
        reviewSummaryArea.setText(buildReviewSummary());
        highlightStep(4);
    }

    // === Step 2a → Back ===
    @FXML
    private void handlePickupBack() {
        step2PickupPane.setVisible(false);
        step1NextBtn.getParent().getParent().setVisible(true);
        highlightStep(1);
    }

    // === Step 2b (Delivery) → Next ===
    @FXML
    private void handleDeliveryNext() {
        if (cityBox.getValue() == null || streetField.getText().isEmpty() ||
                houseField.getText().isEmpty() || deliveryPhoneField.getText().isEmpty()) {
            showAlert("Please fill city, street, house number, and phone.");
            return;
        }
        step2DeliveryPane.setVisible(false);
        step3DeliveryPane.setVisible(true);
        highlightStep(3);
    }

    // === Step 2b → Back ===
    @FXML
    private void handleDeliveryBack() {
        step2DeliveryPane.setVisible(false);
        step1NextBtn.getParent().getParent().setVisible(true);
        highlightStep(1);
    }

    // === Step 3 (Delivery Options) → Next ===
    @FXML
    private void handleOptionsNext() {
        step3DeliveryPane.setVisible(false);
        reviewPane.setVisible(true);
        reviewSummaryArea.setText(buildReviewSummary());
        highlightStep(4);
    }

    // === Step 3 → Back ===
    @FXML
    private void handleOptionsBack() {
        step3DeliveryPane.setVisible(false);
        step2DeliveryPane.setVisible(true);
        highlightStep(2);
    }

    // === Step 4 → Back ===
    @FXML
    private void handleReviewBack() {
        reviewPane.setVisible(false);
        if (chosenMethod == Method.PICKUP) {
            step2PickupPane.setVisible(true);
            highlightStep(2);
        } else {
            step3DeliveryPane.setVisible(true);
            highlightStep(3);
        }
    }

    // === Step 4 → Confirm (send to server) ===
    @FXML
    private void handleConfirm() {
        try {
            if (chosenMethod == Method.PICKUP) {
                PickupInfo pickup = new PickupInfo();
                pickup.setShopId(pickupBranchBox.getValue());
                pickup.setContactName("Customer"); // you can connect this to logged-in user
                pickup.setPhone(pickupPhoneField.getText());
                LocalDate d = pickupDatePicker.getValue();
                LocalTime t = LocalTime.parse(pickupTimeField.getText());
                pickup.setScheduledAt(LocalDateTime.of(d, t));

                SimpleClient.getClient().sendToServer(new PickupInfoRequest(pickup));

            } else {
                DeliveryInfo delivery = new DeliveryInfo();
                delivery.setReceiverName(giftCheck.isSelected()
                        ? recipientNameField.getText()
                        : "Customer");
                delivery.setPhone(deliveryPhoneField.getText());
                delivery.setAddressLine(streetField.getText() + " " + houseField.getText());
                delivery.setCity(cityBox.getValue());
                delivery.setZip(zipField.getText());

                if (deliveryDatePicker.getValue() != null && !deliveryTimeField.getText().isEmpty()) {
                    LocalDate d = deliveryDatePicker.getValue();
                    LocalTime t = LocalTime.parse(deliveryTimeField.getText());
                    delivery.setScheduledAt(LocalDateTime.of(d, t));
                }

                SimpleClient.getClient().sendToServer(new DeliveryInfoRequest(delivery));
            }

            showAlert("Order details sent to server. Proceed to payment.");
            // TODO: switch to Payment.fxml here

        } catch (IOException e) {
            showAlert("Failed to send order: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage());
        }
    }

    // === Build customer summary text ===
    private String buildReviewSummary() {
        StringBuilder sb = new StringBuilder();
        if (chosenMethod == Method.PICKUP) {
            sb.append("Pickup at ").append(pickupBranchBox.getValue()).append("\n");
            sb.append("Date: ").append(pickupDatePicker.getValue()).append("\n");
            sb.append("Time: ").append(pickupTimeField.getText()).append("\n");
            sb.append("Phone: ").append(pickupPhoneField.getText()).append("\n");
        } else {
            sb.append("Delivery to ").append(streetField.getText())
                    .append(" ").append(houseField.getText())
                    .append(", ").append(cityBox.getValue()).append("\n");
            sb.append("ZIP: ").append(zipField.getText()).append("\n");
            sb.append("Phone: ").append(deliveryPhoneField.getText()).append("\n");
            sb.append("Date: ").append(deliveryDatePicker.getValue()).append("\n");
            sb.append("Time: ").append(deliveryTimeField.getText()).append("\n");
            if (giftCheck.isSelected()) {
                sb.append("Gift: Yes\n");
                sb.append("Recipient: ").append(recipientNameField.getText())
                        .append(" (").append(recipientPhoneField.getText()).append(")\n");
                sb.append("Note: ").append(giftNoteArea.getText()).append("\n");
            } else {
                sb.append("Gift: No\n");
            }
        }
        return sb.toString();
    }

    // === Utility: Show alert popup ===
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
