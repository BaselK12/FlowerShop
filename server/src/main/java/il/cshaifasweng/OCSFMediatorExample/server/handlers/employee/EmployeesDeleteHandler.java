package il.cshaifasweng.OCSFMediatorExample.server.handlers.employee;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.DeleteEmployeeResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.EmployeesDeleteRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.Session;

public class EmployeesDeleteHandler {

    public EmployeesDeleteHandler(ServerBus bus) {
        bus.subscribe(EmployeesDeleteRequestedEvent.class, evt -> {
            long id = evt.employeeId();
            System.out.printf("[EMP] Delete id=%d%n", id);

            try {
                boolean deleted = TXDeleteEmployee(id);

                if (deleted) {
                    bus.publish(new SendToClientEvent(
                            new DeleteEmployeeResponse(true, "Employee deleted", id),
                            evt.client()
                    ));
                } else {
                    bus.publish(new SendToClientEvent(
                            new DeleteEmployeeResponse(false, "Employee not found", id),
                            evt.client()
                    ));
                }
            } catch (ConstraintViolationException fk) {
                // FK constraint (e.g., employee referenced by something)
                bus.publish(new SendToClientEvent(
                        new DeleteEmployeeResponse(false, "Cannot delete: employee is referenced by other data"),
                        evt.client()
                ));
            } catch (Exception ex) {
                ex.printStackTrace();
                bus.publish(new SendToClientEvent(
                        new DeleteEmployeeResponse(false, "Delete failed: " + ex.getMessage()),
                        evt.client()
                ));
            }
        });
    }

    /** Returns true if an entity was removed, false if it didn’t exist. */
    private boolean TXDeleteEmployee(long id) {
        final boolean[] removed = { false };
        TX.run((Session s) -> {
            Employee e = s.get(Employee.class, id);
            if (e != null) {
                s.remove(e);            // Hibernate DELETE
                removed[0] = true;
            }
        });
        return removed[0];
    }
}
