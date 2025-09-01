package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.handlers.*;

public class App {
    public static void main(String[] args) throws Exception {
        var bus = new ServerBus();
        var server = new SimpleServer(3000, bus);

        // infra
        new OutboundSender(bus);

        // handlers
        new CustomerLoginNavHandler(bus);
        new RegisterHandler(bus);
        new EmployeesFetchHandler(bus);
        new EmployeesOpenEditorHandler(bus);
        new EmployeesDeleteHandler(bus);

        server.listen();
        System.out.println("Server listening on 3000");
    }
}
