package il.cshaifasweng.OCSFMediatorExample.server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees",
        indexes = {
                @Index(name = "idx_employees_email", columnList = "email", unique = true),
                @Index(name = "idx_employees_phone", columnList = "phone")
        })
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 128)
    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Email @NotBlank @Size(max = 160)
    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @NotBlank @Size(max = 32)
    @Column(nullable = false, length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Min(0)
    @Column(nullable = false)
    private long salary; // store in minor units if you care about cents

    @PastOrPresent
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    // getters/setters
    public Long getId() { return id; }                public void setId(Long id) { this.id = id; }
    public String getName() { return name; }          public void setName(String name) { this.name = name; }
    public Gender getGender() { return gender; }      public void setGender(Gender gender) { this.gender = gender; }
    public String getEmail() { return email; }        public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }        public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }            public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }      public void setActive(boolean active) { this.active = active; }
    public long getSalary() { return salary; }        public void setSalary(long salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; } public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
