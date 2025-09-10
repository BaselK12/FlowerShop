package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.GetEmployeesResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.EmployeesFetchRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.EmployeeMapper;
import il.cshaifasweng.OCSFMediatorExample.server.model.Employee;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;

public class EmployeesFetchHandler {
    public EmployeesFetchHandler(ServerBus bus) {
        bus.subscribe(EmployeesFetchRequestedEvent.class, evt -> {
            System.out.println("[EMP] Fetch all employees");

            try {
                List<EmployeesDTO.Employee> payload = TX.call(s ->
                        s.createQuery("from Employee e order by e.name asc", Employee.class)
                                .getResultList()
                                .stream()
                                .map(EmployeeMapper::toDto)
                                .toList()
                );

                bus.publish(new SendToClientEvent(new GetEmployeesResponse(payload), evt.client()));
            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new GetEmployeesResponse(List.of()),  // empty on failure
                        evt.client()
                ));
            }
        });
    }
}
