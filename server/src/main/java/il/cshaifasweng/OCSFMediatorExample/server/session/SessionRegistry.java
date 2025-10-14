package il.cshaifasweng.OCSFMediatorExample.server.session;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry {
    private static final ConcurrentHashMap<ConnectionToClient, Long> MAP = new ConcurrentHashMap<>();
    private SessionRegistry() {}

    public static void set(ConnectionToClient client, long customerId) { MAP.put(client, customerId); }
    public static Long get(ConnectionToClient client)                  { return MAP.get(client); }
    public static void clear(ConnectionToClient client)                { MAP.remove(client); }
}
