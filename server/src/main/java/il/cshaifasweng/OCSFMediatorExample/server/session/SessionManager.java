package il.cshaifasweng.OCSFMediatorExample.server.session;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which user is bound to which connection, and enforces 1 active session per user.
 * Hibernate later will not change this class.
 */
public final class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    /** Minimal session snapshot for handlers and authz checks. */
    public static final class Session {
        private final String username;
        private final String role;        // may be null if you use the 2-arg registerLogin
        private final String displayName; // may be null
        private final ConnectionToClient client;

        public Session(String username, String role, String displayName, ConnectionToClient client) {
            this.username = username;
            this.role = role;
            this.displayName = displayName;
            this.client = client;
        }

        public String username()     { return username; }
        public String role()         { return role; }
        public String displayName()  { return displayName; }
        public ConnectionToClient client() { return client; }
    }

    // client -> session
    private final Map<ConnectionToClient, Session> byClient = new ConcurrentHashMap<>();
    // username -> session
    private final Map<String, Session> byUser = new ConcurrentHashMap<>();

    private SessionManager() {}
    public static SessionManager get() { return INSTANCE; }

    /**
     * Backward-compatible: register with username only (role/displayName unknown).
     * Prefer the 4-arg overload below so role checks can work.
     */
    public synchronized ConnectionToClient registerLogin(String username, ConnectionToClient newClient) {
        return registerLogin(username, /*role*/ null, /*displayName*/ null, newClient);
    }

    /**
     * Register a successful login with role/displayName.
     * Returns any previous connection for that user (caller may choose to close it).
     */
    public synchronized ConnectionToClient registerLogin(String username, String role, String displayName,
                                                         ConnectionToClient newClient) {
        Session oldSession = byUser.get(username);
        ConnectionToClient oldClient = oldSession != null ? oldSession.client() : null;

        if (oldClient != null && oldClient != newClient) {
            // evict old client mapping
            byClient.remove(oldClient);
            try { oldClient.close(); } catch (IOException ignore) {}
        }

        Session s = new Session(username, role, displayName, newClient);
        byUser.put(username, s);
        byClient.put(newClient, s);
        return oldClient;
    }

    /** Remove mappings for this client. Safe to call on logout or disconnect. */
    public synchronized void logout(ConnectionToClient client) {
        Session s = byClient.remove(client);
        if (s != null) {
            byUser.remove(s.username(), s);
        }
    }

    /** Old helper kept for compatibility. */
    public String usernameFor(ConnectionToClient client) {
        Session s = byClient.get(client);
        return s == null ? null : s.username();
    }

    /** New helper: lookup full session by client. */
    public Optional<Session> getByClient(ConnectionToClient client) {
        return Optional.ofNullable(byClient.get(client));
    }

    /** Optional: lookup full session by username. */
    public Optional<Session> getByUsername(String username) {
        return Optional.ofNullable(byUser.get(username));
    }
}
