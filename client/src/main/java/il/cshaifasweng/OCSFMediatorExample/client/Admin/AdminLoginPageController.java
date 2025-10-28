package il.cshaifasweng.OCSFMediatorExample.client.Admin;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;

public class AdminLoginPageController {

    private static volatile String returnToFxml =
            "/il/cshaifasweng/OCSFMediatorExample/client/HomePage/HomePage.fxml";
    public static void setReturnTo(String fxml) { returnToFxml = fxml; }

    @FXML // fx:id="BackBtn"
    private Button BackBtn; // Value injected by FXMLLoader

    @FXML // fx:id="EmailLabel"
    private Label EmailLabel; // Value injected by FXMLLoader

    @FXML // fx:id="EmailTxt"
    private TextField EmailTxt; // Value injected by FXMLLoader

    @FXML // fx:id="ErrorLabel"
    private Label ErrorLabel; // Value injected by FXMLLoader

    @FXML // fx:id="LoginBtn"
    private Button LoginBtn; // Value injected by FXMLLoader

    @FXML // fx:id="PassLabel"
    private Label PassLabel; // Value injected by FXMLLoader

    @FXML // fx:id="PassTxt"
    private PasswordField PassTxt; // Value injected by FXMLLoader

    @FXML // fx:id="RegisterBtn"
    private Button RegisterBtn; // Value injected by FXMLLoader

    private boolean isValidEmail(String s) {
        if (s == null) return false;
        return s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @FXML
    private void initialize() {
        // Disable Login until basic validation passes
        LoginBtn.disableProperty().bind(
                EmailTxt.textProperty().isEmpty()
                        .or(PassTxt.textProperty().isEmpty())
                        .or(Bindings.createBooleanBinding(
                                () -> !isValidEmail(EmailTxt.getText()),
                                EmailTxt.textProperty()))
        );
        ErrorLabel.setText(""); // start clean
        BackBtn.setOnAction(e -> {
            System.out.println("[RegisterUI] Back clicked -> " + returnToFxml);
            cleanup();
            Nav.go(BackBtn, returnToFxml);
        });

    }

    @FXML
    public void BackBtnOnAction(ActionEvent event) {
        try {
            // send a msg that an admin pressed back from login page
            SimpleClient.getClient().sendToServer("AdminLoginPage Back");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void LoginBtnOnAction(ActionEvent event) {
        try {
            ErrorLabel.setText("");
            final String email = EmailTxt.getText().trim();
            final String pass  = PassTxt.getText();

            if (!isValidEmail(email)) {
                ErrorLabel.setText("Please enter a valid email.");
                return;
            }
            // send a msg that an Admin pressed login from login page
            SimpleClient.getClient().sendToServer(new LoginRequest(email, pass, Role.ADMIN));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanup() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            System.out.println("[RegisterUI] EventBus unregistered");
        }
    }





}

