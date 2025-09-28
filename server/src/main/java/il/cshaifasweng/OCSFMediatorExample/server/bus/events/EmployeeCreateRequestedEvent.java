package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.CreateEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

public record EmployeeCreateRequestedEvent(CreateEmployeeRequest request, ConnectionToClient client) {}

