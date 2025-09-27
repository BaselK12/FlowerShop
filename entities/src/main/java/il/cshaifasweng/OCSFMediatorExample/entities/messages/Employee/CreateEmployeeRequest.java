package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;

import java.io.Serializable;

public class CreateEmployeeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private EmployeesDTO.Create employee;   // write DTO

    public CreateEmployeeRequest() {}
    public CreateEmployeeRequest(EmployeesDTO.Create employee) {
        this.employee = employee;
    }

    public EmployeesDTO.Create getEmployee() { return employee; }
    public void setEmployee(EmployeesDTO.Create employee) { this.employee = employee; }
}