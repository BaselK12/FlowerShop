// server/handlers/RegisterHandler.java
package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.RegisterRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterResponse;

public class RegisterHandler {
    public RegisterHandler(ServerBus bus) {
        bus.subscribe(RegisterRequestedEvent.class, evt -> {
            var req = evt.req();
            System.out.printf("[REGISTER] Attempt username=%s, displayName=%s, phone=%s%n",
                    req.getUsername(), req.getDisplayName(), req.getPhone());

            // TODO: actually persist via Hibernate later
            bus.publish(new SendToClientEvent(
                    new RegisterResponse(false, "Registration not implemented yet"), evt.client()
            ));
        });
    }
}
