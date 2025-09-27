package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;
import java.io.Serializable;

public class CreateEmployeeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private EmployeesDTO.Employee employee;   // created employee (read DTO)

    public CreateEmployeeResponse() {}
    public CreateEmployeeResponse(EmployeesDTO.Employee employee) {
        this.employee = employee;
    }

    public EmployeesDTO.Employee getEmployee() { return employee; }
    public void setEmployee(EmployeesDTO.Employee employee) { this.employee = employee; }
}
