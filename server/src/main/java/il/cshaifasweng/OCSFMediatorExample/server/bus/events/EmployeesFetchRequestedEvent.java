package il.cshaifasweng.OCSFMediatorExample.server.bus.events;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
public record EmployeesFetchRequestedEvent(ConnectionToClient client) {}