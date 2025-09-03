package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.dao.EmployeeDao;
import il.cshaifasweng.OCSFMediatorExample.server.model.Employee;
import il.cshaifasweng.OCSFMediatorExample.server.model.Gender;
import il.cshaifasweng.OCSFMediatorExample.server.model.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EmployeeSmokeTest {
    @Test
    void roundTrip() {
        EmployeeDao dao = new EmployeeDao();
        Employee e = new Employee();
        e.setName("Basel K");
        e.setGender(Gender.MALE);
        e.setEmail("basel@example.com");
        e.setPhone("050-0000000");
        e.setRole(Role.FLORIST);
        e.setActive(true);
        e.setSalary(12000);
        e.setHireDate(LocalDate.now());

        Employee saved = dao.save(e);
        assertNotNull(saved.getId());
    }
}
