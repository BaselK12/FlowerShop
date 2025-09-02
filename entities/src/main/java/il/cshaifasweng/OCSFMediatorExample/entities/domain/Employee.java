package il.cshaifasweng.OCSFMediatorExample.entities.domain;

public class Employee extends Person {
    private EmployeeRole role;

    public Employee() { super(); }

    public Employee(String id, String firstName, String lastName,
                    String email, String phone, EmployeeRole role) {
        super(id, firstName, lastName, email, phone);
        this.role = role;
    }

    public EmployeeRole getRole() { return role; }
    public void setRole(EmployeeRole role) { this.role = role; }

    // Optional: keep a full name for UI
    public String getName() {
        String fn = getFirstName() == null ? "" : getFirstName();
        String ln = getLastName() == null ? "" : getLastName();
        return (fn + " " + ln).trim();
    }
}
