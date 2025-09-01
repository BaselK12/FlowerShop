package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.DeleteEmployeeResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.EmployeesDeleteRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;

public class EmployeesDeleteHandler {
    public EmployeesDeleteHandler(ServerBus bus) {
        bus.subscribe(EmployeesDeleteRequestedEvent.class, evt -> {
            System.out.printf("[EMP] Delete id=%d%n", evt.employeeId());
            // TODO: Hibernate: DELETE FROM employees WHERE id = :id
            bus.publish(new SendToClientEvent(
                    new DeleteEmployeeResponse(false, "Deletion not implemented yet"), evt.client()
            ));
        });
    }
}