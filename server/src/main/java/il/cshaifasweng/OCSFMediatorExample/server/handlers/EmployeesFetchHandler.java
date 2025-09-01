package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.GetEmployeesResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.EmployeesFetchRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;

import java.util.List;

public class EmployeesFetchHandler {
    public EmployeesFetchHandler(ServerBus bus) {
        bus.subscribe(EmployeesFetchRequestedEvent.class, evt -> {
            System.out.println("[EMP] Fetch all employees");
            // TODO: Hibernate: SELECT ... FROM employees; map to List<EmployeesDTO.Employee>
            bus.publish(new SendToClientEvent(new GetEmployeesResponse(List.of()), evt.client()));
        });
    }
}