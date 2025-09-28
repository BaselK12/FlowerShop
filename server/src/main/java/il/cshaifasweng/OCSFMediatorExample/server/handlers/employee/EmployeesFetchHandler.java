package il.cshaifasweng.OCSFMediatorExample.server.handlers.employee;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.GetEmployeesRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.GetEmployeesResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.EmployeesFetchRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.mapping.EmployeeMapper;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Employee;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;
import java.util.Locale;

public class EmployeesFetchHandler {
    public EmployeesFetchHandler(ServerBus bus) {
        bus.subscribe(EmployeesFetchRequestedEvent.class, evt -> {
            GetEmployeesRequest req = evt.request();
            try {
                List<Employee> results = TX.call(s -> {
                    StringBuilder jpql = new StringBuilder("from Employee e where 1=1");

                    if (req.isActiveOnly()) {
                        jpql.append(" and e.active = true");
                    }
                    if (req.getRole() != null) {
                        jpql.append(" and e.role = :role");
                    }
                    if (req.getSearchText() != null && !req.getSearchText().isBlank()) {
                        jpql.append(" and (lower(e.name) like :q or lower(e.email) like :q or lower(e.phone) like :q)");
                    }

                    var query = s.createQuery(jpql.toString(), Employee.class);

                    if (req.getRole() != null) {
                        query.setParameter("role", req.getRole());
                    }
                    if (req.getSearchText() != null && !req.getSearchText().isBlank()) {
                        query.setParameter("q", "%" + req.getSearchText().toLowerCase(Locale.ROOT) + "%");
                    }

                    return query.getResultList();
                });

                List<EmployeesDTO.Employee> payload = results.stream()
                        .map(EmployeeMapper::toDTO)
                        .toList();

                bus.publish(new SendToClientEvent(new GetEmployeesResponse(payload), evt.client()));

            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(new GetEmployeesResponse(List.of()), evt.client()));
            }
        });
    }
}

