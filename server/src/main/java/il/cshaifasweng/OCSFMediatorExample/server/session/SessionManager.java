package il.cshaifasweng.OCSFMediatorExample.server.session;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which user is bound to which connection, and enforces 1 active session per user.
 * Hibernate later will not change this class.
 */
public final class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    private final Map<ConnectionToClient, String> userByClient = new ConcurrentHashMap<>();
    private final Map<String, ConnectionToClient> clientByUser = new ConcurrentHashMap<>();

    private SessionManager() {}
    public static SessionManager get() { return INSTANCE; }

    /** Register a successful login. Returns any previous connection for that user (so caller can close it). */
    public synchronized ConnectionToClient registerLogin(String username, ConnectionToClient newClient) {
        ConnectionToClient old = clientByUser.put(username, newClient);
        if (old != null && old != newClient) {
            userByClient.remove(old);
        }
        userByClient.put(newClient, username);
        return old;
    }

    /** Remove mappings for this client. Safe to call on logout or disconnect. */
    public void logout(ConnectionToClient client) {
        String user = userByClient.remove(client);
        if (user != null) {
            clientByUser.remove(user, client);
        }
    }

    public String usernameFor(ConnectionToClient client) {
        return userByClient.get(client);
    }
}
