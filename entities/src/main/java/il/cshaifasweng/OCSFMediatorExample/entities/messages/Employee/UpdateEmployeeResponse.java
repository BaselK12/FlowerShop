package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;

import java.io.Serializable;

public class UpdateEmployeeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private EmployeesDTO.Employee employee;   // updated employee (read DTO)

    public UpdateEmployeeResponse() {}
    public UpdateEmployeeResponse(EmployeesDTO.Employee employee) {
        this.employee = employee;
    }

    public EmployeesDTO.Employee getEmployee() { return employee; }
    public void setEmployee(EmployeesDTO.Employee employee) { this.employee = employee; }
}