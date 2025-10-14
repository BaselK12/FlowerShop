package il.cshaifasweng.OCSFMediatorExample.client.Customer;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URL;

public class CustomerLoginPageController {

    @FXML private Button BackBtn, LoginBtn, RegisterBtn;
    @FXML private Label  EmailLabel, ErrorLabel, PassLabel;
    @FXML private TextField EmailTxt;
    @FXML private PasswordField PassTxt;

    // proper observable flag so bindings actually update
    private final BooleanProperty loggingIn = new SimpleBooleanProperty(false);

    private boolean isValidEmail(String s) {
        return s != null && s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Disable login when fields empty, email invalid, or mid-request
        LoginBtn.disableProperty().bind(
                EmailTxt.textProperty().isEmpty()
                        .or(PassTxt.textProperty().isEmpty())
                        .or(Bindings.createBooleanBinding(
                                () -> !isValidEmail(EmailTxt.getText()),
                                EmailTxt.textProperty()))
                        .or(loggingIn)
        );

        ErrorLabel.setText("");
    }

    // Call this before leaving the screen to avoid duplicate subscribers
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
        if (loggingIn.get()) return;

        loggingIn.set(true);
        try {
            SimpleClient.getClient().sendSafely(new LoginRequest(email, pass));
        } catch (Exception ex) {
            loggingIn.set(false);
            ErrorLabel.setText("Connection error");
            ex.printStackTrace();
        }
    }

    // SUBSCRIBE DIRECTLY to LoginResponse since SimpleClient posts raw messages
    @Subscribe
    public void onLoginResponse(LoginResponse r) {
        Platform.runLater(() -> {
            try {
                if (r.isOk()) {
                    ErrorLabel.setText("");

                    // Optional guard to fail loudly if FXML is missing during dev
                    URL url = getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/Account/MyAccount.fxml");
                    if (url == null) {
                        ErrorLabel.setText("MyAccount.fxml not found on classpath");
                        return;
                    }

                    onClose(); // unregister before navigation
                    Nav.go(LoginBtn, "/il/cshaifasweng/OCSFMediatorExample/client/Account/MyAccount.fxml");
                } else {
                    ErrorLabel.setText(r.getReason() != null ? r.getReason() : "Login failed");
                }
            } finally {
                loggingIn.set(false);
            }
        });
    }
}
