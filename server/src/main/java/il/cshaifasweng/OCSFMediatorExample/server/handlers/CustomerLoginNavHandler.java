package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.CustomerLoginNavEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Ack;

public class CustomerLoginNavHandler {
    public CustomerLoginNavHandler(ServerBus bus) {
        bus.subscribe(CustomerLoginNavEvent.class, evt -> {
            System.out.printf("[NAV] %s from %s%n", evt.action(), evt.client());
            bus.publish(new SendToClientEvent(new Ack("Received " + evt.action()), evt.client()));
        });
    }
}
