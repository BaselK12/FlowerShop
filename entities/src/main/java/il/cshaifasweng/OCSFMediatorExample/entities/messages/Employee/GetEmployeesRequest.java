package il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;

import java.io.Serializable;

public class GetEmployeesRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean activeOnly;
    private EmployeeRole role;
    private String searchText;   // <-- new

    public GetEmployeesRequest() {}

    public GetEmployeesRequest(boolean activeOnly, EmployeeRole role, String searchText) {
        this.activeOnly = activeOnly;
        this.role = role;
        this.searchText = searchText;
    }

    public boolean isActiveOnly() { return activeOnly; }
    public EmployeeRole getRole() { return role; }
    public String getSearchText() { return searchText; }
}