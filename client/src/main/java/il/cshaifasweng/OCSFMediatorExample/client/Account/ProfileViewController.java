package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileResponse;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.StoreOption;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.SetPremiumRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.SetStoreRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.PaymentDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Payment;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.common.RequiresSession;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import org.greenrobot.eventbus.ThreadMode;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;

public class ProfileViewController implements RequiresSession {

    // Containers
    @FXML private AnchorPane ProfilePane;
    @FXML private VBox ProfileBox;
    @FXML private AnchorPane CardProfileViewPane;
    @FXML private AnchorPane CardProfileEditPane;

    // View Mode
    @FXML private Label NameLabel;
    @FXML private Label EmailLabel;
    @FXML private Label PhoneLabel;
    @FXML private Label accountTypeLabel;
    @FXML private Label premiumLabel;
    @FXML private Button EditBtn;

    // Edit Mode
    @FXML private TextField NameField;
    @FXML private TextField EmailField;
    @FXML private TextField PhoneEmail;
    @FXML private ComboBox<StoreOption> storeCombo;
    @FXML private CheckBox premiumCheck;
    @FXML private Button CancelBtn;
    @FXML private Button SaveBtn;

    // State
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty saving  = new SimpleBooleanProperty(false);

    private long   currentId     = 0L;
    private String currentName   = "";
    private String currentEmail  = "";
    private String currentPhone  = "";

    private Long   currentStoreId   = null; // null => Global
    private String currentStoreName = null;
    private boolean currentPremium  = false;

    private boolean returnToViewAfterSave = false;

    // Cached stores (plus a manual "Global" sentinel)
    private final List<StoreOption> stores = new ArrayList<>();
    private StoreOption globalOption;

    public void setCustomerId(long id) { this.currentId = id; }

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (currentId == 0L) {
            long id = ClientSession.getCustomerId();
            if (id > 0) currentId = id;
        }

        // Buttons follow state via bindings; never call setDisable on them directly
        if (EditBtn != null)   EditBtn.disableProperty().bind(saving);
        if (SaveBtn != null)   SaveBtn.disableProperty().bind(saving);
        if (CancelBtn != null) CancelBtn.disableProperty().bind(saving);

        // Wire actions (harmless if FXML already wires)
        if (EditBtn != null)   EditBtn.setOnAction(e -> switchToEdit());
        if (CancelBtn != null) CancelBtn.setOnAction(e -> cancelEdit());
        if (SaveBtn != null)   SaveBtn.setOnAction(e -> saveProfile());

        // Start with view visible, edit hidden, and hide action buttons
        if (CardProfileViewPane != null) CardProfileViewPane.setVisible(true);
        if (CardProfileEditPane != null) CardProfileEditPane.setVisible(false);
        if (SaveBtn != null)   SaveBtn.setVisible(false);
        if (CancelBtn != null) CancelBtn.setVisible(false);

        requestOverview();
        requestStores();

        // When ticking Premium on, require payment; cancel reverts the check
        if (premiumCheck != null) {
            premiumCheck.selectedProperty().addListener((obs, oldV, newV) -> {
                if (oldV == Boolean.FALSE && newV == Boolean.TRUE) {
                    var ok = promptForPremiumPayment();
                    if (!ok) premiumCheck.setSelected(false);
                }
            });
        }

