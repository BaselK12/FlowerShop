package il.cshaifasweng.OCSFMediatorExample.client.Customer;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterResponse;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Map;

public class CustomerRegisterController {

    // Return target. Set from caller before opening this screen if needed.
    private static volatile String returnToFxml =
            "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml";
    public static void setReturnTo(String fxml) { returnToFxml = fxml; }

    @FXML private ComboBox<String> accountTypeCombo;
    @FXML private Label accountDescriptionLabel;

    @FXML private HBox storeContainerHbox; // stays hidden; we're mapping directly by choice
    @FXML private ComboBox<String> storeCombo; // unused now; leave it alone

    @FXML private VBox PremiumContainer;
    @FXML private Label PremiumNoteLabel;
    @FXML private CheckBox PremiumAgreementCheck;
    @FXML private CheckBox PremiumOptInCheck;


    @FXML private Button BackBtn;
    @FXML private PasswordField ConfirmTxt;
    @FXML private TextField EmailTxt;
    @FXML private Label ErrorLabel;
    @FXML private TextField NameTxt;
    @FXML private PasswordField PassTxt;
    @FXML private TextField PhoneTxt;
    @FXML private Button RegisterBtn;

    private volatile boolean inFlight = false;

    // NEW: busy flag to combine with validation
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private static final String CH_HAIFA      = "Flowershop Haifa Branch";
    private static final String CH_TEL_AVIV   = "Flowershop Tel Aviv Branch";
    private static final String CH_JERUSALEM  = "Flowershop Jerusalem Branch";
    private static final String CH_BEERSHEBA  = "Flowershop Beersheba Branch";
    private static final String CH_GLOBAL     = "Global Account";

    private static final Map<String, Long> STORE_IDS = Map.of(
            CH_HAIFA,     1L,
            CH_TEL_AVIV,  2L,
            CH_JERUSALEM, 3L,
            CH_BEERSHEBA, 4L
    );

    @FXML
    private void initialize() {
        System.out.println("[RegisterUI] init");

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            System.out.println("[RegisterUI] EventBus registered");
        }

        // Populate account types
        accountTypeCombo.getItems().setAll(
                CH_HAIFA, CH_TEL_AVIV, CH_JERUSALEM, CH_BEERSHEBA, CH_GLOBAL
        );

        accountTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel == null) {
                accountDescriptionLabel.setText("");
                storeContainerHbox.setVisible(false);
                return;
            }
            switch (sel) {
                case CH_GLOBAL -> {
                    accountDescriptionLabel.setText("Global Account: valid in all branches. No store binding.");
                    storeContainerHbox.setVisible(false);
                }
                case CH_HAIFA, CH_TEL_AVIV, CH_JERUSALEM, CH_BEERSHEBA -> {
                    accountDescriptionLabel.setText("Branch account: tied to selected branch for local perks and support.");
                    storeContainerHbox.setVisible(false); // we map by choice; no second selection needed
                }
                default -> {
                    accountDescriptionLabel.setText("");
                    storeContainerHbox.setVisible(false);
                }
            }
        });

        // Premium UI
        PremiumNoteLabel.setText("Premium accounts get 10% discount on all orders above 50 shekels");


        PremiumContainer.visibleProperty().bind(PremiumOptInCheck.selectedProperty());
        PremiumContainer.managedProperty().bind(PremiumContainer.visibleProperty());


        PremiumOptInCheck.selectedProperty().addListener((obs, was, isNow) -> {
            if (!isNow) {
                PremiumAgreementCheck.setSelected(false);
            }
        });

        // Basic validation binding that you already had, plus our new checks
        BooleanBinding invalidForm =
                Bindings.createBooleanBinding(
                                () -> !isValidEmail(EmailTxt.getText().trim()),
                                EmailTxt.textProperty()
                        )
                        .or(NameTxt.textProperty().isEmpty())
                        .or(PassTxt.textProperty().isEmpty())
                        .or(ConfirmTxt.textProperty().isEmpty())
                        .or(Bindings.createBooleanBinding(
                                () -> !PassTxt.getText().equals(ConfirmTxt.getText()),
                                PassTxt.textProperty(), ConfirmTxt.textProperty()
                        ))
                        .or(Bindings.createBooleanBinding(
                                () -> accountTypeCombo.getSelectionModel().getSelectedItem() == null,
                                accountTypeCombo.getSelectionModel().selectedItemProperty()
                        ))
                        .or(Bindings.createBooleanBinding(
                                () -> PremiumOptInCheck.isSelected() && !PremiumAgreementCheck.isSelected(),
                                PremiumOptInCheck.selectedProperty(), PremiumAgreementCheck.selectedProperty()
                        ));

        RegisterBtn.disableProperty().bind(invalidForm.or(busy));
        Runnable logValidity = () -> System.out.println("[RegisterUI] form valid = " + !invalidForm.get());

        RegisterBtn.setOnAction(e -> {
            RegisterBtnOnAction();
        });
        BackBtn.setOnAction(e -> {
            System.out.println("[RegisterUI] Back clicked -> " + returnToFxml);
            cleanup();
            Nav.go(BackBtn, returnToFxml);
        });

        EmailTxt.setOnAction(e -> RegisterBtn.fire());
        ConfirmTxt.setOnAction(e -> RegisterBtn.fire());

        ErrorLabel.setText("");
        logValidity.run();
    }

    private boolean isValidEmail(String s) {
        return s != null && s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @FXML
    private void RegisterBtnOnAction() {
        ErrorLabel.setText("");

        final String email = EmailTxt.getText().trim();
        final String name = NameTxt.getText().trim();
        final String phone = PhoneTxt.getText().trim();
        final String pass = PassTxt.getText();

        final String choice = accountTypeCombo.getSelectionModel().getSelectedItem();
        final boolean premium = PremiumOptInCheck.isSelected();

        if (premium && !PremiumAgreementCheck.isSelected()) {
            ErrorLabel.setText("You must agree to the yearly Premium fee.");
            return;
        }

        // If premium, open your existing popup and wait for confirmation
        if (premium) {
            try {
                FXMLLoader fx = new FXMLLoader(getClass().getResource(
                        "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerRegisterPayment.fxml"));
                Parent root = fx.load();

                // Use your existing controller and show 100₪ price
                CustomerRegisterPaymentController ctrl = fx.getController();
                ctrl.setTotalToPay(100.0);

                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(RegisterBtn.getScene().getWindow());
                dialog.setTitle("Premium Payment");
                dialog.setResizable(false);
                dialog.setScene(new Scene(root));

                // We cannot call private fields, so we use fx:id lookups
                final boolean[] approved = { false };

                Label errLbl = (Label) root.lookup("#paymentErrorLabel");
                Button backBtn = (Button) root.lookup("#backBtn");

                // When the controller writes "Payment confirmed successfully!" we close and mark approved
                if (errLbl != null) {
                    errLbl.textProperty().addListener((obs, oldV, now) -> {
                        if (now != null && now.startsWith("Payment confirmed")) {
                            approved[0] = true;
                            dialog.close();
                        }
                    });
                }

                // Back button should close the dialog and cancel
                if (backBtn != null) {
                    backBtn.addEventHandler(javafx.event.ActionEvent.ACTION, e2 -> {
                        approved[0] = false;
                        dialog.close();
                    });
                }

                dialog.showAndWait();

                if (!approved[0]) {
                    ErrorLabel.setText("Payment was cancelled or not confirmed.");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                ErrorLabel.setText("Could not open payment dialog.");
                return;
            }
        }

        // Map choice to store id (null means global)
        Long storeId = null;
        if (!CH_GLOBAL.equals(choice)) {
            storeId = STORE_IDS.get(choice);
            if (storeId == null) {
                ErrorLabel.setText("Unknown branch selection.");
                return;
            }
        }

        setBusy(true);
        try {
            // NOTE: you must add this constructor to your RegisterRequest (see Fix 2)
            var req = new RegisterRequest(
                    email,
                    pass,
                    name,
                    phone.isBlank() ? null : phone,
                    storeId,     // null -> Global
                    premium
            );
            SimpleClient.getClient().sendToServer(req);
        } catch (Exception ex) {
            ex.printStackTrace();
            setBusy(false);
            ErrorLabel.setText("Failed to send request.");
        }
    }



    @Subscribe
    public void onRegisterResponse(RegisterResponse r) {
        System.out.println("[RegisterUI] Got RegisterResponse ok=" + r.isOk() + " reason=" + r.getReason());
        Platform.runLater(() -> {
            setBusy(false);
            inFlight = false;

            if (r.isOk()) {
                ErrorLabel.setText("");
                cleanup();
                // Success -> hop to login (or wherever caller set)
                Nav.go(RegisterBtn, returnToFxml);
            } else {
                String reason = (r.getReason() != null && !r.getReason().isBlank())
                        ? r.getReason()
                        : "Registration failed";
                ErrorLabel.setText(reason);
            }
        });
    }

    // Only disable the other inputs. The button is controlled by binding.
    private void setBusy(boolean b) {
        busy.set(b);  // this drives RegisterBtn.disableProperty via binding
        BackBtn.setDisable(b);
        EmailTxt.setDisable(b);
        NameTxt.setDisable(b);
        PhoneTxt.setDisable(b);
        PassTxt.setDisable(b);
        ConfirmTxt.setDisable(b);
    }

    private void cleanup() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            System.out.println("[RegisterUI] EventBus unregistered");
        }
    }
}
