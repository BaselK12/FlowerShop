package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import il.cshaifasweng.OCSFMediatorExample.entities.converters.RoleConverter;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee extends Person {
    // remove gender here – already inherited

    @Convert(converter = RoleConverter.class)
    @Column(nullable = false, length = 100)
    private EmployeeRole role;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private long salary;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    public Employee() { super(); }

    public Employee(String name, String email, String phone, Gender gender,
                    EmployeeRole role, boolean active,
                    long salary, LocalDate hireDate, String passwordHash) {
        super(name, email, phone, gender);
        this.role = role;
        this.active = active;
        this.salary = salary;
        this.hireDate = hireDate;
        this.passwordHash = passwordHash;
    }



    public EmployeeRole getRole() { return role; }
    public void setRole(EmployeeRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getSalary() { return salary; }
    public void setSalary(long salary) { this.salary = salary; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
