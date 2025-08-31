package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;

import java.io.Serializable;
import java.util.List;

/** Server → Client: the complete employees list. */
public class GetEmployeesResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<EmployeesDTO.Employee> employees;

    public GetEmployeesResponse() { }

    public GetEmployeesResponse(List<EmployeesDTO.Employee> employees) {
        this.employees = employees;
    }

    public List<EmployeesDTO.Employee> getEmployees() { return employees; }
    public void setEmployees(List<EmployeesDTO.Employee> employees) { this.employees = employees; }
}
