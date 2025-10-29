package il.cshaifasweng.OCSFMediatorExample.client.Customer;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.UserLoggedInEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class CustomerLoginPageController {

    @FXML private Button BackBtn, LoginBtn, RegisterBtn;
    @FXML private Label  EmailLabel, ErrorLabel, PassLabel;
    @FXML private TextField EmailTxt;
    @FXML private PasswordField PassTxt;

    private final BooleanProperty loggingIn = new SimpleBooleanProperty(false);

    private boolean isValidEmail(String s) {
        return s != null && s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

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

    /** Make sure we unregister from EventBus when the pop-up is closed. */
    public void onClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /** 2) Back closes the pop-up. No Nav, no stack acrobatics. */
    @FXML
    public void BackBtnOnAction(ActionEvent e) {
        onClose();
        Stage stage = (Stage) BackBtn.getScene().getWindow();
        if (stage != null) stage.close();
    }

    /** 3) Register opens as another pop-up on top of the login pop-up. */
    @FXML
    public void RegisterBtnOnAction(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerRegister.fxml"));
            Parent root = loader.load();

            Stage owner = (Stage) RegisterBtn.getScene().getWindow();
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            ErrorLabel.setText("Failed to open Register");
        }
    }

    /** 1) Login sends credentials; success will only emit a UI event and close. */
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

    /** Handle server login result. On success: notify opener and close this pop-up. */
    @Subscribe
    public void onLoginResponse(LoginResponse r) {
        Platform.runLater(() -> {
            try {
                if (r.isOk()) {
                    ErrorLabel.setText("");

                    // Tell whoever opened us that a user logged in.
                    final String username = EmailTxt.getText() == null ? "" : EmailTxt.getText().trim();
                    EventBus.getDefault().post(new UserLoggedInEvent(
                            username,
                            r.getDisplayName(),
                            r.getRole() == null ? Role.CUSTOMER : r.getRole()
                    ));

                    // Close the pop-up
                    onClose();
                    Stage stage = (Stage) LoginBtn.getScene().getWindow();
                    if (stage != null) stage.close();
                } else {
                    ErrorLabel.setText(r.getReason() != null ? r.getReason() : "Login failed");
                }
            } finally {
                loggingIn.set(false);
            }
        });
    }
}
