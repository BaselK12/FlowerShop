package il.cshaifasweng.OCSFMediatorExample.client.Admin;
import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Admin.AdminLoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Admin.AdminLoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URL;

public class AdminLoginPageController {

    private static volatile String returnToFxml =
            "/il/cshaifasweng/OCSFMediatorExample/client/HomePage/HomePage.fxml";
    public static void setReturnTo(String fxml) { returnToFxml = fxml; }
    private final BooleanProperty loggingIn = new SimpleBooleanProperty(false);


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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
            System.out.println("[LoginUI] LoginBtnOnAction");
            App.getClient().sendToServer(new AdminLoginRequest(email, pass));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onAdminLoginResponse(AdminLoginResponse r) {
        Platform.runLater(() -> {
            try {
                if (r.isSuccess()) {
                    ErrorLabel.setText("");

                    // HYDRATE SESSION OR ADMIN DASHBOARD INIT (optional)
                    try {
                        // Example: request admin dashboard data if needed
                        // SimpleClient.getClient().sendSafely(new AdminDashboardRequest());
                    } catch (Exception ex) {
                        ex.printStackTrace(); // non-fatal
                    }

                    // Verify FXML path for admin dashboard or management view
                    URL url = getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/Admin/AdminDashboard.fxml");
                    if (url == null) {
                        ErrorLabel.setText("AdminDashboard.fxml not found on classpath");
                        return;
                    }

                    cleanup();
                    Nav.go(LoginBtn, "/il/cshaifasweng/OCSFMediatorExample/client/Admin/AdminDashboard.fxml");

                } else {
                    ErrorLabel.setText(r.getMessage() != null ? r.getMessage() : "Admin login failed");
                }
            } finally {
                loggingIn.set(false);
            }
        });
    }


    private void cleanup() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            System.out.println("[RegisterUI] EventBus unregistered");
        }
    }





}

