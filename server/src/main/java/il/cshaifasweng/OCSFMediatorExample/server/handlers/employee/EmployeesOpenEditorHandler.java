package il.cshaifasweng.OCSFMediatorExample.server.handlers.employee;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Ack;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.ErrorResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.EmployeesOpenEditorEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;

public class EmployeesOpenEditorHandler {
    public EmployeesOpenEditorHandler(ServerBus bus) {
        bus.subscribe(EmployeesOpenEditorEvent.class, evt -> {
            switch (evt.mode()) {
                case NEW -> {
                    System.out.println("[EMP] Open editor NEW");
                    bus.publish(new SendToClientEvent(new Ack("EMPLOYEES_OPEN_EDITOR:NEW received"), evt.client()));
                }
                case EDIT -> {
                    System.out.printf("[EMP] Open editor EDIT id=%d%n", evt.employeeId());
                    // TODO: Hibernate: SELECT * FROM employees WHERE id = :id; map to EmployeesDTO.Employee
                    // Example later:
                    // bus.publish(new SendToClientEvent(new GetEmployeeResponse(employee), evt.client()));
                    bus.publish(new SendToClientEvent(new ErrorResponse("Employee fetch not implemented yet"), evt.client()));
                }
            }
        });
    }
}