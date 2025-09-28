package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;

import java.io.Serializable;

public class DeleteEmployeeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private long employeeId;

    public DeleteEmployeeRequest() {}
    public DeleteEmployeeRequest(long employeeId) {
        this.employeeId = employeeId;
    }

    public long getEmployeeId() { return employeeId; }
    public void setEmployeeId(long employeeId) { this.employeeId = employeeId; }
}
