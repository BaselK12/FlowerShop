package il.cshaifasweng.OCSFMediatorExample.client.employee;

import javafx.beans.property.*;
import java.time.LocalDate;

public class EmployeeVM {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty gender = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty phone = new SimpleStringProperty("");
    private final StringProperty role = new SimpleStringProperty("");
    private final BooleanProperty active = new SimpleBooleanProperty(true);
    private final LongProperty salary = new SimpleLongProperty(0);
    private final ObjectProperty<LocalDate> hireDate = new SimpleObjectProperty<>(null);

    public long getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getGender() { return gender.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public String getRole() { return role.get(); }
    public boolean isActive() { return active.get(); }
    public long getSalary() { return salary.get(); }
    public LocalDate getHireDate() { return hireDate.get(); }

    public LongProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty roleProperty() { return role; }
    public BooleanProperty activeProperty() { return active; }
    public LongProperty salaryProperty() { return salary; }
    public ObjectProperty<LocalDate> hireDateProperty() { return hireDate; }

    // Convenience constructor for the mock
    public EmployeeVM(long id, String name, String gender, String email, String phone,
                      String role, boolean active, long salary, LocalDate hireDate) {
        this.id.set(id); this.name.set(name); this.gender.set(gender);
        this.email.set(email); this.phone.set(phone); this.role.set(role);
        this.active.set(active); this.salary.set(salary); this.hireDate.set(hireDate);
    }

    public EmployeeVM() {}
}
