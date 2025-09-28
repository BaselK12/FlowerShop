package il.cshaifasweng.OCSFMediatorExample.client.Complaint;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class FilingComplaintController {

    @FXML private CheckBox AnonymousCheck;

    @FXML private Button CancelBtn;
    @FXML private Button SubmitBtn;

    @FXML private ComboBox<String> CategoryBox;

    @FXML private CheckBox ConfirmCheck;

    @FXML private Label ErrorLabel;

    @FXML private TextArea MsgTxt;

    @FXML private TextField OrderTxt;
    @FXML private TextField PhoneTxt;
    @FXML private TextField SubjectTxt;
    @FXML private TextField EmailTxt;

    @FXML
    private void initialize() {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // categories for the demo; change as needed
        CategoryBox.getItems().setAll(
                "Service quality", "Delivery delay", "Damaged item", "Other"
        );

        // wrap long messages nicely
        MsgTxt.setWrapText(true);

        // limit subject to ~120 chars without writing a custom listener
        SubjectTxt.setTextFormatter(new TextFormatter<String>(limitLength(120)));

        // anonymous toggle behavior
        AnonymousCheck.selectedProperty().addListener((obs, was, is) -> onAnonymousToggle());

        // ensure initial state is correct
        onAnonymousToggle();

        // optional: default focus
        Platform.runLater(() -> CategoryBox.requestFocus());
    }

    // helper function
    @FXML
    private void onAnonymousToggle() {
        boolean anon = AnonymousCheck.isSelected();
        EmailTxt.setDisable(anon);
        PhoneTxt.setDisable(anon);
        if (anon) {
            EmailTxt.clear();
            PhoneTxt.clear();
        }
        // also clear any previous errors when toggling
        ErrorLabel.setText("");
    }

    private static UnaryOperator<TextFormatter.Change> limitLength(int max) {
        return change -> {
            String newText = change.getControlNewText();
            return newText.length() <= max ? change : null;
        };
    }


    // Called by Scene Builder if you set onAction=onCancel on CancelBtn
    @FXML
    private void onCancel() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        closeWindow();
    }


    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isValidEmail(String s) {
        return s.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private static boolean isValidPhone(String s) {
        return s.matches("^\\+?[0-9\\- ]{7,15}$");
    }

    private void showErrors(List<String> errs) {
        if (errs == null || errs.isEmpty()) {
            ErrorLabel.setText("");
        } else {
            ErrorLabel.setText(String.join("\n", errs));
        }
    }

    private List<String> validateForm() {
        List<String> errs = new ArrayList<>();

        String category = CategoryBox.getValue();
        String subject = trim(SubjectTxt.getText());
        String message = trim(MsgTxt.getText());
        boolean anon = AnonymousCheck.isSelected();
        String email = trim(EmailTxt.getText());
        String phone = trim(PhoneTxt.getText());
        boolean consent = ConfirmCheck.isSelected();

        if (category == null || category.isBlank()) errs.add("Category is required.");
        if (subject.isEmpty()) errs.add("Subject is required.");
        if (message.isEmpty()) errs.add("Message is required.");

        if (!anon) {
            if (email.isEmpty() && phone.isEmpty()) {
                errs.add("Provide at least one contact method (email or phone).");
            }
            if (!email.isEmpty() && !isValidEmail(email)) {
                errs.add("Email format is invalid.");
            }
            if (!phone.isEmpty() && !isValidPhone(phone)) {
                errs.add("Phone format is invalid.");
            }
        }

        if (!consent) errs.add("Please confirm the consent checkbox.");

        return errs;
    }

    // Called by Scene Builder if you set onAction=onSubmit on SubmitBtn
    @FXML
    private void onSubmit() {
        List<String> errors = validateForm();
        if (!errors.isEmpty()) {
            showErrors(errors);
            return;
        }
        showErrors(List.of()); // clear
        SubmitBtn.setDisable(true);

        SubmitComplaintRequest req = new SubmitComplaintRequest(
                "customer123", // needs to replace it with the customer id that submitted the compliant
                trim(OrderTxt.getText()),
                CategoryBox.getValue(),
                trim(SubjectTxt.getText()),
                trim(MsgTxt.getText()),
                AnonymousCheck.isSelected(),
                AnonymousCheck.isSelected() ? null : trim(EmailTxt.getText()),
                AnonymousCheck.isSelected() ? null : trim(PhoneTxt.getText())
        );


        try {
            SimpleClient.getClient().sendToServer(req);
        } catch (Exception e) {
            e.printStackTrace();
            SubmitBtn.setDisable(false);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) CancelBtn.getScene().getWindow();
        if (stage != null) stage.close();
    }

    @Subscribe
    public void onComplaintResponse(SubmitComplaintResponse resp) {
        Platform.runLater(() -> {
            if (resp.isOk()) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Complaint submitted successfully.\nID: " + resp.getComplaintId()
                ).showAndWait();
                closeWindow();
            } else {
                showErrors(List.of(resp.getReason()));
                SubmitBtn.setDisable(false);
            }
        });
    }


}
