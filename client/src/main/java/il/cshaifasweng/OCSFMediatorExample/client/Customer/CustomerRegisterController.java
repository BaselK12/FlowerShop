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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CustomerRegisterController {

    // Return target. Set from caller before opening this screen if needed.
    private static volatile String returnToFxml =
            "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml";
    public static void setReturnTo(String fxml) { returnToFxml = fxml; }

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

    @FXML
    private void initialize() {
        System.out.println("[RegisterUI] init");

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            System.out.println("[RegisterUI] EventBus registered");
        }

        // Validation binding
        BooleanBinding invalidForm =
                Bindings.createBooleanBinding(
                                () -> !isValidEmail(EmailTxt.getText().trim()),
                                EmailTxt.textProperty()
                        )
                        .or(NameTxt.textProperty().isEmpty())
                        .or(PassTxt.textProperty().length().lessThan(6))
                        .or(Bindings.createBooleanBinding(
                                () -> !PassTxt.getText().equals(ConfirmTxt.getText()),
                                PassTxt.textProperty(), ConfirmTxt.textProperty()
                        ));

        // Button disable is now: busy OR invalid
        RegisterBtn.disableProperty().bind(busy.or(invalidForm));

        // Live logging for your sanity
        Runnable logValidity = () -> {
            boolean emailOk = isValidEmail(EmailTxt.getText().trim());
            boolean nameOk  = !NameTxt.getText().trim().isEmpty();
            boolean passOk  = PassTxt.getText() != null && PassTxt.getText().length() >= 6;
            boolean matchOk = PassTxt.getText().equals(ConfirmTxt.getText());
            boolean disabled = RegisterBtn.isDisabled();
            System.out.println("[RegisterUI] valid? email=" + emailOk +
                    " name=" + nameOk + " passLen>=6=" + passOk +
                    " confirmMatch=" + matchOk + " busy=" + busy.get() +
                    " -> disabled=" + disabled);
        };
        RegisterBtn.disabledProperty().addListener((o,a,b) -> logValidity.run());
        EmailTxt.textProperty().addListener((o,a,b) -> logValidity.run());
        NameTxt.textProperty().addListener((o,a,b) -> logValidity.run());
        PassTxt.textProperty().addListener((o,a,b) -> logValidity.run());
        ConfirmTxt.textProperty().addListener((o,a,b) -> logValidity.run());

        // Wire actions explicitly
        RegisterBtn.setOnAction(e -> {
            System.out.println("[RegisterUI] Register clicked");
            RegisterBtnOnAction();
        });
        BackBtn.setOnAction(e -> {
            System.out.println("[RegisterUI] Back clicked -> " + returnToFxml);
            cleanup();
            Nav.go(BackBtn, returnToFxml);
        });

        // Enter to submit
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
        if (inFlight) {
            System.out.println("[RegisterUI] Ignored, request in flight");
            return;
        }
        try {
            ErrorLabel.setText("");
            final String name   = NameTxt.getText().trim();
            final String email  = EmailTxt.getText().trim();
            final String phone  = PhoneTxt.getText().trim().isEmpty() ? null : PhoneTxt.getText().trim();
            final String pass   = PassTxt.getText();
            final String confirm= ConfirmTxt.getText();

            if (!isValidEmail(email)) { ErrorLabel.setText("Please enter a valid email."); System.out.println("[RegisterUI] invalid email: " + email); return; }
            if (name.isEmpty())       { ErrorLabel.setText("Name is required."); System.out.println("[RegisterUI] empty name"); return; }
            if (pass == null || pass.length() < 6) { ErrorLabel.setText("Password must be at least 6 characters."); System.out.println("[RegisterUI] short password"); return; }
            if (!pass.equals(confirm)){ ErrorLabel.setText("Passwords do not match."); System.out.println("[RegisterUI] confirm mismatch"); return; }

            System.out.println("[RegisterUI] Sending RegisterRequest -> " + email);
            setBusy(true);
            inFlight = true;

            SimpleClient.getClient().sendToServer(new RegisterRequest(email, pass, name, phone));
        } catch (Exception e) {
            e.printStackTrace();
            setBusy(false);
            inFlight = false;
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
