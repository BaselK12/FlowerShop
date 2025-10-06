package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.RegisterRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.query.Query;

import java.util.Locale;
import java.util.Optional;

public class RegisterHandler {
    private final ServerBus bus;

    public RegisterHandler(ServerBus bus) {
        this.bus = bus;
        bus.subscribe(RegisterRequestedEvent.class, this::onRegisterRequested);
        System.out.println("[SRV/RegisterHandler] subscribed");
    }

    private void onRegisterRequested(RegisterRequestedEvent evt) {
        RegisterRequest req = evt.req();
        System.out.println("[SRV/RegisterHandler] start email=" + req.getUsername());

        try {
            // validation logs
            if (req.getUsername() == null || !req.getUsername().contains("@")) {
                System.out.println("[SRV/RegisterHandler] invalid email");
                send(evt, RegisterResponse.error("Invalid email"));
                return;
            }
            if (req.getDisplayName() == null || req.getDisplayName().trim().isEmpty()) {
                System.out.println("[SRV/RegisterHandler] missing name");
                send(evt, RegisterResponse.error("Name is required"));
                return;
            }
            if (req.getPassword() == null || req.getPassword().length() < 6) {
                System.out.println("[SRV/RegisterHandler] short password");
                send(evt, RegisterResponse.error("Password must be at least 6 characters"));
                return;
            }

            // duplicate check
            boolean exists = TX.call(s -> {
                System.out.println("[SRV/RegisterHandler] checking duplicate for " + req.getUsername());
                Long n = s.createQuery(
                                "select count(c.id) from Customer c where c.email = :email", Long.class)
                        .setParameter("email", req.getUsername().toLowerCase())
                        .uniqueResult();
                return n != null && n > 0;
            });
            System.out.println("[SRV/RegisterHandler] duplicate? " + exists);
            if (exists) {
                send(evt, RegisterResponse.error("Email already registered"));
                return;
            }

            // persist
            TX.run(s -> {
                System.out.println("[SRV/RegisterHandler] persisting customer " + req.getUsername());
                Customer c = new Customer();
                c.setEmail(req.getUsername().toLowerCase());
                c.setPasswordHash(req.getPassword()); // matches your current login flow
                c.setDisplayName(req.getDisplayName().trim());
                c.setPhone(req.getPhone());
                c.setActive(true);
                s.persist(c);
                System.out.println("[SRV/RegisterHandler] persist done (id assigned on flush)");
            });

            System.out.println("[SRV/RegisterHandler] success " + req.getUsername());
            send(evt, RegisterResponse.success());

        } catch (Exception ex) {
            ex.printStackTrace();
            send(evt, RegisterResponse.error("Server error"));
        }
    }

    private void send(RegisterRequestedEvent evt, RegisterResponse r) {
        System.out.println("[SRV/RegisterHandler] replying -> " + (r.isOk() ? "OK" : r.getReason()));
        bus.publish(new SendToClientEvent(r, evt.client()));
    }
}

