package il.cshaifasweng.OCSFMediatorExample.server.handlers.employee;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;

import java.util.List;

public class EmployeesRepository {

    public static List<Employee> findAll() {
        return TX.call(s ->
                s.createQuery("from Employee e order by e.name asc", Employee.class)
                        .getResultList()
        );
    }

    public static Employee findById(long id) {
        return TX.call(s -> s.get(Employee.class, id));
    }

    public static void save(Employee e) {
        TX.run(s -> s.persist(e));
    }

    public static void update(Employee e) {
        TX.run(s -> s.merge(e));
    }

    public static boolean delete(long id) {
        return TX.call(s -> {
            Employee e = s.get(Employee.class, id);
            if (e != null) {
                s.remove(e);
                return true;
            }
            return false;
        });
    }

    public static List<Employee> findFiltered(boolean activeOnly, EmployeeRole role) {
        return TX.call(s ->
                s.createQuery("from Employee e order by e.name asc", Employee.class)
                        .getResultList()
                        .stream()
                        .filter(e -> !activeOnly || e.isActive())
                        .filter(e -> role == null || e.getRole() == role)
                        .toList()
        );
    }
}