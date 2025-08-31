package il.cshaifasweng.OCSFMediatorExample.client.employee;

import java.io.Serializable;
import java.util.List;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;

public final class EmployeesEvents {
    private EmployeesEvents() {}

    // Full list replaced (e.g., after FETCH_EMPLOYEES)
    public static final class EmployeesFetched implements Serializable {
        private final List<EmployeesDTO.Employee> employees;
        public EmployeesFetched(List<EmployeesDTO.Employee> employees) { this.employees = employees; }
        public List<EmployeesDTO.Employee> getEmployees() { return employees; }
    }

    // Single-row changes (after server ACKs)
    public static final class EmployeeCreated implements Serializable {
        private final EmployeesDTO.Employee employee;
        public EmployeeCreated(EmployeesDTO.Employee employee) { this.employee = employee; }
        public EmployeesDTO.Employee getEmployee() { return employee; }
    }

    public static final class EmployeeUpdated implements Serializable {
        private final EmployeesDTO.Employee employee;
        public EmployeeUpdated(EmployeesDTO.Employee employee) { this.employee = employee; }
        public EmployeesDTO.Employee getEmployee() { return employee; }
    }

    public static final class EmployeeDeleted implements Serializable {
        private final long employeeId;
        public EmployeeDeleted(long employeeId) { this.employeeId = employeeId; }
        public long getEmployeeId() { return employeeId; }
    }

    public static final class Error implements Serializable {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
