package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.RequiresSession;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileResponse;



import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public class ProfileViewController implements RequiresSession {

    // Main Containers
    @FXML private VBox ProfileBox;
    @FXML private AnchorPane cardProfileView;
    @FXML private AnchorPane CardProfileEditPane;

    // View Mode Elements
    @FXML private Label NameLabel;
    @FXML private Label EmailLabel;
    @FXML private Label PhoneLabel;
    @FXML private Label accountTypeLabel;
    @FXML private Button EditBtn;

    // Edit Mode Elements
    @FXML private TextField NameField;
    @FXML private TextField EmailField;
    @FXML private TextField PhoneEmail; // misnamed in FXML; it's the phone field
    @FXML private Button CancelBtn;
    @FXML private Button SaveBtn;


    // State
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty saving  = new SimpleBooleanProperty(false);

    // Current profile snapshot
    private long   currentId    = 0L;
    private String currentName  = "";
    private String currentEmail = "";
    private String currentPhone = "";

    // Session-provided id (from parent controller)
    private long injectedCustomerId = 0L;

    @Override
    public void setCustomerId(long customerId) {
        this.injectedCustomerId = customerId;
        this.currentId = customerId;
        requestOverview(); // always request; server will infer if id==0
    }

    @FXML
    private void initialize() {
        // EventBus registration
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Make hidden panes not take space
        cardProfileView.managedProperty().bind(cardProfileView.visibleProperty());
        CardProfileEditPane.managedProperty().bind(CardProfileEditPane.visibleProperty());

        // Start in view mode
        CardProfileEditPane.setVisible(false);
        cardProfileView.setVisible(true);

        // Wire actions
        EditBtn.setOnAction(e -> switchToEdit());
        CancelBtn.setOnAction(e -> cancelEdit());
        SaveBtn.setOnAction(e -> saveProfile());

        // Disable Save when invalid or mid-save
        SaveBtn.disableProperty().bind(
                saving
                        .or(Bindings.createBooleanBinding(
                                () -> !isValidName(NameField.getText()), NameField.textProperty()))
                        .or(Bindings.createBooleanBinding(
                                () -> !isValidEmail(EmailField.getText()), EmailField.textProperty()))
                        .or(Bindings.createBooleanBinding(
                                () -> !isValidPhone(PhoneEmail.getText()), PhoneEmail.textProperty()))
        );

        // Disable Edit while loading
        EditBtn.disableProperty().bind(loading);

        // Do NOT call requestOverview() here; wait for setCustomerId(...)
    }

    /** Call this from parent when leaving the screen. */
    public void onClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    // ===== UI helpers =====

    private void switchToEdit() {
        // Pre-fill fields
        NameField.setText(currentName);
        EmailField.setText(currentEmail);
        PhoneEmail.setText(currentPhone);

        cardProfileView.setVisible(false);
        CardProfileEditPane.setVisible(true);
    }

    private void cancelEdit() {
        CardProfileEditPane.setVisible(false);
        cardProfileView.setVisible(true);
    }

    private void setEditDisabled(boolean b) {
        CardProfileEditPane.setDisable(b);
    }


    private void saveProfile() {
        if (saving.get()) return;

        final String newName  = safeTrim(NameField.getText());
        final String newEmail = safeTrim(EmailField.getText());
        final String newPhone = safeTrim(PhoneEmail.getText());

        if (!isValidName(newName) || !isValidEmail(newEmail) || !isValidPhone(newPhone)) {
            showError("Please fix the highlighted fields.");
            return;
        }
        if (Objects.equals(newName, currentName)
                && Objects.equals(newEmail, currentEmail)
                && Objects.equals(newPhone, currentPhone)) {
            cancelEdit();
            return;
        }

        saving.set(true);
        setEditDisabled(true);

        // allow id==0 (server will infer from socket)
        final long idForUpdate = currentId > 0 ? currentId : 0L;

        try {
            SimpleClient.getClient().sendSafely(
                    new UpdateCustomerProfileRequest(idForUpdate, newName, newEmail, newPhone)
            );
        } catch (Exception ex) {
            saving.set(false);
            setEditDisabled(false);
            showError("Connection error while saving profile.");
            ex.printStackTrace();
        }
    }




    private void updateViewLabels() {
        NameLabel.setText(nullToEmpty(currentName));
        EmailLabel.setText(nullToEmpty(currentEmail));
        PhoneLabel.setText(nullToEmpty(currentPhone));
    }

    // ===== Server I/O =====

    private void requestOverview() {
        if (loading.get()) return;
        loading.set(true);

        long customerId = injectedCustomerId > 0 ? injectedCustomerId : currentId;

        try {
            SimpleClient.getClient().sendSafely(new AccountOverviewRequest(customerId));
        } catch (Exception ex) {
            loading.set(false);
            showError("Connection error while loading profile.");
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onAccountOverview(AccountOverviewResponse resp) {
        Platform.runLater(() -> {
            try {
                if (!resp.ok()) {
                    showError("Failed to load profile: " + nullToEmpty(resp.reason()));
                    return;
                }
                var dto = resp.customer();
                if (dto == null) {
                    showError("Failed to load profile.");
                    return;
                }
                currentId    = dto.getId();
                currentName  = dto.getDisplayName();
                currentEmail = dto.getEmail();
                currentPhone = dto.getPhone();

                updateViewLabels();
            } finally {
                loading.set(false);
            }
        });
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onUpdateProfile(UpdateCustomerProfileResponse resp) {
        Platform.runLater(() -> {
            try {
                if (!resp.ok()) {
                    showError(nullToEmpty(resp.reason()));
                    return;
                }
                var dto = resp.customer();
                if (dto == null) {
                    showError("Server returned empty profile.");
                    return;
                }

                // snapshot from server
                currentId    = dto.getId();
                currentName  = dto.getDisplayName();
                currentEmail = dto.getEmail();
                currentPhone = dto.getPhone();

                updateViewLabels();

                // flip back to view
                CardProfileEditPane.setVisible(false);
                cardProfileView.setVisible(true);
                showInfo("Profile updated.");
            } finally {
                saving.set(false);
                setEditDisabled(false);
            }
        });
    }



    // ===== Validation & utils =====

    private static boolean isValidName(String s) {
        String x = safeTrim(s);
        return x.length() >= 2 && x.length() <= 120;
    }

    private static boolean isValidEmail(String s) {
        String x = safeTrim(s);
        return x.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static boolean isValidPhone(String s) {
        String x = safeTrim(s);
        if (x.isEmpty()) return true; // optional phone
        // digits, spaces, +, -, parentheses, up to 32 chars
        return x.matches("^[0-9+()\\-\\s]{3,32}$");
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private static void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
