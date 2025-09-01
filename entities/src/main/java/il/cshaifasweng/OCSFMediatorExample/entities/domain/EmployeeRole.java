package il.cshaifasweng.OCSFMediatorExample.entities.domain;
import java.io.Serializable;

public class EmployeeRole implements Serializable {
    private String roleName;

    public EmployeeRole() {}
    public EmployeeRole(String roleName) { this.roleName = roleName; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
