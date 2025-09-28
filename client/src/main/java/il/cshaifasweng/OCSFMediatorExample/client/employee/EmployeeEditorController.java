package il.cshaifasweng.OCSFMediatorExample.client.employee;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Gender;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.CreateEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.UpdateEmployeeRequest;
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
    private EmployeeVM employee;

    private static final Pattern EMAIL_RE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_RE = Pattern.compile("^(0\\d{1,2}-?\\d{3}-?\\d{4}|0\\d{8,9}|\\+?\\d[\\d\\- ]+)$");
    private static final Pattern MONEY_RE = Pattern.compile("^\\d+(?:[\\.,]\\d{1,2})?$");

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

        // Salary validation
        SalaryTxt.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) return change;
            return MONEY_RE.matcher(change.getControlNewText().replace(',', '.')).matches() ? change : null;
        }));
    }

    private void setError(String msg) {
        if (ErrorLabel != null) ErrorLabel.setText(msg == null ? "" : msg);
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
            NameTxt.setText(employee.getName());
            EmailTxt.setText(employee.getEmail());
            PhoneTxt.setText(employee.getPhone());
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
        String name     = NameTxt.getText() != null ? NameTxt.getText().trim() : "";
        String email    = EmailTxt.getText() != null ? EmailTxt.getText().trim() : "";
        String phone    = PhoneTxt.getText() != null ? PhoneTxt.getText().trim() : "";
        String salaryStr= SalaryTxt.getText() != null ? SalaryTxt.getText().trim() : "";
        EmployeeRole role   = RoleBox.getValue();
        Gender gender       = GenderBox.getValue();
        boolean active  = ActiveBox.isSelected();

        // === Required fields check ===
        if (name.isEmpty()) {
            ErrorLabel.setText("Name is required.");
            return;
        }
        if (email.isEmpty()) {
            ErrorLabel.setText("Email is required.");
            return;
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            ErrorLabel.setText("Invalid email format.");
            return;
        }
        if (phone.isEmpty()) {
            ErrorLabel.setText("Phone is required.");
            return;
        }
        if (!phone.matches("^(0\\d{1,2}-?\\d{3}-?\\d{4}|0\\d{8,9}|\\+?\\d[\\d\\- ]+)$")) {
            ErrorLabel.setText("Invalid phone number.");
            return;
        }
        if (role == null) {
            ErrorLabel.setText("Role is required.");
            return;
        }
        if (gender == null) {
            ErrorLabel.setText("Gender is required.");
            return;
        }
        if (salaryStr.isEmpty()) {
            ErrorLabel.setText("Salary is required.");
            return;
        }

        // === Salary check ===
        long salary;
        try {
            salary = Long.parseLong(salaryStr);
            if (salary < 0) {
                ErrorLabel.setText("Salary cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            ErrorLabel.setText("Salary must be a number.");
            return;
        }

        // === Build request ===
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

                App.getClient().sendToServer(new CreateEmployeeRequest(dto));

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
                App.getClient().sendToServer(new UpdateEmployeeRequest(dto));
            }
            stage.close();
        } catch (Exception e) {
            ErrorLabel.setText("Network error: " + e.getMessage());
            e.printStackTrace();
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
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
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
            // unregister from EventBus if still registered
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        } catch (Exception ignore) {}

        // close the Stage
        if (stage != null) {
            stage.close();
        } else if (SaveBtn != null && SaveBtn.getScene() != null) {
            var w = SaveBtn.getScene().getWindow();
            if (w instanceof Stage s) {
                s.close();
            }
        }
    }

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

