package il.cshaifasweng.OCSFMediatorExample.server.handlers.employee;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.UpdateEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.UpdateEmployeeResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.EmployeeUpdateRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.EmployeeMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

public class EmployeeUpdateHandler {

    public EmployeeUpdateHandler(ServerBus bus) {
        bus.subscribe(EmployeeUpdateRequestedEvent.class, evt -> {
            UpdateEmployeeRequest req = evt.request();
            EmployeesDTO.Update dto = req.getEmployee();

            try {
                Employee updated = TX.call(s -> {
                    Employee e = s.find(Employee.class, dto.getId());
                    if (e == null) {
                        throw new IllegalArgumentException("Employee not found with id=" + dto.getId());
                    }

                    e.setName(dto.getName());
                    e.setGender(dto.getGender());
                    e.setEmail(dto.getEmail());
                    e.setPhone(dto.getPhone());
                    e.setRole(dto.getRole());
                    e.setActive(dto.isActive());
                    e.setSalary(dto.getSalary());

                    s.merge(e);
                    return e;
                });

                EmployeesDTO.Employee payload = EmployeeMapper.toDTO(updated);

                bus.publish(new SendToClientEvent(
                        new UpdateEmployeeResponse(payload),
                        evt.client()
                ));

            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(
                        "Failed to update employee: " + ex.getMessage(),
                        evt.client()
                ));
            }
        });
    }
}
