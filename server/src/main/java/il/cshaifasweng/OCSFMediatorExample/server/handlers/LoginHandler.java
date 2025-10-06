package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Employee;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Role;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.LoginRequestedEvent;
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

public class LoginHandler {

    private final ServerBus bus;

    public LoginHandler(ServerBus bus) {
        this.bus = bus;
        bus.subscribe(LoginRequestedEvent.class, this::onLoginRequested);
    }

    private void onLoginRequested(LoginRequestedEvent evt) {
        final LoginRequest req = evt.req();
        final ConnectionToClient client = (ConnectionToClient) evt.client();

        try {
            String rawEmail = Optional.ofNullable(req.getUsername()).orElse("").trim();
            String email = rawEmail.toLowerCase(Locale.ROOT);
            String password = Optional.ofNullable(req.getPassword()).orElse("");

            // 1) basic validation
            if (email.isEmpty() || !email.contains("@")) {
                send(client, false, "Invalid email", null, null);
                return;
            }
            if (password.isBlank()) {
                send(client, false, "Password is required", null, null);
                return;
            }

            // 2) reject duplicate active session (optional policy)
            if (SessionManager.get().getByUsername(email).isPresent()) {
                send(client, false, "User already logged in", null, null);
                return;
            }

            // 3) route by role (default to CUSTOMER if null)
            Role role = req.getRole() != null ? req.getRole() : Role.CUSTOMER;
            switch (role) {
                case CUSTOMER -> loginCustomer(email, password, client);
                case SHOP_EMPLOYEE, SHOP_MANAGER, COMPANY_MANAGER, ADMIN -> loginEmployee(email, password, client, role);
                default -> send(client, false, "Unsupported role", null, null);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            send(client, false, "Server error", null, null);
        }
    }

    // ------------------ CUSTOMER ------------------
    private void loginCustomer(String email, String password, ConnectionToClient client) {
        Customer cust = TX.call(s -> {
            Query<Customer> q = s.createQuery(
                    "from Customer c where c.email = :email and c.active = true", // property name 'active' -> column is_active
                    Customer.class
            );
            q.setParameter("email", email);
            return q.uniqueResultOptional().orElse(null);
        });

        if (cust == null) {
            send(client, false, "No such customer or inactive", null, null);
            return;
        }

        if (!matchesPlainOrLegacyHash(password, safeStrip(cust.getPasswordHash()))) {
            send(client, false, "Wrong email or password", null, null);
            return;
        }

        SessionManager.get().registerLogin(
                cust.getEmail().toLowerCase(Locale.ROOT),
                Role.CUSTOMER.name(),
                cust.getDisplayName(),
                client
        );

        send(client, true, null, cust.getDisplayName(), Role.CUSTOMER);
        System.out.printf("[LOGIN] CUSTOMER ok email=%s%n", cust.getEmail());
    }

    // ------------------ EMPLOYEE ------------------
    private void loginEmployee(String email, String password, ConnectionToClient client, Role roleFromReq) {
        Employee emp = TX.call(s -> {
            Query<Employee> q = s.createQuery(
                    "from Employee e where e.email = :email",
                    Employee.class
            );
            q.setParameter("email", email);
            return q.uniqueResultOptional().orElse(null);
        });

        if (emp == null) {
            send(client, false, "No such employee", null, null);
            return;
        }

        if (!matchesPlainOrLegacyHash(password, safeStrip(emp.getPasswordHash()))) {
            send(client, false, "Wrong email or password", null, null);
            return;
        }

        Role effective = roleFromReq; // or derive from emp if you store it there
        String display = emp.getName() != null ? emp.getName() : emp.getEmail();

        SessionManager.get().registerLogin(
                emp.getEmail().toLowerCase(Locale.ROOT),
                effective.name(),
                display,
                client
        );

        send(client, true, null, display, effective);
        System.out.printf("[LOGIN] EMPLOYEE ok email=%s role=%s%n", emp.getEmail(), effective);
    }

    // ------------------ REPLY ------------------
    private void send(ConnectionToClient client, boolean ok, String reason, String display, Role role) {
        LoginResponse resp = new LoginResponse(ok, reason, display, role);
        bus.publish(new SendToClientEvent(resp, client));
    }

    // ------------------ Helpers ------------------

    /** Handle legacy CHAR(64) padding from the dark times. */
    private static String safeStrip(String s) {
        return s == null ? "" : s.stripTrailing();
    }

    /**
     * Primary: plaintext equals.
     * Legacy fallback: if stored looks like a 64-char hex hash, compare against SHA-256(password).
     * This lets old accounts work while you’ve moved to plaintext storage for the course demo.
     */
    private static boolean matchesPlainOrLegacyHash(String suppliedPlain, String stored) {
        if (stored.equals(suppliedPlain)) return true;

        // Legacy SHA-256 hex support
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
