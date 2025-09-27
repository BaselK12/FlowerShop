package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.GetEmployeesRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
public record EmployeesFetchRequestedEvent(GetEmployeesRequest request, ConnectionToClient client) {}
