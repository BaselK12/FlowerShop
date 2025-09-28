package il.cshaifasweng.OCSFMediatorExample.server.handlers.employee;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.CreateEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.CreateEmployeeResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.EmployeeCreateRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.EmployeeMapper;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.time.LocalDate;

public class EmployeeCreateHandler {
    public EmployeeCreateHandler(ServerBus bus) {
        bus.subscribe(EmployeeCreateRequestedEvent.class, evt -> {
            CreateEmployeeRequest req = evt.request();
            EmployeesDTO.Create dto = req.getEmployee();

            try {
                // Save employee in DB transaction
                Employee saved = TX.call(s -> {
                    Employee e = new Employee();
                    e.setName(dto.getName());
                    e.setGender(dto.getGender());
                    e.setEmail(dto.getEmail());
                    e.setPhone(dto.getPhone());
                    e.setRole(dto.getRole());
                    e.setActive(dto.isActive());
                    e.setSalary(dto.getSalary());
                    e.setHireDate(LocalDate.now());

                    // NEW: persist the password hash
                    e.setPasswordHash(dto.getPasswordHash());

                    s.persist(e);
                    return e;
                });

                // Map entity → DTO for response (never include password hash!)
                EmployeesDTO.Employee payload = EmployeeMapper.toDTO(saved);

                // Send back success response
                bus.publish(new SendToClientEvent(
                        new CreateEmployeeResponse(payload),
                        evt.client()
                ));

            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(
                        "Failed to create employee: " + ex.getMessage(),
                        evt.client()
                ));
            }
        });
    }
}