package il.cshaifasweng.OCSFMediatorExample.client;


import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterRequest;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class CustomerRegisterController {

    @FXML // fx:id="BackBtn"
    private Button BackBtn; // Value injected by FXMLLoader

    @FXML // fx:id="ConfirmTxt"
    private PasswordField ConfirmTxt; // Value injected by FXMLLoader

    @FXML // fx:id="EmailTxt"
    private TextField EmailTxt; // Value injected by FXMLLoader

    @FXML // fx:id="ErrLabel"
    private Label ErrorLabel; // Value injected by FXMLLoader

    @FXML // fx:id="NameTxt"
    private TextField NameTxt; // Value injected by FXMLLoader

    @FXML // fx:id="PassTxt"
    private PasswordField PassTxt; // Value injected by FXMLLoader

    @FXML // fx:id="PhoneTxt"
    private TextField PhoneTxt; // Value injected by FXMLLoader

    @FXML // fx:id="RegisterBtn"
    private Button RegisterBtn; // Value injected by FXMLLoader

    @FXML
    private void initialize() {
        RegisterBtn.disableProperty().bind(
                Bindings.createBooleanBinding(() -> !isValidEmail(EmailTxt.getText().trim()), EmailTxt.textProperty())
                        .or(NameTxt.textProperty().isEmpty())
                        .or(PassTxt.textProperty().length().lessThan(6))
                        .or(Bindings.createBooleanBinding(
                                () -> !PassTxt.getText().equals(ConfirmTxt.getText()),
                                PassTxt.textProperty(), ConfirmTxt.textProperty()))
        );

        EmailTxt.setOnAction(e -> RegisterBtn.fire());
        ConfirmTxt.setOnAction(e -> RegisterBtn.fire());
        ErrorLabel.setText("");
    }

    @FXML
    private void BackBtnOnAction() {
        try {
            SimpleClient.getClient().sendToServer("register back");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setError(String text) { ErrorLabel.setText(text); }

    private boolean isValidEmail(String s) {
        return s != null && s.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @FXML
    private void RegisterBtnOnAction() {
        try {
            ErrorLabel.setText("");
            final String name  = NameTxt.getText().trim();
            final String email = EmailTxt.getText().trim();
            final String phone = PhoneTxt.getText().trim().isEmpty() ? null : PhoneTxt.getText().trim();
            final String pass  = PassTxt.getText();

            if (!isValidEmail(email)) { ErrorLabel.setText("Please enter a valid email."); return; }
            if (pass.length() < 6)    { ErrorLabel.setText("Password must be at least 6 characters."); return; }
            if (!pass.equals(ConfirmTxt.getText())) { ErrorLabel.setText("Passwords do not match."); return; }



            SimpleClient.getClient().sendToServer(new RegisterRequest(email, pass, name, phone));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}

