package il.cshaifasweng.OCSFMediatorExample.client.Complaint;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.session.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintResponse;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class FilingComplaintController {

    @FXML private ComboBox<String> CategoryBox;
    @FXML private TextField SubjectTxt, EmailTxt, PhoneTxt, OrderTxt;
    @FXML private TextArea  MsgTxt;
    @FXML private CheckBox  AnonymousCheck, ConfirmCheck;
    @FXML private Label     ErrorLabel;
    @FXML private Button    SubmitBtn, CancelBtn;

    @FXML
    private void initialize() {
        ClientSession.install();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (ErrorLabel != null) ErrorLabel.setText("");

        // Categories (static until you fetch from server)
        if (CategoryBox != null) {
            CategoryBox.setItems(FXCollections.observableArrayList(
                    "GENERAL", "ORDER", "DELIVERY", "PRODUCT", "BILLING", "OTHER"
            ));
            if (!CategoryBox.getItems().isEmpty()) CategoryBox.getSelectionModel().selectFirst();
        }

        // Anonymous disables and clears contact info
        if (AnonymousCheck != null) {
            if (EmailTxt != null)  EmailTxt.disableProperty().bind(AnonymousCheck.selectedProperty());
            if (PhoneTxt != null)  PhoneTxt.disableProperty().bind(AnonymousCheck.selectedProperty());
            AnonymousCheck.selectedProperty().addListener((obs, was, isNow) -> {
                if (isNow) {
                    if (EmailTxt != null) EmailTxt.clear();
                    if (PhoneTxt != null) PhoneTxt.clear();
                }
            });
        }

        // Order ID: digits only (up to 18)
        if (OrderTxt != null) {
            OrderTxt.setPromptText("e.g. 12345");
            OrderTxt.setTextFormatter(new TextFormatter<>(change ->
                    change.getControlNewText().matches("\\d{0,18}") ? change : null));
        }

        if (ClientSession.getCustomerId() <= 0) {
            try { SimpleClient.getClient().sendSafely(new AccountOverviewRequest(0)); }
            catch (Exception ignored) {}
        }
    }

    @FXML
    private void onCancel() {
        if (CancelBtn != null && CancelBtn.getScene() != null) {
            CancelBtn.getScene().getWindow().hide();
        }
    }

    @FXML
    private void onSubmit() {
        if (ErrorLabel != null) ErrorLabel.setText("");

        long cid = ClientSession.getCustomerId();
        if (cid <= 0) { setError("Please log in again."); return; }

        String subject = text(SubjectTxt);
        String desc    = text(MsgTxt);
        if (subject.isBlank() || desc.isBlank()) {
            setError("Subject and description are required.");
            return;
        }

        if (ConfirmCheck != null && !ConfirmCheck.isSelected()) {
            setError("Please confirm the information is accurate.");
            return;
        }

        Long   orderId  = parseLongOrNull(text(OrderTxt));   // null if blank
        String category = CategoryBox != null ? CategoryBox.getValue() : null;

        boolean anon    = AnonymousCheck != null && AnonymousCheck.isSelected();
        String email    = anon ? null : blankToNull(text(EmailTxt));
        String phone    = anon ? null : blankToNull(text(PhoneTxt));

        SubmitBtn.setDisable(true);
        try {
            // (customerId, orderId, category, subject, message, anonymous, email, phone)
            SubmitComplaintRequest req = new SubmitComplaintRequest(
                    cid, orderId, category, subject, desc, anon, email, phone
            );
            SimpleClient.getClient().sendSafely(req);
        } catch (Exception ex) {
            SubmitBtn.setDisable(false);
            setError("Connection error.");
            ex.printStackTrace();
        }
    }

    @Subscribe
    public void onSubmitComplaintResponse(SubmitComplaintResponse resp) {
        Platform.runLater(() -> {
            SubmitBtn.setDisable(false);
            if (resp != null && resp.isOk()) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Complaint submitted. ID: " + resp.getComplaintId()).showAndWait();
                if (SubmitBtn.getScene() != null) {
                    SubmitBtn.getScene().getWindow().hide();
                }
            } else {
                setError(resp != null && resp.getReason() != null ? resp.getReason() : "Submit failed.");
            }
        });
    }

    // helpers
    private static String text(TextInputControl c) { return c == null || c.getText() == null ? "" : c.getText().trim(); }
    private static Long parseLongOrNull(String s) { try { return (s == null || s.isBlank()) ? null : Long.parseLong(s); } catch (NumberFormatException e) { return null; } }
    private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
    private void setError(String msg) { if (ErrorLabel != null) ErrorLabel.setText(msg); }
}
