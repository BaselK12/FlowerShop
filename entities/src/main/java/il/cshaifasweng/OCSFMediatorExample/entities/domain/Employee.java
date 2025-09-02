package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import java.io.Serializable;

public class Employee implements Serializable {
    private String id;
    private String name;
    private EmployeeRole role;

    public Employee() {}

    public Employee(String id, String name, EmployeeRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EmployeeRole getRole() { return role; }
    public void setRole(EmployeeRole role) { this.role = role; }
}
