package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.*;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.complaints.GetComplaintsHandler;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.employee.EmployeesDeleteHandler;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.employee.EmployeesFetchHandler;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.employee.EmployeesOpenEditorHandler;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
// optional smoke (delete after sanity check)


public class App {
    public static void main(String[] args) throws Exception {
        // No Flyway here. We’re using lab-style Hibernate via hibernate.properties.

        var bus = new ServerBus();
        var server = new SimpleServer(3050, bus);

        // infra
        new OutboundSender(bus);

        // handlers
        new CustomerLoginNavHandler(bus);
        new RegisterHandler(bus);
        new EmployeesFetchHandler(bus);
        new EmployeesOpenEditorHandler(bus);
        new EmployeesDeleteHandler(bus);
        new GetComplaintsHandler(bus);

        // optional smoke to force Hibernate to init once (safe to remove)
        TX.run(s -> {
            // comment out if you don’t want demo data
            // s.save(new Employee("Alice", "Female", "alice@example.com", "111-222", "Manager", true, 5000, LocalDate.now()));
        });

        server.listen();
        System.out.println("Server listening on 3000");
    }
}
