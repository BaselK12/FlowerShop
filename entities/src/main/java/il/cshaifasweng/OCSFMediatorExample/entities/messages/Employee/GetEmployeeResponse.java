package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;

import java.io.Serializable;

public class GetEmployeeResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private EmployeesDTO.Employee employee;

    public GetEmployeeResponse() {}
    public GetEmployeeResponse(EmployeesDTO.Employee employee) { this.employee = employee; }

    public EmployeesDTO.Employee getEmployee() { return employee; }
    public void setEmployee(EmployeesDTO.Employee employee) { this.employee = employee; }
}