package il.cshaifasweng.OCSFMediatorExample.client.employee;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.greenrobot.eventbus.EventBus;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.sun.org.apache.xerces.internal.util.XMLChar.trim;

public class EmployeeEditorController {

    // check boxes
    @FXML private CheckBox ActiveBox;

    // text
    @FXML private Text ErrorLabel;

    // textfields
    @FXML private TextField NameTxt;
    @FXML private TextField PhoneTxt;
    @FXML private TextField SalaryTxt;
    @FXML private TextField EmailTxt;

    // comboBox
    @FXML private ComboBox<String> RoleBox;
    @FXML private ComboBox<String> Genderbox;

    // buttons
    @FXML private Button SaveBtn;
    @FXML private Button CancelBtn;

    private Stage stage;

    // When editing, keep the ID so we put it back on SAVE
    private Long employeeId = null;
    private boolean editMode = false;


    // Optional callback: if set, we call it with the built DTO on SAVE.
    // If not set, we post a SaveEmployeeRequest event to EventBus.
    private Consumer<EmployeesDTO> onSave;

    // Option lists (fallback). If you have canonical lists (from server/enums) call setRoleOptions/setGenderOptions.
    private final ObservableList<String> roles = FXCollections.observableArrayList(
            "Manager", "Cashier", "Florist", "Delivery", "Admin"
    );
    private final ObservableList<String> genders = FXCollections.observableArrayList(
            "Male", "Female", "Other", "Prefer not to say"
    );

    private static final Pattern EMAIL_RE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_RE = Pattern.compile("^(0\\d{1,2}-?\\d{3}-?\\d{4}|0\\d{8,9}|\\+?\\d[\\d\\- ]+)$");
    private static final Pattern MONEY_RE = Pattern.compile("^\\d+(?:[\\.,]\\d{1,2})?$");


    public void initialize() {

        // subscribe
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);


        RoleBox.setItems(roles);
        Genderbox.setItems(genders);

        SalaryTxt.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) return change;
            return MONEY_RE.matcher(change.getControlNewText().replace(',', '.')).matches() ? change : null;
        }));

        RoleBox.setConverter(new StringConverter<>() {
            @Override public String toString(String object) { return object; }
            @Override public String fromString(String string) { return string; }
        });
        Genderbox.setConverter(new StringConverter<>() {
            @Override public String toString(String object) { return object; }
            @Override public String fromString(String string) { return string; }
        });


    }

    private void setError(String msg) {
        if (ErrorLabel != null) ErrorLabel.setText(msg == null ? "" : msg);
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }
    private static String nz(String s) { return s == null ? "" : s; }
    private void closeWindow() {
        if (stage != null) stage.close();
        else {
            var w = SaveBtn != null ? SaveBtn.getScene().getWindow() : null;
            if (w instanceof javafx.stage.Stage s) s.close();
        }
    }

    // onSaveAction: validate, build Create/Update, send to server, then close
    @FXML
    public void onSaveAction() {
        if (ErrorLabel != null) ErrorLabel.setText("");

        String name   = trim(NameTxt.getText());
        String email  = trim(EmailTxt.getText());
        String phone  = trim(PhoneTxt.getText());
        String role   = RoleBox.getValue();
        String gender = Genderbox.getValue();
        String salStr = trim(SalaryTxt.getText());
        boolean active = ActiveBox.isSelected();

        if (name.isEmpty()) { setError("Name is required."); return; }
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) { setError("Invalid email."); return; }
        if (!phone.isEmpty() && !phone.matches("^(0\\d{1,2}-?\\d{3}-?\\d{4}|0\\d{8,9}|\\+?\\d[\\d\\- ]+)$")) { setError("Invalid phone."); return; }
        if (role == null || role.isBlank()) { setError("Role is required."); return; }
        if (gender == null || gender.isBlank()) { setError("Gender is required."); return; }
        if (salStr.isEmpty()) { setError("Salary is required."); return; }

        long salary;
        try {
            salary = Long.parseLong(salStr);
            if (salary < 0) { setError("Salary cannot be negative."); return; }
        } catch (NumberFormatException ex) {
            setError("Salary must be an integer number."); return;
        }

        Object dtoToSend;
        if (editMode) {
            dtoToSend = new EmployeesDTO.Update(
                    employeeId, name, gender, email, phone, role, active, salary
            );
        } else {
            dtoToSend = new EmployeesDTO.Create(
                    name, gender, email, phone, role, active, salary
            );
        }

        try {
            SimpleClient.getClient().sendToServer(dtoToSend);
        } catch (java.io.IOException io) {
            setError("Network error on save: " + io.getMessage());
            return; // keep window open so user can fix/retry
        }

        // success → just go back
        // should close the window at the end

    }

    // onCancelAction: do NOT save — simply close and go back
    @FXML
    public void onCancelAction() {
        // no server call on cancel per your requirement
        closeWindow();
    }

    // openEditor: load FXML, init controller, optionally prefill for edit, and show
    public static void openEditor(javafx.stage.Window owner,
                                  il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO.Employee existingOrNull) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    EmployeeEditorController.class.getResource("EmployeeEditor.fxml")
            );
            javafx.scene.Parent root = loader.load();
            EmployeeEditorController c = loader.getController();

            javafx.stage.Stage st = new javafx.stage.Stage();
            st.setTitle(existingOrNull == null ? "New Employee" : "Edit Employee");
            st.initModality(javafx.stage.Modality.WINDOW_MODAL);
            if (owner != null) st.initOwner(owner);
            st.setScene(new javafx.scene.Scene(root));

            c.stage = st;          // remember stage so we can close
            c.initialize();              // set up UI, subscribe, handlers

            // prefill if editing
            if (existingOrNull != null) {
                c.editMode = true;
                c.employeeId = existingOrNull.getId();
                c.NameTxt.setText(nz(existingOrNull.getName()));
                c.EmailTxt.setText(nz(existingOrNull.getEmail()));
                c.PhoneTxt.setText(nz(existingOrNull.getPhone()));
                c.RoleBox.getSelectionModel().select(nz(existingOrNull.getRole()));
                c.Genderbox.getSelectionModel().select(nz(existingOrNull.getGender()));
                c.SalaryTxt.setText(String.valueOf(existingOrNull.getSalary()));
                c.ActiveBox.setSelected(existingOrNull.isActive());
            }

            // optional: auto-unregister EventBus when window closes
            st.setOnHidden(e -> {
                try {
                    if (EventBus.getDefault().isRegistered(c) ){
                        EventBus.getDefault().unregister(c);
                    }
                } catch (Throwable ignore) {}
            });

            st.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

