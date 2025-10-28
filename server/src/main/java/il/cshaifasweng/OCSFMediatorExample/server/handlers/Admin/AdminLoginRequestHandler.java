package il.cshaifasweng.OCSFMediatorExample.server.handlers.Admin;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Admin.AdminLoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Admin.AdminLoginResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.AdminLoginRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionManager;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import org.hibernate.query.Query;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

public class AdminLoginRequestHandler {

    private final ServerBus bus;

    public AdminLoginRequestHandler(ServerBus bus) {
        this.bus = bus;
        bus.subscribe(AdminLoginRequestEvent.class, evt -> {
            AdminLoginRequest req = evt.req();
            final ConnectionToClient client = (ConnectionToClient) evt.client();

            try {
                String rawEmail = Optional.ofNullable(req.getUsername()).orElse("").trim();
                String email = rawEmail.toLowerCase(Locale.ROOT);
                String password = Optional.ofNullable(req.getPassword()).orElse("");

                // 1) Basic validation
                if (email.isEmpty() || !email.contains("@")) {
                    send(client, false, "Invalid email", "Florist");
                    return;
                }
                if (password.isBlank()) {
                    send(client, false, "Password is required", "Florist");
                    return;
                }

                // 2) Reject duplicate active session
                if (SessionManager.get().getByUsername(email).isPresent()) {
                    send(client, false, "Admin already logged in", "Florist");
                    return;
                }

                // 3) Query admin from DB
                Employee admin = TX.call(s -> {
                    Query<Employee> q = s.createQuery(
                            "from Employee e where e.email = :email and e.role = :role",
                            Employee.class
                    );
                    q.setParameter("email", email);
                    q.setParameter("role", "Florist"); // DB role string
                    return q.uniqueResultOptional().orElse(null);
                });

                if (admin == null) {
                    send(client, false, "No such admin account", "Florist");
                    return;
                }

                if (!matchesPlainOrLegacyHash(password, safeStrip(admin.getPasswordHash()))) {
                    send(client, false, "Wrong email or password", "Florist");
                    return;
                }

                // 4) Register session
                String display = admin.getName() != null ? admin.getName() : admin.getEmail();
                SessionManager.get().registerLogin(
                        admin.getEmail().toLowerCase(Locale.ROOT),
                        "Florist",
                        display,
                        client
                );

                send(client, true, "Admin login successful", "Florist");
                System.out.printf("[LOGIN] ADMIN ok email=%s role=%s%n", admin.getEmail(), admin.getRole());

            } catch (Exception ex) {
                ex.printStackTrace();
                send(client, false, "Server error", "Florist");
            }
        });
    }

    private void send(ConnectionToClient client, boolean ok, String message, String role) {
        AdminLoginResponse resp = new AdminLoginResponse(ok, message, role);
        bus.publish(new SendToClientEvent(resp, client));
    }

    private static String safeStrip(String s) {
        return s == null ? "" : s.stripTrailing();
    }

    private static boolean matchesPlainOrLegacyHash(String suppliedPlain, String stored) {
        if (stored.equals(suppliedPlain)) return true;

        if (stored.length() == 64 && stored.matches("^[0-9a-fA-F]{64}$")) {
            String hex = sha256Hex(suppliedPlain);
            return constantTimeEq(stored.toLowerCase(Locale.ROOT), hex);
        }
        return false;
    }

    private static String sha256Hex(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantTimeEq(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}
