package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.UpdateEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record EmployeeUpdateRequestedEvent(UpdateEmployeeRequest request, ConnectionToClient client) {}

