package il.cshaifasweng.OCSFMediatorExample.client.employee;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Gender;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.CreateEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.UpdateEmployeeRequest;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

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
    private EmployeeVM employee;

    private static final Pattern EMAIL_RE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_RE = Pattern.compile("^(0\\d{1,2}-?\\d{3}-?\\d{4}|0\\d{8,9}|\\+?\\d[\\d\\- ]+)$");
    private static final Pattern INT_MONEY_RE = Pattern.compile("^\\d{0,9}$"); // integer shekels

    private volatile boolean saving = false;

    public void initialize() {

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

        // Salary: integers only (₪)
        SalaryTxt.setTextFormatter(new TextFormatter<>(change -> {
            String next = change.getControlNewText();
            return INT_MONEY_RE.matcher(next).matches() ? change : null;
        }));

        // Cancel button closes cleanly
        if (CancelBtn != null) {
            CancelBtn.setOnAction(e -> onClose());
        }
    }

    private void setError(String msg) {
        if (ErrorLabel != null) ErrorLabel.setText(msg == null ? "" : msg);
    }

    private void lockUI(boolean on) {
        if (SaveBtn != null)   SaveBtn.setDisable(on);
        if (CancelBtn != null) CancelBtn.setDisable(on);
    }

    private void closeWindow() {
        if (stage != null) stage.close();
        else {
            var w = SaveBtn != null ? SaveBtn.getScene().getWindow() : null;
            if (w instanceof Stage s) s.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setEmployee(EmployeeVM employee) {
        this.employee = employee;
        if (employee != null) {
            // Fill fields for edit
            NameTxt.setText(safe(employee.getName()));
            EmailTxt.setText(safe(employee.getEmail()));
            PhoneTxt.setText(safe(employee.getPhone()));
            SalaryTxt.setText(String.valueOf(employee.getSalary()));
            RoleBox.setValue(employee.getRole());
            GenderBox.setValue(employee.getGender());
            ActiveBox.setSelected(employee.isActive());
        } else {
            // Clear fields for add
            NameTxt.clear();
            EmailTxt.clear();
            PhoneTxt.clear();
            SalaryTxt.clear();
            RoleBox.getSelectionModel().clearSelection();
            GenderBox.getSelectionModel().clearSelection();
            ActiveBox.setSelected(true); // default active
        }
    }

    @FXML
    private void onSave() {
        if (saving) return;
        setError("");

        String name      = trim(NameTxt.getText());
        String email     = trim(EmailTxt.getText());
        String phone     = trim(PhoneTxt.getText());
        String salaryStr = trim(SalaryTxt.getText());
        EmployeeRole role = RoleBox.getValue();
        Gender gender     = GenderBox.getValue();
        boolean active    = ActiveBox.isSelected();

        // === Required fields check ===
        if (name.isEmpty()) { setError("Name is required."); return; }
        if (email.isEmpty()) { setError("Email is required."); return; }
        if (!EMAIL_RE.matcher(email).matches()) { setError("Invalid email format."); return; }
        if (phone.isEmpty()) { setError("Phone is required."); return; }
        if (!PHONE_RE.matcher(phone).matches()) { setError("Invalid phone number."); return; }
        if (role == null) { setError("Role is required."); return; }
        if (gender == null) { setError("Gender is required."); return; }
        if (salaryStr.isEmpty()) { setError("Salary is required."); return; }
        if (!INT_MONEY_RE.matcher(salaryStr).matches()) { setError("Salary must be a whole number."); return; }

        long salary;
        try {
            salary = Long.parseLong(salaryStr);
            if (salary < 0) { setError("Salary cannot be negative."); return; }
        } catch (NumberFormatException e) {
            setError("Salary must be a number.");
            return;
        }

        // network guard
        var client = App.getClient();
        if (client == null || !client.isConnected()) {
            setError("Client not connected.");
            return;
        }

        lockUI(true);
        saving = true;

        try {
            if (employee == null) {
                // 1. Generate raw password
                String rawPassword = generateRandomPassword(10);
                // 2. Hash it
                String hashed = hashPassword(rawPassword);
                // 3. Create DTO with hashed password
                EmployeesDTO.Create dto = new EmployeesDTO.Create(
                        name, gender, email, phone, role, active, salary, hashed
                );
                client.sendToServer(new CreateEmployeeRequest(dto));

                // 4. Show raw password once to manager
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Employee Created");
                alert.setHeaderText("Account for " + name + " created successfully");
                alert.setContentText("Temporary password: " + rawPassword +
                        "\n⚠️ Please give it to the employee.");
                alert.showAndWait();
            } else {
                // build DTO for update
                EmployeesDTO.Update dto = new EmployeesDTO.Update(
                        employee.getId(), name, gender, email, phone, role, active, salary
                );
                client.sendToServer(new UpdateEmployeeRequest(dto));
            }
            closeWindow();
        } catch (Exception e) {
            setError("Network error: " + e.getMessage());
            e.printStackTrace();
            lockUI(false);
            saving = false;
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String hashPassword(String raw) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    @FXML
    public void onCancelAction() {
        onClose();
    }

    private void onClose() {
        try {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        } catch (Exception ignore) {}
        closeWindow();
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String trim(String s) { return s == null ? "" : s.trim(); }

    private String prettifyRole(EmployeeRole role) {
        if (role == null) return "";
        return switch (role) {
            case STORE_MANAGER -> "Manager";
            case CASHIER       -> "Cashier";
            case FLORIST       -> "Florist";
            case DELIVERY      -> "Driver";
            case OTHER         -> "Other";
        };
    }

    private String prettifyGender(Gender gender) {
        return switch (gender) {
            case Female -> "Female";
            case Male   -> "Male";
            case Other  -> "Other";
        };
    }
}
