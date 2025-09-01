package il.cshaifasweng.OCSFMediatorExample.server.session;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public final class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

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


    public synchronized ConnectionToClient registerLogin(String username, ConnectionToClient newClient) {
        return registerLogin(username, /*role*/ null, /*displayName*/ null, newClient);
    }


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

    public synchronized void logout(ConnectionToClient client) {
        Session s = byClient.remove(client);
        if (s != null) {
            byUser.remove(s.username(), s);
        }
    }

    public String usernameFor(ConnectionToClient client) {
        Session s = byClient.get(client);
        return s == null ? null : s.username();
    }

    public Optional<Session> getByClient(ConnectionToClient client) {
        return Optional.ofNullable(byClient.get(client));
    }

    public Optional<Session> getByUsername(String username) {
        return Optional.ofNullable(byUser.get(username));
    }
}
