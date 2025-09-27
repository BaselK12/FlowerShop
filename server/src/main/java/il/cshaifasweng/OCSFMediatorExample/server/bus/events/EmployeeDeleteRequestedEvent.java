package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.DeleteEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
public record EmployeeDeleteRequestedEvent(DeleteEmployeeRequest request, ConnectionToClient client) {}
