package il.cshaifasweng.OCSFMediatorExample.server.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // keep it boring and TA-friendly: strings, not enums
    private String name;
    private String gender;   // "Female", "Male", "Other" (free text for now)
    private String email;
    private String phone;
    private String role;     // "Manager", "Cashier", etc.

    private boolean active;
    private long salary;     // plain number; no cents math here

    @Column(name = "hire_date")
    private LocalDate hireDate;

    public Employee() {}

    public Employee(String name, String gender, String email, String phone,
                    String role, boolean active, long salary, LocalDate hireDate) {
        this.name = name;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    // getters / setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getSalary() { return salary; }
    public void setSalary(long salary) { this.salary = salary; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
