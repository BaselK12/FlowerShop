package il.cshaifasweng.OCSFMediatorExample.server.handlers;

import com.google.common.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.LoginRequested;

import java.util.HashMap;
import java.util.Map;

// TODO [Hibernate]: inject UserRepository here instead of in-memory map
// TODO [Hibernate]: fetch user by username and verify password/hash
// TODO [Hibernate]: return same LoginResponse; signature unchanged


public class LoginHandler {

    // microscopic demo user store; we’ll replace with Hibernate later
    private final Map<String, User> users = new HashMap<>();

    public LoginHandler() {
        users.put("alice", new User("alice123", "Alice Customer", "CUSTOMER"));
        users.put("boss",  new User("boss123",  "Network Manager", "COMPANY_MANAGER"));
    }

    @Subscribe
    public void onLogin(LoginRequested ev) {
        try {
            User u = users.get(ev.username);
            if (u == null || !u.pass.equals(ev.password)) {
                ev.client.sendToClient(new LoginResponse(false, "Bad credentials", null, null));
                return;
            }
            ev.client.sendToClient(new LoginResponse(true, null, u.display, u.role));
        } catch (Exception e) {
            try { ev.client.sendToClient(new LoginResponse(false, "Server error", null, null)); }
            catch (Exception ignore) {}
            e.printStackTrace();
        }
    }

    private static class User {
        final String pass, display, role;
        User(String pass, String display, String role) { this.pass = pass; this.display = display; this.role = role; }
    }
}
