package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;


import java.io.Serializable;

public class UpdateEmployeeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private EmployeesDTO.Update employee;   // update DTO

    public UpdateEmployeeRequest() {}
    public UpdateEmployeeRequest(EmployeesDTO.Update employee) {
        this.employee = employee;
    }

    public EmployeesDTO.Update getEmployee() { return employee; }
    public void setEmployee(EmployeesDTO.Update employee) { this.employee = employee; }
}