        // Auto-unregister when the view detaches
        if (ProfilePane != null) {
            ProfilePane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null && EventBus.getDefault().isRegistered(this)) {
                    EventBus.getDefault().unregister(this);
                }
            });
        }
    }

    @FXML
    private void onClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @FXML
    private void switchToEdit() {
        // keep view visible
        if (CardProfileEditPane != null) CardProfileEditPane.setVisible(true);
        if (SaveBtn != null)   SaveBtn.setVisible(true);
        if (CancelBtn != null) CancelBtn.setVisible(true);

        // ensure inputs are enabled in case a previous save left them disabled
        setEditDisabled(false);

        // prefill
        if (NameField != null)   NameField.setText(currentName);
        if (EmailField != null)  EmailField.setText(currentEmail);
        if (PhoneEmail != null)  PhoneEmail.setText(currentPhone);
        selectCurrentStoreInCombo();
        if (premiumCheck != null) premiumCheck.setSelected(currentPremium);
    }

    @FXML
    private void cancelEdit() {
        if (CardProfileEditPane != null) CardProfileEditPane.setVisible(false);
        if (SaveBtn != null)   SaveBtn.setVisible(false);
        if (CancelBtn != null) CancelBtn.setVisible(false);

        // belt and suspenders: make sure next Edit starts fully enabled
        setEditDisabled(false);
    }


    private void setEditDisabled(boolean disabled) {
        safeDisable(NameField, disabled);
        safeDisable(EmailField, disabled);
        safeDisable(PhoneEmail, disabled);
        safeDisable(storeCombo, disabled);
        safeDisable(premiumCheck, disabled);
        // Do not touch EditBtn/SaveBtn/CancelBtn; they are bound.
    }

    private static void safeDisable(javafx.scene.control.Control c, boolean disabled) {
        if (c == null) return;
        if (c.disableProperty().isBound()) return;
        c.setDisable(disabled);
    }

    @FXML
    private void saveProfile() {
        if (saving.get()) return;

        final String newName  = safeTrim(NameField != null ? NameField.getText() : "");
        final String newEmail = safeTrim(EmailField != null ? EmailField.getText() : "");
        final String newPhone = safeTrim(PhoneEmail != null ? PhoneEmail.getText() : "");

        if (!isValidName(newName) || !isValidEmail(newEmail) || !isValidPhone(newPhone)) {
            showError("Please fix the highlighted fields.");
            return;
        }

        boolean anyChange =
                !Objects.equals(newName, currentName)
                        || !Objects.equals(newEmail, currentEmail)
                        || !Objects.equals(newPhone, currentPhone);

        // Desired store
        Long desiredStoreId = currentStoreId;
        if (storeCombo != null) {
            var sel = storeCombo.getSelectionModel().getSelectedItem();
            if (sel != null) {
                desiredStoreId = (sel == globalOption) ? null : parseStoreId(sel.id);
            }
        }
        boolean storeChanged = !Objects.equals(desiredStoreId, currentStoreId);

        // Desired premium
        boolean desiredPremium = (premiumCheck != null) && premiumCheck.isSelected();
        boolean premiumChanged = desiredPremium != currentPremium;

        if (!anyChange && !storeChanged && !premiumChanged) {
            cancelEdit();
            return;
        }

        saving.set(true);
        boolean somethingSent = false;
        final long idForUpdate = currentId > 0 ? currentId : 0L;

        // 1) Profile basics
        if (anyChange) {
            try {
                SimpleClient.getClient().sendSafely(
                        new UpdateCustomerProfileRequest(idForUpdate, newName, newEmail, newPhone)
                );
                somethingSent = true;
            } catch (Exception ex) {
                saving.set(false);
                setEditDisabled(false);
                showError("Connection error while saving profile.");
                ex.printStackTrace();
                return;
            }
        }

        // 2) Premium change
        if (premiumChanged) {
            try {
                PaymentDTO pay = null;
                if (desiredPremium) {
                    pay = buildDummyPaymentDTOFromLastDialogOrInline();
                    if (pay == null) {
                        desiredPremium = currentPremium;
                        premiumChanged = false;
                        if (premiumCheck != null) premiumCheck.setSelected(currentPremium);
                    }
                }
                if (premiumChanged) {
                    SimpleClient.getClient().sendSafely(new SetPremiumRequest(desiredPremium, pay));
                    somethingSent = true;
                }
            } catch (Exception ex) {
                saving.set(false);
                setEditDisabled(false);
                showError("Connection error while updating premium.");
                ex.printStackTrace();
                return;
            }
        }

        // 3) Store change
        if (storeChanged) {
            try {
                SimpleClient.getClient().sendSafely(new SetStoreRequest(desiredStoreId));
                somethingSent = true;
            } catch (Exception ex) {
                saving.set(false);
                setEditDisabled(false);
                showError("Connection error while updating store.");
                ex.printStackTrace();
                return;
            }
        }

        // Allow the user to keep editing immediately; we’ll still flip back to view on overview.
        returnToViewAfterSave = somethingSent;
        saving.set(false);
        setEditDisabled(false);
    }


    private void updateViewLabels() {
        if (NameLabel != null)  NameLabel.setText(nullToEmpty(currentName));
        if (EmailLabel != null) EmailLabel.setText(nullToEmpty(currentEmail));
        if (PhoneLabel != null) PhoneLabel.setText(nullToEmpty(currentPhone));

        if (accountTypeLabel != null) {
            if (currentStoreId == null) {
                accountTypeLabel.setText("Global Account");
            } else {
                String name = (currentStoreName == null || currentStoreName.isBlank()) ? "Unknown" : currentStoreName;
                accountTypeLabel.setText(name + " Branch Account");
            }
        }
        if (premiumLabel != null) premiumLabel.setText(currentPremium ? "Yes" : "No");
    }

    // ===== Server I/O =====

    private void requestOverview() {
        try {
            loading.set(true);
            SimpleClient.getClient().sendSafely(new AccountOverviewRequest(currentId > 0 ? currentId : 0L));
        } catch (Exception ex) {
            loading.set(false);
            showError("Connection error while loading profile.");
            ex.printStackTrace();
        }
    }

    private void requestStores() {
        try {
            SimpleClient.getClient().sendSafely(new GetStoresRequest());
        } catch (Exception ex) {
            // non-fatal
            ex.printStackTrace();
        }
    }

    @Subscribe
    public void onAccountOverview(AccountOverviewResponse resp) {
        Platform.runLater(() -> {
            try {
                if (resp == null || !resp.ok() || resp.customer() == null) {
                    showError("Failed to load profile" + (resp != null && resp.reason() != null ? (": " + resp.reason()) : "."));
                    return;
                }
                var dto = resp.customer();

                // Cache current state
                currentId        = dto.getId();
                currentName      = dto.getDisplayName();
                currentEmail     = dto.getEmail();
                currentPhone     = dto.getPhone();
                currentStoreId   = dto.getStoreId();
                currentStoreName = dto.getStoreName();
                currentPremium   = dto.isPremium();

                // Update view labels
                updateViewLabels();

                // If edit pane is open, keep form in sync (do not toggle panes here)
                if (CardProfileEditPane != null && CardProfileEditPane.isVisible()) {
                    if (NameField != null)   NameField.setText(currentName);
                    if (EmailField != null)  EmailField.setText(currentEmail);
                    if (PhoneEmail != null)  PhoneEmail.setText(currentPhone);
                    selectCurrentStoreInCombo();
                    if (premiumCheck != null) premiumCheck.setSelected(currentPremium);
                }

            } finally {
                loading.set(false);
                saving.set(false);
                setEditDisabled(false);

                // Only go back to view automatically if we actually saved something
                if (returnToViewAfterSave) {
                    returnToViewAfterSave = false;
                    cancelEdit();
                    showInfo("Profile updated.");
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        long id = ClientSession.getCustomerId();
        if (id > 0) {
            currentId = id;       // keep local in sync
            requestOverview();     // refresh labels/form with hydrated data
        }
    }

    @Subscribe
    public void onUpdateProfile(UpdateCustomerProfileResponse resp) {
        Platform.runLater(() -> {
            try {
                // ensure UI never remains locked
                saving.set(false);               // <— add this

                if (resp == null || !resp.ok()) {
                    showError(resp != null ? nullToEmpty(resp.reason()) : "Update failed.");
                    return;
                }
                var dto = resp.customer();
                if (dto == null) {
                    showError("Server returned empty profile.");
                    return;
                }
                currentId    = dto.getId();
                currentName  = dto.getDisplayName();
                currentEmail = dto.getEmail();
                currentPhone = dto.getPhone();

                updateViewLabels();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Unexpected error.");
            } finally {
                setEditDisabled(false);          // <— keep inputs alive
            }
        });
    }


    @Subscribe
    public void onStores(GetStoresResponse resp) {
        Platform.runLater(() -> {
            try {
                stores.clear();
                stores.addAll(resp != null && resp.stores != null ? resp.stores : List.of());

                // ensure single shared "Global" option at the end
                globalOption = new StoreOption("__global__", "Global");
                stores.removeIf(s -> "__global__".equals(s.id));
                stores.add(globalOption);

                if (storeCombo != null) {
                    storeCombo.getItems().setAll(stores);
                    selectCurrentStoreInCombo();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // ===== Helpers =====

    private void selectCurrentStoreInCombo() {
        if (storeCombo == null || storeCombo.getItems().isEmpty()) return;

        if (currentStoreId == null) {
            storeCombo.getSelectionModel().select(globalOption);
            return;
        }
        for (var opt : storeCombo.getItems()) {
            if (opt == globalOption) continue;
            Long id = parseStoreId(opt.id);
            if (Objects.equals(id, currentStoreId)) {
                storeCombo.getSelectionModel().select(opt);
                return;
            }
        }
        storeCombo.getSelectionModel().select(globalOption);
    }

    private static Long parseStoreId(String s) {
        try { return s == null ? null : Long.parseLong(s); }
        catch (NumberFormatException nfe) { return null; }
    }

    private boolean promptForPremiumPayment() {
        Dialog<Boolean> d = new Dialog<>();
        d.setTitle("Premium Subscription");
        d.setHeaderText("Enter your card details");

        Label holderLbl = new Label("Card holder:");
        TextField holder = new TextField();

        Label numberLbl = new Label("Card number (16):");
        TextField number = new TextField();

        Label monthLbl = new Label("Expiry MM:");
        ComboBox<String> mm = new ComboBox<>();
        for (int m = 1; m <= 12; m++) mm.getItems().add(String.format("%02d", m));

        Label yearLbl = new Label("Expiry YY:");
        ComboBox<String> yy = new ComboBox<>();
        int y0 = Year.now().getValue();
        for (int dy = 0; dy < 10; dy++) yy.getItems().add(String.valueOf(y0 + dy).substring(2));

        Label cvvLbl = new Label("CVV (3):");
        PasswordField cvv = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.addRow(0, holderLbl, holder);
        grid.addRow(1, numberLbl, number);
        grid.addRow(2, monthLbl, mm);
        grid.addRow(3, yearLbl, yy);
        grid.addRow(4, cvvLbl, cvv);

        d.getDialogPane().setContent(grid);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        var okBtn = d.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.disableProperty().bind(
                Bindings.createBooleanBinding(() ->
                                holder.getText().trim().isEmpty()
                                        || !number.getText().trim().matches("\\d{16}")
                                        || mm.getValue() == null
                                        || yy.getValue() == null
                                        || !cvv.getText().trim().matches("\\d{3}"),
                        holder.textProperty(), number.textProperty(), mm.valueProperty(), yy.valueProperty(), cvv.textProperty())
        );

        d.setResultConverter(bt -> bt == ButtonType.OK);
        var res = d.showAndWait();
        return res.isPresent() && res.get();
    }

    private PaymentDTO buildDummyPaymentDTOFromLastDialogOrInline() {
        PaymentDTO dto = new PaymentDTO();
        dto.setMethod(Payment.Method.CREDIT_CARD);
        dto.setMaskedCardNumber("**** **** **** 4242");
        dto.setCardHolderName("Premium User");
        dto.setExpirationDate("12/30");
        dto.setAmount(0.0);
        return dto;
    }

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }

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
        if (x.isEmpty()) return true;
        return x.matches("^[0-9+()\\-\\s]{0,32}$");
    }

    private static void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private static void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
