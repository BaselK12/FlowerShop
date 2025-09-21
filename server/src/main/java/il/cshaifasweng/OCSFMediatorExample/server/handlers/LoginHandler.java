// server/handlers/LoginHandler.java
package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.LoginRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.Authz;
//import il.cshaifasweng.OCSFMediatorExample.server.session.UserRepository;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;



public class LoginHandler {
    /*public LoginHandler(ServerBus bus, UserRepository repo, Authz authz) {
        bus.subscribe(LoginRequestedEvent.class, evt -> {
            var req = evt.req();
            System.out.printf("[LOGIN] Attempt by %s%n", req.email());

            try {
                var user = repo.findByEmailAndPassword(req.email(), req.password());
                if (user.isPresent()) {
                    authz.attachSession(evt.client(), user.get());
                    bus.publish(new SendToClientEvent(
                            new LoginResponse(true, "Welcome"), evt.client()
                    ));
                    System.out.printf("[LOGIN] Success for %s%n", req.email());
                } else {
                    bus.publish(new SendToClientEvent(
                            new LoginResponse(false, "Invalid credentials"), evt.client()
                    ));
                    System.out.printf("[LOGIN] Failure for %s%n", req.email());
                }
            } catch (Exception e) {
                bus.publish(new SendToClientEvent(
                        new LoginResponse(false, "Server error"), evt.client()
                ));
                e.printStackTrace();
            }
        });
    }*/
}
