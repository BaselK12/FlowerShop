package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTOs for Employee. Plain classes (no records) so they work on Java 8/11+.
 * Keep this module shared by client and server.
 */
public final class EmployeesDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private EmployeesDTO() {}

    // ===== Read model returned from the server =====
    public static final class Employee implements Serializable {
        private static final long serialVersionUID = 1L;

        private long id;
        private String name;
        private String gender;    // "Female", "Male", "Other"
        private String email;
        private String phone;
        private String role;      // "Manager", "Cashier", ...
        private boolean active;
        private long salary;
        private LocalDate hireDate;

        public Employee() { }

        public Employee(long id, String name, String gender, String email, String phone,
                        String role, boolean active, long salary, LocalDate hireDate) {
            this.id = id; this.name = name; this.gender = gender; this.email = email; this.phone = phone;
            this.role = role; this.active = active; this.salary = salary; this.hireDate = hireDate;
        }

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

    // ===== Write model for Create =====
    public static final class Create implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private String gender;
        private String email;
        private String phone;
        private String role;
        private boolean active;
        private long salary;

        public Create() { }

        public Create(String name, String gender, String email, String phone,
                      String role, boolean active, long salary) {
            this.name = name; this.gender = gender; this.email = email; this.phone = phone;
            this.role = role; this.active = active; this.salary = salary;
        }

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
    }

    // ===== Write model for Update =====
    public static final class Update implements Serializable {
        private static final long serialVersionUID = 1L;

        private long id;
        private String name;
        private String gender;
        private String email;
        private String phone;
        private String role;
        private boolean active;
        private long salary;

        public Update() { }

        public Update(long id, String name, String gender, String email, String phone,
                      String role, boolean active, long salary) {
            this.id = id; this.name = name; this.gender = gender; this.email = email; this.phone = phone;
            this.role = role; this.active = active; this.salary = salary;
        }

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
    }
}