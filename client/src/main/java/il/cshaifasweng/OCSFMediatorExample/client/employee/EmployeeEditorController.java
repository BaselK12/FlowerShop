package il.cshaifasweng.OCSFMediatorExample.client.employee;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Gender;
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

//import static com.sun.org.apache.xerces.internal.util.XMLChar.trim;


public class EmployeeEditorController {

    // check boxes
    @FXML private CheckBox ActiveBox;

    // error label
    @FXML private Text ErrorLabel;

    // textfields
    @FXML private TextField NameTxt;
    @FXML private TextField PhoneTxt;
    @FXML private TextField SalaryTxt;
    @FXML private TextField EmailTxt;

    // comboBoxes
    @FXML private ComboBox<EmployeeRole> RoleBox;
    @FXML private ComboBox<Gender> GenderBox;

    // buttons
    @FXML private Button SaveBtn;
    @FXML private Button CancelBtn;

    private Stage stage;
    private Long employeeId = null;
    private boolean editMode = false;

    private Consumer<EmployeesDTO> onSave;

    private static final Pattern EMAIL_RE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_RE = Pattern.compile("^(0\\d{1,2}-?\\d{3}-?\\d{4}|0\\d{8,9}|\\+?\\d[\\d\\- ]+)$");
    private static final Pattern MONEY_RE = Pattern.compile("^\\d+(?:[\\.,]\\d{1,2})?$");

    public void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Populate comboBoxes with enums
        RoleBox.setItems(FXCollections.observableArrayList(EmployeeRole.values()));
        GenderBox.setItems(FXCollections.observableArrayList(Gender.values()));

        // Pretty display for roles
        RoleBox.setConverter(new StringConverter<>() {
            @Override public String toString(EmployeeRole role) {
                if (role == null) return "";
                return switch (role) {
                    case STORE_MANAGER -> "Manager";
                    case CASHIER -> "Cashier";
                    case FLORIST -> "Florist";
                    case DELIVERY -> "Driver";
                    case OTHER -> "Other";
                };
            }
            @Override public EmployeeRole fromString(String s) {
                return EmployeeRole.valueOf(s.toUpperCase().replace(" ", "_"));
            }
        });

        // Pretty display for genders
        GenderBox.setConverter(new StringConverter<>() {
            @Override public String toString(Gender gender) {
                if (gender == null) return "";
                return switch (gender) {
                    case Male -> "Male";
                    case Female -> "Female";
                    case Other -> "Other";
                };
            }
            @Override public Gender fromString(String s) {
                return Gender.valueOf(s.toUpperCase());
            }
        });

        // Salary validation
        SalaryTxt.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) return change;
            return MONEY_RE.matcher(change.getControlNewText().replace(',', '.')).matches() ? change : null;
        }));
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
            if (w instanceof Stage s) s.close();
        }
    }

    @FXML
    public void onSaveAction() {
        setError("");

        String name   = trim(NameTxt.getText());
        String email  = trim(EmailTxt.getText());
        String phone  = trim(PhoneTxt.getText());
        EmployeeRole role = RoleBox.getValue();
        Gender gender = GenderBox.getValue();
        String salStr = trim(SalaryTxt.getText());
        boolean active = ActiveBox.isSelected();

        if (name.isEmpty()) { setError("Name is required."); return; }
        if (!email.isEmpty() && !EMAIL_RE.matcher(email).matches()) { setError("Invalid email."); return; }
        if (!phone.isEmpty() && !PHONE_RE.matcher(phone).matches()) { setError("Invalid phone."); return; }
        if (role == null) { setError("Role is required."); return; }
        if (gender == null) { setError("Gender is required."); return; }
        if (salStr.isEmpty()) { setError("Salary is required."); return; }

        long salary;
        try {
            salary = Long.parseLong(salStr);
            if (salary < 0) { setError("Salary cannot be negative."); return; }
        } catch (NumberFormatException ex) {
            setError("Salary must be an integer."); return;
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
            closeWindow();
        } catch (Exception io) {
            setError("Network error on save: " + io.getMessage());
        }
    }

    @FXML
    public void onCancelAction() {
        closeWindow();
    }

//    public static void openEditor(javafx.stage.Window owner,
//                                  EmployeesDTO.Employee existingOrNull) {
//        try {
//            var loader = new javafx.fxml.FXMLLoader(
//                    EmployeeEditorController.class.getResource("EmployeeEditor.fxml")
//            );
//            var root = loader.load();
//            var c = loader.getController();
//
//            Stage st = new Stage();
//            st.setTitle(existingOrNull == null ? "New Employee" : "Edit Employee");
//            st.initModality(javafx.stage.Modality.WINDOW_MODAL);
//            if (owner != null) st.initOwner(owner);
//            st.setScene(new javafx.scene.Scene(root));
//
//            c.stage = st;
//            c.initialize();
//
//            if (existingOrNull != null) {
//                c.editMode = true;
//                c.employeeId = existingOrNull.getId();
//                c.NameTxt.setText(nz(existingOrNull.getName()));
//                c.EmailTxt.setText(nz(existingOrNull.getEmail()));
//                c.PhoneTxt.setText(nz(existingOrNull.getPhone()));
//                c.RoleBox.getSelectionModel().select(existingOrNull.getRole());
//                c.GenderBox.getSelectionModel().select(existingOrNull.getGender());
//                c.SalaryTxt.setText(String.valueOf(existingOrNull.getSalary()));
//                c.ActiveBox.setSelected(existingOrNull.isActive());
//            }
//
//            st.setOnHidden(e -> {
//                try {
//                    if (EventBus.getDefault().isRegistered(c)) {
//                        EventBus.getDefault().unregister(c);
//                    }
//                } catch (Throwable ignore) {}
//            });
//
//            st.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}

