package il.cshaifasweng.OCSFMediatorExample.client.employee;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Gender;
import javafx.beans.property.*;
import java.time.LocalDate;

public class EmployeeVM {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty("");
    private final ObjectProperty<Gender> gender = new SimpleObjectProperty<>(Gender.Other);
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty phone = new SimpleStringProperty("");
    private final ObjectProperty<EmployeeRole> role = new SimpleObjectProperty<>(EmployeeRole.CASHIER);
    private final BooleanProperty active = new SimpleBooleanProperty(true);
    private final LongProperty salary = new SimpleLongProperty(0);
    private final ObjectProperty<LocalDate> hireDate = new SimpleObjectProperty<>(null);

    // ===== Getters =====
    public long getId() { return id.get(); }
    public String getName() { return name.get(); }
    public Gender getGender() { return gender.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public EmployeeRole getRole() { return role.get(); }
    public boolean isActive() { return active.get(); }
    public long getSalary() { return salary.get(); }
    public LocalDate getHireDate() { return hireDate.get(); }

    // ===== Properties =====
    public LongProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public ObjectProperty<Gender> genderProperty() { return gender; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; }
    public ObjectProperty<EmployeeRole> roleProperty() { return role; }
    public BooleanProperty activeProperty() { return active; }
    public LongProperty salaryProperty() { return salary; }
    public ObjectProperty<LocalDate> hireDateProperty() { return hireDate; }

    // ===== Convenience for displaying pretty strings in the TableView =====
    public StringProperty genderDisplayProperty() {
        return new SimpleStringProperty(
                gender.get() == null ? "" : capitalize(gender.get().name())
        );
    }

    public StringProperty roleDisplayProperty() {
        return new SimpleStringProperty(
                role.get() == null ? "" : prettifyRole(role.get())
        );
    }

    // ===== Constructors =====
    public EmployeeVM(long id, String name, Gender gender, String email, String phone,
                      EmployeeRole role, boolean active, long salary, LocalDate hireDate) {
        this.id.set(id);
        this.name.set(name);
        this.gender.set(gender);
        this.email.set(email);
        this.phone.set(phone);
        this.role.set(role);
        this.active.set(active);
        this.salary.set(salary);
        this.hireDate.set(hireDate);
    }

    public EmployeeVM() {}

    // ===== Helpers =====
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
    private String capitalize(String input) {
        return input.substring(0,1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
