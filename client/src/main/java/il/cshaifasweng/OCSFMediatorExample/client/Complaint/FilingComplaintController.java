package il.cshaifasweng.OCSFMediatorExample.client.Complaint;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.session.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FilingComplaintController {

    @FXML private ComboBox<String> CategoryBox;
    @FXML private TextField SubjectTxt, EmailTxt, PhoneTxt, OrderTxt;
    @FXML private TextArea  MsgTxt;
    @FXML private CheckBox  AnonymousCheck, ConfirmCheck;
    @FXML private Label     ErrorLabel;
    @FXML private Button    SubmitBtn, CancelBtn;

    private volatile boolean sending = false;

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

        // Try to hydrate contact fields (even if already logged in, to prefill)
        try { SimpleClient.getClient().sendSafely(new AccountOverviewRequest(0)); }
        catch (Exception ignored) {}
    }

    @FXML
    private void onCancel() {
        if (CancelBtn != null && CancelBtn.getScene() != null) {
            CancelBtn.getScene().getWindow().hide();
        }
    }

    @FXML
    private void onSubmit() {
        if (sending) return;
        setError("");

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

        boolean anon = AnonymousCheck != null && AnonymousCheck.isSelected();
        String email = anon ? null : blankToNull(text(EmailTxt));
        String phone = anon ? null : blankToNull(text(PhoneTxt));

        // Basic validation when not anonymous
        if (!anon) {
            if (email == null || !isValidEmail(email)) {
                setError("Please provide a valid email or select Anonymous.");
                return;
            }
            if (phone != null && !isValidPhone(phone)) {
                setError("Phone number can contain digits, +, -, spaces, (), and must be ≤ 32 chars.");
                return;
            }
        }

        Long   orderId  = parseLongOrNull(text(OrderTxt));   // null if blank
        String category = CategoryBox != null ? CategoryBox.getValue() : null;

        lockUI(true);
        sending = true;
        try {
            // (customerId, orderId, category, subject, message, anonymous, email, phone)
            SubmitComplaintRequest req = new SubmitComplaintRequest(
                    cid, orderId, category, subject, desc, anon, email, phone
            );
            SimpleClient.getClient().sendSafely(req);
        } catch (Exception ex) {
            sending = false;
            lockUI(false);
            setError("Connection error.");
            ex.printStackTrace();
        }
    }

    /* ==== EventBus: direct & wrapped ==== */

    // Direct post
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubmitComplaintResponse(SubmitComplaintResponse resp) {
        handleSubmitResponse(resp);
    }

    // Wrapped in ServerMessageEvent
    @Subscribe
    public void onServerMessage(ServerMessageEvent ev) {
        Object p = ev.getPayload();
        if (p instanceof SubmitComplaintResponse resp) {
            Platform.runLater(() -> handleSubmitResponse(resp));
        } else if (p instanceof AccountOverviewResponse ov) {
            Platform.runLater(() -> handleOverview(ov));
        }
    }

    // If login completes while dialog is open, refresh overview to prefill contacts
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        try { SimpleClient.getClient().sendSafely(new AccountOverviewRequest(0)); }
        catch (Exception ignored) {}
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse ov) {
        handleOverview(ov);
    }

    /* ==== Handlers ==== */

    private void handleSubmitResponse(SubmitComplaintResponse resp) {
        sending = false;
        lockUI(false);

        if (resp != null && resp.isOk()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Complaint submitted. ID: " + resp.getComplaintId()).showAndWait();
            if (SubmitBtn != null && SubmitBtn.getScene() != null) {
                SubmitBtn.getScene().getWindow().hide();
            }
        } else {
            setError(resp != null && resp.getReason() != null ? resp.getReason() : "Submit failed.");
        }
    }

    private void handleOverview(AccountOverviewResponse resp) {
        if (resp == null || !resp.ok() || resp.customer() == null) return;
        // Pre-fill email/phone only if not anonymous and fields are still blank (don’t clobber user edits)
        boolean anon = AnonymousCheck != null && AnonymousCheck.isSelected();
        if (anon) return;

        var c = resp.customer();
        if (EmailTxt != null && (EmailTxt.getText() == null || EmailTxt.getText().isBlank())) {
            try { if (c.getEmail() != null) EmailTxt.setText(c.getEmail()); } catch (Exception ignored) {}
        }
        if (PhoneTxt != null && (PhoneTxt.getText() == null || PhoneTxt.getText().isBlank())) {
            try { if (c.getPhone() != null) PhoneTxt.setText(c.getPhone()); } catch (Exception ignored) {}
        }
    }

    /* ==== helpers ==== */

    private void lockUI(boolean on) {
        if (SubmitBtn != null) SubmitBtn.setDisable(on);
        if (CancelBtn != null) CancelBtn.setDisable(on);
    }

    private static String text(TextInputControl c) { return c == null || c.getText() == null ? "" : c.getText().trim(); }
    private static Long parseLongOrNull(String s) { try { return (s == null || s.isBlank()) ? null : Long.parseLong(s); } catch (NumberFormatException e) { return null; } }
    private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }

    private static boolean isValidEmail(String s) {
        return s != null && s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    private static boolean isValidPhone(String s) {
        return s != null && s.length() <= 32 && s.matches("^[0-9+()\\-\\s]*$");
    }

    private void setError(String msg) { if (ErrorLabel != null) ErrorLabel.setText(msg); }
}
