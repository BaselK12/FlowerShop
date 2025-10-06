package il.cshaifasweng.OCSFMediatorExample.client.Customer;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CustomerLoginPageController {

    @FXML private Button BackBtn, LoginBtn, RegisterBtn;
    @FXML private Label  EmailLabel, ErrorLabel, PassLabel;
    @FXML private TextField EmailTxt;
    @FXML private PasswordField PassTxt;

    // prevent double-click spam
    private volatile boolean loggingIn = false;

    private boolean isValidEmail(String s) {
        return s != null && s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @FXML
    private void initialize() {
        // Subscribe once
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Disable login button when inputs are empty or email invalid, or while logging in
        LoginBtn.disableProperty().bind(
                EmailTxt.textProperty().isEmpty()
                        .or(PassTxt.textProperty().isEmpty())
                        .or(Bindings.createBooleanBinding(
                                () -> !isValidEmail(EmailTxt.getText()),
                                EmailTxt.textProperty()))
                        .or(Bindings.createBooleanBinding(() -> loggingIn)) // reflects volatile field via lambda
        );

        ErrorLabel.setText("");
    }

    // Call this when leaving the screen (e.g., after successful nav or when Nav.back is used)
    public void onClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @FXML
    public void BackBtnOnAction(ActionEvent e) {
        onClose();
        Nav.back(BackBtn);
    }

    @FXML
    public void RegisterBtnOnAction(ActionEvent e) {
        onClose();
        Nav.go(RegisterBtn, "/il/cshaifasweng/OCSFMediatorExample/client/CustomerRegister.fxml");
    }

    @FXML
    public void LoginBtnOnAction(ActionEvent e) {
        ErrorLabel.setText("");

        final String email = EmailTxt.getText() == null ? "" : EmailTxt.getText().trim();
        final String pass  = PassTxt.getText() == null ? "" : PassTxt.getText();

        if (!isValidEmail(email)) {
            ErrorLabel.setText("Please enter a valid email.");
            return;
        }
        if (loggingIn) return; // extra guard

        loggingIn = true; // triggers button disable via binding above

        try {
            // Use sendSafely to ensure connection (reopen if needed)
            SimpleClient.getClient().sendSafely(new LoginRequest(email, pass));
        } catch (Exception ex) {
            loggingIn = false;
            ErrorLabel.setText("Connection error");
            ex.printStackTrace();
        }
    }

    @Subscribe
    public void onServer(ServerMessageEvent ev) {
        final Object payload = ev.getPayload();
        if (!(payload instanceof LoginResponse)) return;

        final LoginResponse r = (LoginResponse) payload;

        Platform.runLater(() -> {
            try {
                if (r.isOk()) {
                    ErrorLabel.setText("");
                    onClose();
                    // Navigate to catalog
                    Nav.go(LoginBtn, "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml");
                } else {
                    ErrorLabel.setText(r.getReason() != null ? r.getReason() : "Login failed");
                }
            } finally {
                loggingIn = false; // re-enable login after handling result
            }
        });
    }
}
